package com.example.enggo.api;

public class DeviceTokenRequest {

    public String token;
    public String platform;
    public String deviceId;

    public DeviceTokenRequest(String token, String platform, String deviceId) {
        this.token = token;
        this.platform = platform;
        this.deviceId = deviceId;
    }
}
