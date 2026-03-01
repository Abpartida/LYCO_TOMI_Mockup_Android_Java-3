package com.lyco.tomi.mock;

import android.content.Context;
import android.util.Log;

import com.lyco.tomi.network.ApiEnvironment;
import com.lyco.tomi.network.FastApiClient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Spins up a MockWebServer instance that mimics the FastAPI backend for debug builds.
 */
public final class MockServerController {
    private static final String TAG = "MockServerController";
    private static MockWebServer server;

    private MockServerController() {
    }

    public static synchronized void initIfNeeded(Context context) {
        if (server != null) return;

        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<IOException> startError = new AtomicReference<>();

        Thread starter = new Thread(() -> {
            MockWebServer mock = new MockWebServer();
            mock.setDispatcher(new FastApiDispatcher());
            try {
                mock.start();
                server = mock;
                ApiEnvironment.overrideBaseUrl(mock.url("/").toString());
                FastApiClient.reset();
                Log.i(TAG, "Mock FastAPI server listening at " + ApiEnvironment.getBaseUrl());
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        mock.shutdown();
                    } catch (IOException ignored) {
                    }
                }));
            } catch (IOException e) {
                startError.set(e);
                Log.e(TAG, "Unable to start MockWebServer", e);
                stopServer();
            } finally {
                startLatch.countDown();
            }
        }, "MockWebServerStarter");
        starter.start();

        try {
            if (!startLatch.await(2, TimeUnit.SECONDS)) {
                Log.w(TAG, "Timed out waiting for MockWebServer start; using real backend instead");
                stopServer();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "Interrupted while waiting for mock server start");
            stopServer();
        }
    }

    private static void stopServer() {
        if (server == null) return;
        try {
            server.shutdown();
        } catch (IOException ignored) {
        } finally {
            server = null;
        }
    }

    private static class FastApiDispatcher extends Dispatcher {

        private volatile String lastLiftCommand = "NONE";
        private volatile String lastDriveDirection = "STOP";
    
        @Override
        public MockResponse dispatch(RecordedRequest request) {
            String path = request.getPath();
            String method = request.getMethod();
    
            if (path == null || method == null) {
                return new MockResponse().setResponseCode(400);
            }
    
            // --- Existing endpoints ---
            if (path.startsWith("/auth/login")) {
                return jsonResponse("{\"access_token\":\"mock-token\",\"token_type\":\"bearer\",\"expires_in\":3600,\"message\":\"Mock login succeeded\"}");
            }
    
            if (path.startsWith("/status")) {
                long uptimeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                String body = "{"
                        + "\"system_status\":\"Nominal\","
                        + "\"uptime_seconds\":" + uptimeSeconds + ","
                        + "\"last_updated\":\"" + System.currentTimeMillis()
                        + "}";
                return jsonResponse(body);
            }
    
            // --- Lift endpoints (what your ControlFragment should hit) ---
            if (path.equals("/api/lift/up") && method.equalsIgnoreCase("POST")) {
                lastLiftCommand = "LIFT_UP";
                Log.i(TAG, "Mock received: LIFT_UP");
                return jsonResponse("{\"ok\":true,\"reply\":\"OK\"}");
            }
    
            if (path.equals("/api/lift/down") && method.equalsIgnoreCase("POST")) {
                lastLiftCommand = "LIFT_DOWN";
                Log.i(TAG, "Mock received: LIFT_DOWN");
                return jsonResponse("{\"ok\":true,\"reply\":\"OK\"}");
            }
    
            if (path.equals("/api/lift/stop") && method.equalsIgnoreCase("POST")) {
                lastLiftCommand = "LIFT_STOP";
                Log.i(TAG, "Mock received: LIFT_STOP");
                return jsonResponse("{\"ok\":true,\"reply\":\"OK\"}");
            }
    
            // Optional: a debug endpoint to verify state from the app/tests
            if (path.equals("/api/lift/last") && method.equalsIgnoreCase("GET")) {
                return jsonResponse("{\"ok\":true,\"last\":\"" + lastLiftCommand + "\"}");
            }

            if (path.equals("/api/fan/on") && method.equalsIgnoreCase("POST")) {
                Log.i(TAG, "Mock received: FAN_ON");
                return jsonResponse("{\"ok\":true,\"reply\":\"OK\"}");
            }
            
            if (path.equals("/api/fan/off") && method.equalsIgnoreCase("POST")) {
                Log.i(TAG, "Mock received: FAN_OFF");
                return jsonResponse("{\"ok\":true,\"reply\":\"OK\"}");
            }

            if (path.equals("/api/drive/joystick") && method.equalsIgnoreCase("POST")) {
                String body = request.getBody().readUtf8();
                lastDriveDirection = body;
                Log.i(TAG, "Mock received: DRIVE " + body);
                return jsonResponse("{\"ok\":true,\"reply\":\"OK\"}");
            }

            if (path.equals("/api/drive/stop") && method.equalsIgnoreCase("POST")) {
                lastDriveDirection = "{\"direction\":\"STOP\"}";
                Log.i(TAG, "Mock received: DRIVE_STOP");
                return jsonResponse("{\"ok\":true,\"reply\":\"OK\"}");
            }
    
            return new MockResponse().setResponseCode(404);
        }
    
        private MockResponse jsonResponse(String body) {
            return new MockResponse()
                    .addHeader("Content-Type", "application/json")
                    .setBody(body)
                    .setResponseCode(200);
        }
    }
}
