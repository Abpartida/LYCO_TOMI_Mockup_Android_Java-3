package com.lyco.tomi.network.dto;

import com.squareup.moshi.Json;

public class StatusResponse {
    @Json(name = "system_status")
    private String systemStatus;

    @Json(name = "uptime_seconds")
    private long uptimeSeconds;

    @Json(name = "last_updated")
    private String lastUpdated;

    public String getSystemStatus() {
        return systemStatus;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }
}
