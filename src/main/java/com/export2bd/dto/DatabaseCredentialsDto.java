package com.export2bd.dto;

public class DatabaseCredentialsDto {
    
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
    private String schema;

    public DatabaseCredentialsDto() {
    }

    public DatabaseCredentialsDto(String host, int port, String username, 
                                  String password, String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isValid() {
        return host != null && !host.trim().isEmpty() &&
               port > 0 && port <= 65535 &&
               username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               database != null && !database.trim().isEmpty();
    }
}