package com.export2bd.config;

import com.export2bd.dto.DatabaseCredentialsDto;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class CredentialsManager {

    private static final String CONFIG_DIR = ".export2bd";
    private static final String CREDENTIALS_FILE = "credentials.json";
    private static final Gson gson = new Gson();

    private static CredentialsManager instance;

    private CredentialsManager() {
    }

    public static CredentialsManager getInstance() {
        if (instance == null) {
            instance = new CredentialsManager();
        }
        return instance;
    }

    public void saveCredentials(DatabaseCredentialsDto credentials) throws IOException {
        File configFile = getConfigFile();
        
        File parentDir = configFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        String json = gson.toJson(credentials);
        String encoded = Base64.getEncoder().encodeToString(json.getBytes());

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(encoded);
        }
    }

    public DatabaseCredentialsDto loadCredentials() throws IOException {
        File configFile = getConfigFile();
        
        if (!configFile.exists()) {
            return null;
        }

        String encoded = new String(Files.readAllBytes(configFile.toPath()));
        String json = new String(Base64.getDecoder().decode(encoded));
        
        return gson.fromJson(json, DatabaseCredentialsDto.class);
    }

    public boolean hasCredentials() {
        File configFile = getConfigFile();
        return configFile.exists() && configFile.length() > 0;
    }

    public void clearCredentials() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    private File getConfigFile() {
        String userHome = System.getProperty("user.home");
        Path configPath = Paths.get(userHome, CONFIG_DIR, CREDENTIALS_FILE);
        return configPath.toFile();
    }
}