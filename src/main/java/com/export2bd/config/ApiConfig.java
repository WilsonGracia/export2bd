package com.export2bd.config;

public class ApiConfig {


/**
 * url of the backend API. This should match the URL where your NestJS server is running. Adjust as needed for production or different environments.
 */
public static final String BASE_URL = "http://localhost:3000/export";

/**
 * Endpoint to upload JSON data to the backend. This should match the route defined in your NestJS controller.
 */
public static final String UPLOAD_JSON = BASE_URL + "/upload-json";


}
