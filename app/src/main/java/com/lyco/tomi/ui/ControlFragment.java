package com.lyco.tomi.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lyco.tomi.R;
// ✅ Update these imports to match your actual packages:
import com.lyco.tomi.network.FastApiClient;
import com.lyco.tomi.network.FastApiService;
import com.lyco.tomi.network.dto.JoystickDirectionRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ControlFragment extends Fragment {
    private static final String TAG = "ControlFragment";
    private static final long JOYSTICK_DIRECTION_DEBOUNCE_MS = 150L;
    private static final long JOYSTICK_NEUTRAL_HOLD_MS = 350L;
    private static final long JOYSTICK_HEARTBEAT_INTERVAL_MS = 250L;
    private static final float JOYSTICK_DEAD_ZONE_DP = 24f;
    private String lastDirectionSent;
    private long lastDirectionTimestamp;
    private long neutralSinceTimestamp;
    private float joystickDeadZonePx;
    private FastApiService api;
    private Call<Void> joystickDirectionCallInFlight;
    private final Handler joystickHeartbeatHandler = new Handler(Looper.getMainLooper());
    private final Runnable joystickHeartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (lastDirectionSent == null) {
                return;
            }
            sendDirectionIfNeeded(lastDirectionSent);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_control, container, false);
        joystickDeadZonePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            JOYSTICK_DEAD_ZONE_DP,
            getResources().getDisplayMetrics()
        );

        EditText heightInput = root.findViewById(R.id.inputHeight);
        Button autonomousStart = root.findViewById(R.id.btnAutonomousStart);
        Button autonomousStop = root.findViewById(R.id.btnAutonomousStop);
        Button liftRaise = root.findViewById(R.id.btnLiftRaise);
        Button liftLower = root.findViewById(R.id.btnLiftLower);
        Button fanOn = root.findViewById(R.id.btnFanOn);
        Button fanOff = root.findViewById(R.id.btnFanOff);
        FrameLayout joystickArea = root.findViewById(R.id.joystickArea);
        TextView joystickStatus = root.findViewById(R.id.tvJoystickStatus);

        //  Retrofit service (uses ApiEnvironment baseUrl; MockWebServer overrides it in debug)
        api = FastApiClient.getService();

        // --- Lift: press = move, release = stop ---
        liftRaise.setOnTouchListener((btn, event) -> {
            int a = event.getAction();
            if (a == MotionEvent.ACTION_DOWN) {
                api.liftUp().enqueue(emptyCallback());
                return true;
            } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                api.liftStop().enqueue(emptyCallback());
                return true;
            }
            return false;
        });

        liftLower.setOnTouchListener((btn, event) -> {
            int a = event.getAction();
            if (a == MotionEvent.ACTION_DOWN) {
                api.liftDown().enqueue(emptyCallback());
                return true;
            } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                api.liftStop().enqueue(emptyCallback());
                return true;
            }
            return false;
        });

        // --- Fan: TODO (only enable if you add endpoints to FastApiService) ---
        // If you have these methods in FastApiService, uncomment:
         fanOn.setOnClickListener(v -> api.fanOn().enqueue(emptyCallback()));
         fanOff.setOnClickListener(v -> api.fanOff().enqueue(emptyCallback()));

        // For now, keep them disabled (prevents crashes due to missing methods):
        //fanOn.setEnabled(false);
        //fanOff.setEnabled(false);

        autonomousStart.setOnClickListener(v -> {
            Log.d(TAG, "Start Autonomous Control tapped");
            // TODO: Hook up to backend once endpoint is available.
        });

        autonomousStop.setOnClickListener(v -> {
            Log.d(TAG, "Stop Autonomous Control tapped");
            // TODO: Hook up to backend once endpoint is available.
        });

        // Joystick stays the same
        joystickArea.setOnTouchListener((view, event) ->
            handleJoystick(event, view, joystickStatus)
        );

        // Currently we just keep the user-provided height handy for future controls.
        heightInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                Log.d(TAG, "Height input updated to " + heightInput.getText());
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopJoystickHeartbeat();
        cancelInFlightDirectionCall();
        api = null;
    }

    // Simple reusable callback so we don’t repeat boilerplate
    private Callback<Void> emptyCallback() {
        return new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                // Optional: check response.isSuccessful()
            }

            @Override public void onFailure(Call<Void> call, Throwable t) {
                // Optional: show toast/log
            }
        };
    }

    private boolean handleJoystick(MotionEvent event, View area, TextView status) {
        int action = event.getActionMasked();
        long now = System.currentTimeMillis();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            requestParentDisallowIntercept(area, true);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            requestParentDisallowIntercept(area, false);
            status.setText(R.string.joystick_idle);
            clearNeutral();
            String reason = action == MotionEvent.ACTION_UP ? "ACTION_UP" : "ACTION_CANCEL";
            Log.d(TAG, "Pointer finished via " + reason);
            sendDriveStopIfNeeded(reason);
            return true;
        }

        float centerX = area.getWidth() / 2f;
        float centerY = area.getHeight() / 2f;
        if (centerX == 0 || centerY == 0) {
            status.setText(R.string.joystick_idle);
            markNeutral(now);
            maybeSendNeutralStop(now, "NO_DIMENSIONS");
            return true;
        }

        // Clamp to the bounds so sliding outside the gray pad still produces movement.
        float x = clamp(event.getX(), 0f, area.getWidth());
        float y = clamp(event.getY(), 0f, area.getHeight());

        float dx = x - centerX;
        float dy = centerY - y;
        double distance = Math.hypot(dx, dy);
        if (distance < joystickDeadZonePx) {
            status.setText(R.string.joystick_idle);
            markNeutral(now);
            maybeSendNeutralStop(now, "DEAD_ZONE");
            return true;
        }

        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) angle += 360;

        String direction = directionForAngle(angle);
        status.setText(getString(R.string.joystick_direction, direction));
        clearNeutral();
        sendDirectionIfNeeded(direction);

        return true;
    }

    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private void markNeutral(long now) {
        if (neutralSinceTimestamp == 0L) {
            neutralSinceTimestamp = now;
        }
    }

    private void clearNeutral() {
        neutralSinceTimestamp = 0L;
    }

    private void maybeSendNeutralStop(long now, String reason) {
        if (lastDirectionSent == null) {
            return;
        }
        if (neutralSinceTimestamp == 0L) {
            return;
        }
        if ((now - neutralSinceTimestamp) >= JOYSTICK_NEUTRAL_HOLD_MS) {
            Log.d(TAG, "Neutral hold triggered stop via " + reason);
            sendDriveStopIfNeeded("NEUTRAL_" + reason);
        }
    }

    private void requestParentDisallowIntercept(View area, boolean disallow) {
        ViewParent parent = area.getParent();
        while (parent != null) {
            // Prevent ScrollView (and any ancestor) from cancelling long joystick drags.
            parent.requestDisallowInterceptTouchEvent(disallow);
            parent = parent.getParent();
        }
    }

    private String directionForAngle(double angle) {
        if (angle >= 337.5 || angle < 22.5) return getString(R.string.direction_right);
        if (angle < 67.5) return getString(R.string.direction_forward_right);
        if (angle < 112.5) return getString(R.string.direction_forward);
        if (angle < 157.5) return getString(R.string.direction_forward_left);
        if (angle < 202.5) return getString(R.string.direction_left);
        if (angle < 247.5) return getString(R.string.direction_backward_left);
        if (angle < 292.5) return getString(R.string.direction_backward);
        if (angle < 337.5) return getString(R.string.direction_backward_right);
        return getString(R.string.direction_center);
    }

    private void sendDirectionIfNeeded(String direction) {
        if (api == null) {
            Log.w(TAG, "Cannot send joystick direction; api is null");
            startJoystickHeartbeat();
            return;
        }
        long now = System.currentTimeMillis();
        if (direction.equals(lastDirectionSent) && (now - lastDirectionTimestamp) < JOYSTICK_DIRECTION_DEBOUNCE_MS) {
            startJoystickHeartbeat();
            return;
        }

        if (joystickDirectionCallInFlight != null) {
            Log.d(TAG, "Joystick direction call still in flight; skipping new send");
            startJoystickHeartbeat();
            return;
        }

        lastDirectionSent = direction;
        lastDirectionTimestamp = now;
        Call<Void> call = api.sendJoystickDirection(new JoystickDirectionRequest(direction));
        joystickDirectionCallInFlight = call;
        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "Joystick direction request failed with code " + response.code());
                }
                clearInFlightCall(call);
            }

            @Override public void onFailure(Call<Void> call, Throwable t) {
                if (!call.isCanceled()) {
                    Log.w(TAG, "Joystick direction request error", t);
                }
                clearInFlightCall(call);
            }
        });
        startJoystickHeartbeat();
    }

    private void sendDriveStopIfNeeded(String reason) {
        if (lastDirectionSent == null) {
            Log.d(TAG, "Skip stop (" + reason + ") because nothing is active");
            return;
        }
        if (api == null) {
            Log.w(TAG, "Cannot send stop; api is null");
            lastDirectionSent = null;
            lastDirectionTimestamp = 0L;
            neutralSinceTimestamp = 0L;
            cancelInFlightDirectionCall();
            stopJoystickHeartbeat();
            return;
        }
        Log.d(TAG, "Sending stop (" + reason + ") after " + lastDirectionSent);
        lastDirectionSent = null;
        lastDirectionTimestamp = 0L;
        neutralSinceTimestamp = 0L;
        cancelInFlightDirectionCall();
        stopJoystickHeartbeat();
        api.stopDrive().enqueue(emptyCallback());
    }

    private void startJoystickHeartbeat() {
        joystickHeartbeatHandler.removeCallbacks(joystickHeartbeatRunnable);
        joystickHeartbeatHandler.postDelayed(joystickHeartbeatRunnable, JOYSTICK_HEARTBEAT_INTERVAL_MS);
    }

    private void stopJoystickHeartbeat() {
        joystickHeartbeatHandler.removeCallbacks(joystickHeartbeatRunnable);
    }

    private void cancelInFlightDirectionCall() {
        if (joystickDirectionCallInFlight != null) {
            joystickDirectionCallInFlight.cancel();
            joystickDirectionCallInFlight = null;
        }
    }

    private void clearInFlightCall(Call<Void> completedCall) {
        if (joystickDirectionCallInFlight == completedCall) {
            joystickDirectionCallInFlight = null;
        }
    }
}
