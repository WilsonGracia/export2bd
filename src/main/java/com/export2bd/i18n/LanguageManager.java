package com.export2bd.i18n;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * Manages application language/locale and provides translation services.
 * Persists language preference across application restarts.
 */
public class LanguageManager {

    private static LanguageManager instance;
    
    private Locale currentLocale;
    private ResourceBundle bundle;
    private final List<LanguageChangeListener> listeners = new ArrayList<>();
    
    // Preferences key for persisting language
    private static final String PREF_LANGUAGE = "app.language";
    private final Preferences prefs = Preferences.userNodeForPackage(LanguageManager.class);

    private LanguageManager() {
        loadSavedLanguage();
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    /**
     * Load the saved language preference or default to Spanish
     */
    private void loadSavedLanguage() {
        String savedLang = prefs.get(PREF_LANGUAGE, "es");
        currentLocale = new Locale(savedLang);
        loadBundle();
    }

    /**
     * Load the resource bundle for the current locale
     */
    private void loadBundle() {
        try {
            bundle = ResourceBundle.getBundle("messages", currentLocale);
        } catch (MissingResourceException e) {
            System.err.println("Could not load resource bundle for locale: " + currentLocale);
            // Fallback to Spanish
            currentLocale = new Locale("es");
            bundle = ResourceBundle.getBundle("messages", currentLocale);
        }
    }

    /**
     * Set the application language
     * @param language Language code ("es" for Spanish, "en" for English)
     */
    public void setLanguage(String language) {
        if (!language.equals(currentLocale.getLanguage())) {
            currentLocale = new Locale(language);
            loadBundle();
            
            // Persist the choice
            prefs.put(PREF_LANGUAGE, language);
            
            // Notify listeners
            notifyListeners();
        }
    }

    /**
     * Get the current language code
     * @return "es" or "en"
     */
    public String getCurrentLanguage() {
        return currentLocale.getLanguage();
    }

    /**
     * Get a translated string by key
     * @param key Translation key
     * @return Translated string
     */
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("Missing translation key: " + key);
            return "!" + key + "!";
        }
    }

    /**
     * Get a translated string with parameters
     * @param key Translation key
     * @param params Parameters to replace in the string
     * @return Formatted translated string
     */
    public String get(String key, Object... params) {
        try {
            String pattern = bundle.getString(key);
            return String.format(pattern, params);
        } catch (MissingResourceException e) {
            System.err.println("Missing translation key: " + key);
            return "!" + key + "!";
        }
    }

    /**
     * Translate backend error messages intelligently.
     * Only translates known error patterns, preserves technical details.
     * 
     * @param backendMessage Original error message from backend
     * @return Translated message with preserved technical details
     */
    public String translateBackendError(String backendMessage) {
        if (backendMessage == null || backendMessage.isEmpty()) {
            return get("error.unknown");
        }

        String lower = backendMessage.toLowerCase();

        // Connection errors
        if (lower.contains("connection") || lower.contains("connect")) {
            if (lower.contains("refused") || lower.contains("timeout")) {
                return get("error.connection.refused");
            }
            return get("error.connection.general");
        }

        // Authentication errors
        if (lower.contains("authentication") || lower.contains("unauthorized") || 
            lower.contains("401")) {
            return get("error.auth.failed");
        }

        // Validation errors - preserve field names
        if (lower.contains("required") || lower.contains("mandatory")) {
            // Extract field name if present
            String fieldName = extractFieldName(backendMessage);
            if (fieldName != null) {
                return get("error.validation.required", fieldName);
            }
            return get("error.validation.required.general");
        }

        if (lower.contains("invalid") || lower.contains("not valid")) {
            String fieldName = extractFieldName(backendMessage);
            if (fieldName != null) {
                return get("error.validation.invalid", fieldName);
            }
            return get("error.validation.invalid.general");
        }

        if (lower.contains("duplicate") || lower.contains("already exists")) {
            String fieldName = extractFieldName(backendMessage);
            if (fieldName != null) {
                return get("error.validation.duplicate", fieldName);
            }
            return get("error.validation.duplicate.general");
        }

        // Database errors
        if (lower.contains("database") || lower.contains("sql")) {
            return get("error.database");
        }

        // File errors
        if (lower.contains("file not found")) {
            return get("error.file.notfound");
        }
        if (lower.contains("file") && (lower.contains("read") || lower.contains("access"))) {
            return get("error.file.read");
        }

        // Server errors
        if (lower.contains("500") || lower.contains("internal server")) {
            return get("error.server.internal");
        }
        if (lower.contains("503") || lower.contains("unavailable")) {
            return get("error.server.unavailable");
        }

        // Network errors
        if (lower.contains("network") || lower.contains("host")) {
            return get("error.network");
        }

        // If no pattern matches, return original message
        // (preserves technical details, table names, etc.)
        return backendMessage;
    }

    /**
     * Extract field name from error message
     * Looks for patterns like "field 'name'" or "field: name"
     */
    private String extractFieldName(String message) {
        // Try to extract field name from common patterns
        // Pattern 1: field 'name' or field "name"
        int startQuote = message.indexOf("'");
        if (startQuote == -1) startQuote = message.indexOf("\"");
        
        if (startQuote != -1) {
            int endQuote = message.indexOf("'", startQuote + 1);
            if (endQuote == -1) endQuote = message.indexOf("\"", startQuote + 1);
            
            if (endQuote != -1) {
                return message.substring(startQuote + 1, endQuote);
            }
        }
        
        // Pattern 2: field: name or field = name
        String[] parts = message.split("[:=]");
        if (parts.length > 1) {
            String fieldName = parts[1].trim().split("\\s+")[0];
            return fieldName.replaceAll("[^a-zA-Z0-9_]", "");
        }
        
        return null;
    }

    /**
     * Add a listener to be notified of language changes
     */
    public void addListener(LanguageChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     */
    public void removeListener(LanguageChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of a language change
     */
    private void notifyListeners() {
        for (LanguageChangeListener listener : listeners) {
            listener.onLanguageChanged(currentLocale.getLanguage());
        }
    }

    /**
     * Listener interface for language changes
     */
    public interface LanguageChangeListener {
        void onLanguageChanged(String newLanguage);
    }
}