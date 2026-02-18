package com.export2bd.services;

import com.export2bd.dto.UploadResultDto;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ExportApiService {

    private static final Gson gson = new Gson();
    private static final String BOUNDARY = "----WebKitFormBoundary7MA4YWxkTrZu0gW";

    public UploadResultDto uploadFileWithCredentials(File file) throws IOException {
        
        AuthService authService = AuthService.getInstance();
        
        if (!authService.hasValidToken()) {
            throw new RuntimeException("No valid authentication token. Please configure credentials in Settings.");
        }

        String token = authService.getToken();
        
        String endpoint = "http://localhost:3000/export/upload-with-credentials";
        URL url = URI.create(endpoint).toURL();
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            
            writeString(os, "--" + BOUNDARY + "\r\n");
            writeString(os, "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            writeString(os, "Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n");
            
            Files.copy(file.toPath(), os);
            
            writeString(os, "\r\n--" + BOUNDARY + "--\r\n");
        }

        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200 || responseCode == 201) {
            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(responseBody, UploadResultDto.class);
            
        } else if (responseCode == 401) {
            authService.clearToken();
            throw new RuntimeException("authentication expired, please save credentials again in Settings.");
            
        } else {
            String errorBody = "";
            try {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                errorBody = "Unknown error occurred.";
            }
            throw new RuntimeException("Upload failed: " + errorBody);
        }
    }

    private void writeString(OutputStream os, String str) throws IOException {
        os.write(str.getBytes(StandardCharsets.UTF_8));
    }
}