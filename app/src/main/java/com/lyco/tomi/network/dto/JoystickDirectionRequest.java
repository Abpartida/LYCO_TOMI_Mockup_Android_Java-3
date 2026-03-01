package com.lyco.tomi.network.dto;

/**
 * Payload for sending joystick direction commands to the FastAPI backend.
 */
public class JoystickDirectionRequest {
    private final String direction;

    public JoystickDirectionRequest(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }
}
