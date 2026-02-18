package com.export2bd.theme;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Singleton that holds the current dark/light theme state.
 * Any view can subscribe via addListener() and will be notified
 * whenever the theme changes. No direct node references needed.
 * 
 * IMPROVED: Better contrast and readability in dark mode
 */
public class ThemeManager {

    // ── Singleton ────────────────────────────────────────────────────────────
    private static final ThemeManager INSTANCE = new ThemeManager();
    public static ThemeManager getInstance() { return INSTANCE; }
    private ThemeManager() {}

    // ── State ────────────────────────────────────────────────────────────────
    private boolean dark = false; // default: light
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();

    // ── API ──────────────────────────────────────────────────────────────────

    public boolean isDark() { return dark; }

    /** Switches to the opposite theme and notifies all subscribers. */
    public void toggle() { setDark(!dark); }

    /** Sets the theme explicitly and notifies all subscribers. */
    public void setDark(boolean dark) {
        this.dark = dark;
        listeners.forEach(l -> l.accept(dark));
    }

    /**
     * Registers a listener that is called immediately with the current
     * theme and again on every future change.
     */
    public void addListener(Consumer<Boolean> listener) {
        listeners.add(listener);
        listener.accept(dark); // apply current theme right away
    }

    // ── Color palette - IMPROVED DARK MODE ───────────────────────────────────
    
    /** Main background color */
    public String bg() { 
        return dark ? "#1a1f2e" : "#f6f7fb"; 
    }
    
    /** Panel/Card background */
    public String panel() { 
        return dark ? "#242b3d" : "#ffffff"; 
    }
    
    /** Border color */
    public String border() { 
        return dark ? "#3d4556" : "#e5e7eb"; 
    }
    
    /** Primary text color - HIGH CONTRAST */
    public String text() { 
        return dark ? "#e8edf4" : "#111827"; 
    }
    
    /** Muted/Secondary text - BETTER READABILITY */
    public String muted() { 
        return dark ? "#b8c1d3" : "#6b7280"; 
    }
    
    /** Sidebar background */
    public String sidebarBg() { 
        return dark ? "#1f2535" : "#ffffff"; 
    }
    
    /** Input field background - MORE CONTRAST */
    public String input() { 
        return dark ? "#2d3548" : "#f3f4f6"; 
    }
    
    /** Card hover state */
    public String cardHover() { 
        return dark ? "#2f3748" : "#f3f4f6"; 
    }
    
    /** Accent color for interactive elements */
    public String accent() {
        return dark ? "#5b9cf5" : "#3b82f6";
    }
    
    /** Success color */
    public String success() {
        return dark ? "#4ade80" : "#22c55e";
    }
    
    /** Error/Danger color */
    public String error() {
        return dark ? "#f87171" : "#ef4444";
    }
    
    /** Warning color */
    public String warning() {
        return dark ? "#fbbf24" : "#f59e0b";
    }

    // ── Reusable style strings ────────────────────────────────────────────────

    public String cardStyle() {
        return "-fx-background-color: " + panel() + ";" +
               "-fx-border-color: "     + border() + ";" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 12;" +
               "-fx-background-radius: 12;";
    }

    public String inputStyle() {
        return "-fx-background-color: " + input() + ";" +
               "-fx-background-radius: 8;" +
               "-fx-border-color: "     + border() + ";" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 8;" +
               "-fx-padding: 10 12;" +
               "-fx-text-fill: " + text() + ";" +
               "-fx-font-size: 14;";
    }
    
    /** ComboBox style with proper text visibility */
    public String comboBoxStyle() {
        return "-fx-background-color: " + input() + ";" +
               "-fx-background-radius: 8;" +
               "-fx-border-color: "     + border() + ";" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 8;" +
               "-fx-text-fill: " + text() + ";" +
               "-fx-font-size: 14;" +
               "-fx-padding: 6 12;" +
               // Para el texto del item seleccionado
               "-fx-prompt-text-fill: " + muted() + ";";
    }

    /** Standard sidebar/menu button style - IMPROVED CONTRAST */
    public String menuButtonStyle() {
        return "-fx-background-color: transparent;" +
               "-fx-text-fill: "         + text()  + ";" +
               "-fx-background-radius: 8;" +
               "-fx-alignment: CENTER-LEFT;" +
               "-fx-padding: 0 12;" +
               "-fx-font-size: 14;";
    }
    
    /** Menu button hover state */
    public String menuButtonHoverStyle() {
        return "-fx-background-color: " + input() + ";" +
               "-fx-text-fill: "         + text()  + ";" +
               "-fx-background-radius: 8;" +
               "-fx-alignment: CENTER-LEFT;" +
               "-fx-padding: 0 12;" +
               "-fx-font-size: 14;";
    }

    /** Primary action button - BETTER VISIBILITY IN BOTH THEMES */
    public String primaryButtonStyle() {
        return "-fx-background-color: " + (dark ? "#5b9cf5" : "#111827") + ";" +
               "-fx-text-fill: " + (dark ? "#0f1419" : "white") + ";" +
               "-fx-font-weight: bold;" +
               "-fx-font-size: 14;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 8 16;" +
               "-fx-cursor: hand;";
    }

    public String primaryButtonHoverStyle() {
        return "-fx-background-color: " + (dark ? "#7ab2f7" : "#374151") + ";" +
               "-fx-text-fill: " + (dark ? "#0f1419" : "white") + ";" +
               "-fx-font-weight: bold;" +
               "-fx-font-size: 14;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 8 16;" +
               "-fx-cursor: hand;";
    }
    
    /** Secondary button style */
    public String secondaryButtonStyle() {
        return "-fx-background-color: transparent;" +
               "-fx-border-color: " + border() + ";" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 8;" +
               "-fx-text-fill: " + text() + ";" +
               "-fx-font-size: 14;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 8 16;" +
               "-fx-cursor: hand;";
    }
    
    public String secondaryButtonHoverStyle() {
        return "-fx-background-color: " + input() + ";" +
               "-fx-border-color: " + border() + ";" +
               "-fx-border-width: 1;" +
               "-fx-border-radius: 8;" +
               "-fx-text-fill: " + text() + ";" +
               "-fx-font-size: 14;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 8 16;" +
               "-fx-cursor: hand;";
    }
}