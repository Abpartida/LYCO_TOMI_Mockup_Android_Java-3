package com.lyco.tomi.network;

import com.lyco.tomi.BuildConfig;

/**
 * Holds the base URL that Retrofit should talk to, allowing tests/mocks to override it at runtime.
 */
public final class ApiEnvironment {
    private static volatile String baseUrl = sanitize(BuildConfig.FASTAPI_BASE_URL);

    private ApiEnvironment() {
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void overrideBaseUrl(String newBaseUrl) {
        baseUrl = sanitize(newBaseUrl);
    }

    private static String sanitize(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be empty");
        }
        String trimmed = url.trim();
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }
}
