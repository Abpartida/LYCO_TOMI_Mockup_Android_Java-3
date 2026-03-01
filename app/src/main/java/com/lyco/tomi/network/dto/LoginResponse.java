package com.lyco.tomi.network.dto;

import com.squareup.moshi.Json;

public class LoginResponse {
    @Json(name = "access_token")
    private String accessToken;

    @Json(name = "token_type")
    private String tokenType;

    @Json(name = "expires_in")
    private long expiresIn;

    @Json(name = "message")
    private String message;

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getMessage() {
        return message;
    }
}
