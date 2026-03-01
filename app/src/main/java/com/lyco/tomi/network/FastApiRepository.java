package com.lyco.tomi.network;

import com.lyco.tomi.network.dto.LoginRequest;
import com.lyco.tomi.network.dto.LoginResponse;
import com.lyco.tomi.network.dto.StatusResponse;

import retrofit2.Call;

/**
 * Repository that wraps Retrofit calls so UI components can remain cleaner.
 */
public class FastApiRepository {
    private final FastApiService service;

    public FastApiRepository(FastApiService service) {
        this.service = service;
    }

    public static FastApiRepository getDefault() {
        return new FastApiRepository(FastApiClient.getService());
    }

    public Call<LoginResponse> login(String username, String password) {
        return service.login(new LoginRequest(username, password));
    }

    public Call<StatusResponse> fetchStatus() {
        return service.fetchStatus();
    }
}
