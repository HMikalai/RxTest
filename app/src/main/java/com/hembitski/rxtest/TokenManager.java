package com.hembitski.rxtest;

public class TokenManager {

    private static TokenManager tokenManager;

    private String accessToken;
    private String refreshToken;

    private TokenManager() {}

    public static TokenManager getInstance() {
        if(tokenManager == null) {
            tokenManager = new TokenManager();
        }
        return tokenManager;
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }

    public synchronized void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public synchronized String getRefreshToken() {
        return refreshToken;
    }

    public synchronized void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
