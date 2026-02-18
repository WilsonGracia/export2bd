package com.export2bd.services;

import com.export2bd.dto.DatabaseCredentialsDto;
import com.export2bd.dto.LoginResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class AuthService {

    private static AuthService instance;
    private static final Gson gson = new Gson();

    private String currentToken;
    private long tokenExpiresAt;

    private AuthService() {
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public LoginResponse login(DatabaseCredentialsDto credentials) throws IOException {
        
        String endpoint = "http://localhost:3000/auth/login";
        URL url = URI.create(endpoint).toURL();
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonBody = gson.toJson(credentials);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200 || responseCode == 201) {
            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            LoginResponse response = gson.fromJson(responseBody, LoginResponse.class);
            
            this.currentToken = response.getAccess_token();
            this.tokenExpiresAt = Instant.now().toEpochMilli() + (response.getExpires_in() * 1000L);
            
            return response;
            
        } else if (responseCode == 401) {
            throw new RuntimeException("Invalid database credentials");
        } else {
            String errorBody = "";
            try {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                // Ignore
            }
            throw new RuntimeException("Login failed: " + errorBody);
        }
    }

    public String getToken() {
        return currentToken;
    }

    public boolean hasValidToken() {
        if (currentToken == null || currentToken.isEmpty()) {
            return false;
        }
        
        long now = Instant.now().toEpochMilli();
        return tokenExpiresAt > (now + 60000);
    }

    public boolean isTokenExpiringSoon() {
        if (currentToken == null) {
            return true;
        }
        
        long now = Instant.now().toEpochMilli();
        return tokenExpiresAt < (now + 300000);
    }

    public void clearToken() {
        this.currentToken = null;
        this.tokenExpiresAt = 0;
    }

    public LoginResponse refreshToken() throws IOException {
        
        if (currentToken == null) {
            throw new RuntimeException("No token to refresh");
        }
        
        String endpoint = "http://localhost:3000/auth/refresh";
        URL url = URI.create(endpoint).toURL();
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + currentToken);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200 || responseCode == 201) {
            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            LoginResponse response = gson.fromJson(responseBody, LoginResponse.class);
            
            this.currentToken = response.getAccess_token();
            this.tokenExpiresAt = Instant.now().toEpochMilli() + (response.getExpires_in() * 1000L);
            
            return response;
            
        } else {
            clearToken();
            throw new RuntimeException("Token refresh failed");
        }
    }

    public boolean validateToken() {
        
        if (currentToken == null) {
            return false;
        }
        
        try {
            String endpoint = "http://localhost:3000/auth/validate";
            URL url = URI.create(endpoint).toURL();
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + currentToken);
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
            
        } catch (IOException e) {
            return false;
        }
    }
}