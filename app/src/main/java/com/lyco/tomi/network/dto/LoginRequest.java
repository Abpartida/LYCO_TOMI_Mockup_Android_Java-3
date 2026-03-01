package com.lyco.tomi.network.dto;

import com.squareup.moshi.Json;

public class LoginRequest {
    @Json(name = "username")
    private final String username;

    @Json(name = "password")
    private final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
