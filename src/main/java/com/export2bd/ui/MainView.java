package com.export2bd.ui;

import com.export2bd.i18n.LanguageManager;
import com.export2bd.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainView {

    private BorderPane root;
    private Label appName;
    private Button exportBtn;
    private Button settingsBtn;

    public Scene build() {

        ThemeManager theme = ThemeManager.getInstance();
        LanguageManager lang = LanguageManager.getInstance();

        //  App name 
        appName = new Label(lang.get("app.name"));
        appName.setFont(Font.font("System", FontWeight.BOLD, 16));

        //  Nav buttons 
        exportBtn   = new Button(lang.get("menu.export"));
        settingsBtn = new Button(lang.get("menu.settings"));

        for (Button b : new Button[]{ exportBtn, settingsBtn }) {
            b.setPrefHeight(40);
            b.setMaxWidth(Double.MAX_VALUE);
        }

        //  Sidebar 
        VBox sidebar = new VBox(10, appName, new Separator(),
                exportBtn, settingsBtn);
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(200);

        //  Content views 
        ExportView   exportView   = new ExportView();
        SettingsView settingsView = new SettingsView();

        root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(exportView.build());

        exportBtn.setOnAction(e -> root.setCenter(exportView.build()));
        settingsBtn.setOnAction(e -> root.setCenter(settingsView.build()));

        //  Subscribe to theme changes 
        theme.addListener(dark -> applyTheme(theme));

        //  Subscribe to language changes 
        lang.addListener(newLang -> {
            updateTexts(lang);
            // Rebuild the current view to reflect language changes
            if (root.getCenter() instanceof VBox) {
                VBox currentView = (VBox) root.getCenter();
                // Check which view is currently displayed and rebuild it
                String firstLabelText = "";
                if (currentView.getChildren().size() > 0 && 
                    currentView.getChildren().get(0) instanceof VBox) {
                    VBox headerBox = (VBox) currentView.getChildren().get(0);
                    if (headerBox.getChildren().size() > 0 && 
                        headerBox.getChildren().get(0) instanceof Label) {
                        firstLabelText = ((Label) headerBox.getChildren().get(0)).getText();
                    }
                }
                
                // Rebuild the appropriate view
                if (firstLabelText.contains("Export") || firstLabelText.contains("Conversor") || 
                    firstLabelText.contains("Document")) {
                    root.setCenter(exportView.build());
                } else {
                    root.setCenter(settingsView.build());
                }
            }
        });

       
        applyTheme(theme);

        Scene scene = new Scene(root, 980, 620);
        return scene;
    }

    private void updateTexts(LanguageManager lang) {
        appName.setText(lang.get("app.name"));
        exportBtn.setText(lang.get("menu.export"));
        settingsBtn.setText(lang.get("menu.settings"));
    }

    private void applyTheme(ThemeManager theme) {
        root.setStyle("-fx-background-color: " + theme.bg() + ";");
        
        VBox sidebar = (VBox) root.getLeft();
        sidebar.setStyle(
                "-fx-background-color: " + theme.sidebarBg() + ";" +
                "-fx-border-color: "     + theme.border()    + ";" +
                "-fx-border-width: 0 1 0 0;"
        );
        appName.setStyle("-fx-text-fill: " + theme.text() + ";");

        for (Button b : new Button[]{ exportBtn, settingsBtn }) {
            b.setStyle(theme.menuButtonStyle());
        }
    }
}