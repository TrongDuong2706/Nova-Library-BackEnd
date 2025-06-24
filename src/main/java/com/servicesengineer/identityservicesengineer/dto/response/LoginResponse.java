package com.servicesengineer.identityservicesengineer.dto.response;

public class LoginResponse {
    private String token;
    private boolean authenticated;
    public LoginResponse() {
    }
    public LoginResponse(String token, boolean authenticated) {
        this.token = token;
        this.authenticated = authenticated;
    }
    public String getToken() {
        return token;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
