package com.lyco.tomi.network;

import com.lyco.tomi.network.dto.JoystickDirectionRequest;
import com.lyco.tomi.network.dto.LoginRequest;
import com.lyco.tomi.network.dto.LoginResponse;
import com.lyco.tomi.network.dto.StatusResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Retrofit contract that mirrors the FastAPI endpoints the app depends on.
 */
public interface FastApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("status")
    Call<StatusResponse> fetchStatus();

    @POST("/api/lift/up")
    Call<Void> liftUp();

    @POST("/api/lift/down")
    Call<Void> liftDown();

    @POST("/api/lift/stop")
    Call<Void> liftStop();

    @POST("/api/fan/on")
    Call<Void> fanOn();

    @POST("/api/fan/off")
    Call<Void> fanOff();

    @POST("/api/drive/joystick")
    Call<Void> sendJoystickDirection(@Body JoystickDirectionRequest request);

    @POST("/api/drive/stop")
    Call<Void> stopDrive();
}
