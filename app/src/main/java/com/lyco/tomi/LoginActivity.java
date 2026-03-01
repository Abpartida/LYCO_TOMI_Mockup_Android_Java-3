package com.lyco.tomi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lyco.tomi.network.FastApiRepository;
import com.lyco.tomi.network.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private FastApiRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        repository = FastApiRepository.getDefault();

        EditText etUser = findViewById(R.id.etUsername);
        EditText etPass = findViewById(R.id.etPassword);
        Button btn = findViewById(R.id.btnLogin);

        btn.setOnClickListener(v -> attemptLogin(etUser, etPass, btn));
    }

    private void attemptLogin(EditText etUser, EditText etPass, Button btn) {
        String username = etUser.getText().toString().trim();
        String password = etPass.getText().toString();

        if (TextUtils.isEmpty(username)) {
            etUser.setError("Username required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPass.setError("Password required");
            return;
        }

        btn.setEnabled(false);

        repository.login(username, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btn.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && !TextUtils.isEmpty(response.body().getAccessToken())) {
                    Toast.makeText(LoginActivity.this, "Connected to TOMI", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    String errorMessage = response.body() != null && !TextUtils.isEmpty(response.body().getMessage())
                        ? response.body().getMessage()
                        : "Unable to login. Check credentials or FastAPI server.";
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btn.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
