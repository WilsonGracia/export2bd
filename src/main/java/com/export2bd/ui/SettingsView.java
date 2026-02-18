package com.export2bd.ui;

import com.export2bd.config.CredentialsManager;
import com.export2bd.dto.DatabaseCredentialsDto;
import com.export2bd.dto.LoginResponse;
import com.export2bd.i18n.LanguageManager;
import com.export2bd.services.AuthService;
import com.export2bd.theme.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SettingsView {

    private Label title;
    private Label subtitle;
    private Label credentialsSectionTitle;
    private Label interfaceSectionTitle;
    private Label[] credentialsLabels;
    private Label languageLabel;
    private Label themeLabel;
    private Label statusLabel;
    private Button saveBtn;
    private ComboBox<String> languageCombo;
    private ComboBox<String> themeCombo;

    private TextField hostField;
    private TextField portField;
    private TextField userField;
    private PasswordField passField;
    private TextField nameField;

    public VBox build() {

        ThemeManager theme = ThemeManager.getInstance();
        LanguageManager lang = LanguageManager.getInstance();

        title = new Label(lang.get("settings.title"));
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        subtitle = new Label(lang.get("settings.subtitle"));

        VBox header = new VBox(6, title, subtitle);

        VBox credentialsSection = buildCredentialsSection(theme, lang);
        VBox interfaceSection   = buildInterfaceSection(theme, lang);

        VBox root = new VBox(18, header, credentialsSection, interfaceSection);
        root.setPadding(new Insets(22));
        root.setMaxWidth(700);
        root.setAlignment(Pos.TOP_LEFT);

        theme.addListener(dark -> {
            root.setStyle("-fx-background-color: " + theme.bg() + ";");
            title.setStyle("-fx-text-fill: " + theme.text() + ";");
            subtitle.setStyle("-fx-text-fill: " + theme.muted() + ";");
        });

        lang.addListener(newLang -> updateTexts(lang));

        return root;
    }

    private VBox buildCredentialsSection(ThemeManager theme, LanguageManager lang) {

        credentialsSectionTitle = new Label(lang.get("settings.credentials.title"));
        credentialsSectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        hostField = new TextField();
        portField = new TextField();
        userField = new TextField();
        passField = new PasswordField();
        nameField = new TextField();

        loadSavedCredentials();

        Label lHost = new Label(lang.get("settings.credentials.host"));
        Label lPort = new Label(lang.get("settings.credentials.port"));
        Label lUser = new Label(lang.get("settings.credentials.user"));
        Label lPass = new Label(lang.get("settings.credentials.pass"));
        Label lName = new Label(lang.get("settings.credentials.name"));

        credentialsLabels = new Label[]{ lHost, lPort, lUser, lPass, lName };
        TextInputControl[] inputs = { hostField, portField, userField, passField, nameField };

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        grid.add(lHost, 0, 0); grid.add(hostField, 1, 0);
        grid.add(lPort, 0, 1); grid.add(portField, 1, 1);
        grid.add(lUser, 0, 2); grid.add(userField, 1, 2);
        grid.add(lPass, 0, 3); grid.add(passField, 1, 3);
        grid.add(lName, 0, 4); grid.add(nameField, 1, 4);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setMinWidth(120);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        statusLabel = new Label();
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);

        saveBtn = new Button(lang.get("settings.credentials.save"));
        saveBtn.setPrefHeight(40);
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setStyle(theme.primaryButtonStyle());

        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(theme.primaryButtonHoverStyle()));
        saveBtn.setOnMouseExited(e  -> saveBtn.setStyle(theme.primaryButtonStyle()));

        saveBtn.setOnAction(e -> handleSaveCredentials(lang, theme));

        VBox card = new VBox(12, credentialsSectionTitle, grid, statusLabel, saveBtn);
        card.setPadding(new Insets(14));

        theme.addListener(dark -> {
            card.setStyle(theme.cardStyle());
            credentialsSectionTitle.setStyle("-fx-text-fill: " + theme.text() + ";");
            for (Label l : credentialsLabels) {
                l.setStyle("-fx-text-fill: " + theme.muted() + ";");
            }
            for (TextInputControl input : inputs) {
                input.setStyle(theme.inputStyle());
            }
            saveBtn.setStyle(theme.primaryButtonStyle());
        });

        return card;
    }

    private void handleSaveCredentials(LanguageManager lang, ThemeManager theme) {
        
        saveBtn.setDisable(true);
        saveBtn.setText(lang.get("settings.credentials.saving"));
        statusLabel.setVisible(false);

        new Thread(() -> {
            try {
                String host = hostField.getText().trim();
                String portStr = portField.getText().trim();
                String user = userField.getText().trim();
                String pass = passField.getText();
                String dbName = nameField.getText().trim();

                if (host.isEmpty() || portStr.isEmpty() || user.isEmpty() || 
                    pass.isEmpty() || dbName.isEmpty()) {
                    
                    Platform.runLater(() -> {
                        showStatus(lang.get("settings.credentials.error.empty"), true, theme);
                        resetSaveButton(lang);
                    });
                    return;
                }

                int port;
                try {
                    port = Integer.parseInt(portStr);
                    if (port < 1 || port > 65535) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    Platform.runLater(() -> {
                        showStatus(lang.get("settings.credentials.error.port"), true, theme);
                        resetSaveButton(lang);
                    });
                    return;
                }

                DatabaseCredentialsDto credentials = new DatabaseCredentialsDto(
                    host, port, user, pass, dbName
                );

                AuthService authService = AuthService.getInstance();
                LoginResponse loginResponse = authService.login(credentials);

                CredentialsManager credManager = CredentialsManager.getInstance();
                credManager.saveCredentials(credentials);

                Platform.runLater(() -> {
                    showStatus(lang.get("settings.credentials.success"), false, theme);
                    resetSaveButton(lang);
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    String errorMsg = ex.getMessage();
                    if (errorMsg != null && errorMsg.contains("Invalid database credentials")) {
                        showStatus(lang.get("settings.credentials.error.invalid"), true, theme);
                    } else {
                        showStatus(lang.get("settings.credentials.error.connection"), true, theme);
                    }
                    resetSaveButton(lang);
                });
            }
        }).start();
    }

    private void showStatus(String message, boolean isError, ThemeManager theme) {
        statusLabel.setText(message);
        
        if (isError) {
            statusLabel.setStyle(
                "-fx-text-fill: #991b1b;" +
                "-fx-background-color: #fef2f2;" +
                "-fx-border-color: #fca5a5;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 12;"
            );
        } else {
            statusLabel.setStyle(
                "-fx-text-fill: #15803d;" +
                "-fx-background-color: #f0fdf4;" +
                "-fx-border-color: #86efac;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 12;"
            );
        }
        
        statusLabel.setVisible(true);
    }

    private void resetSaveButton(LanguageManager lang) {
        saveBtn.setDisable(false);
        saveBtn.setText(lang.get("settings.credentials.save"));
    }

    private void loadSavedCredentials() {
        try {
            CredentialsManager credManager = CredentialsManager.getInstance();
            DatabaseCredentialsDto savedCreds = credManager.loadCredentials();
            
            if (savedCreds != null) {
                hostField.setText(savedCreds.getHost());
                portField.setText(String.valueOf(savedCreds.getPort()));
                userField.setText(savedCreds.getUsername());
                passField.setText(savedCreds.getPassword());
                nameField.setText(savedCreds.getDatabase());
            }
        } catch (Exception e) {
            System.err.println("Failed to load saved credentials: " + e.getMessage());
        }
    }

    private VBox buildInterfaceSection(ThemeManager theme, LanguageManager lang) {

        interfaceSectionTitle = new Label(lang.get("settings.interface.title"));
        interfaceSectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        languageLabel = new Label(lang.get("settings.interface.language"));
        themeLabel    = new Label(lang.get("settings.interface.theme"));

        languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll(
            lang.get("settings.interface.language.spanish"),
            lang.get("settings.interface.language.english")
        );
        
        if ("es".equals(lang.getCurrentLanguage())) {
            languageCombo.setValue(lang.get("settings.interface.language.spanish"));
        } else {
            languageCombo.setValue(lang.get("settings.interface.language.english"));
        }

        themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll(
            lang.get("settings.interface.theme.light"),
            lang.get("settings.interface.theme.dark")
        );
        themeCombo.setValue(theme.isDark() ? 
            lang.get("settings.interface.theme.dark") : 
            lang.get("settings.interface.theme.light")
        );

        applyComboBoxStyle(languageCombo, theme);
        applyComboBoxStyle(themeCombo, theme);

        theme.addListener(dark -> {
            themeCombo.setValue(dark ? 
                lang.get("settings.interface.theme.dark") : 
                lang.get("settings.interface.theme.light"));
            
            applyComboBoxStyle(languageCombo, theme);
            applyComboBoxStyle(themeCombo, theme);
        });

        themeCombo.setOnAction(e -> {
            String selected = themeCombo.getValue();
            String darkOption = lang.get("settings.interface.theme.dark");
            theme.setDark(darkOption.equals(selected));
        });

        languageCombo.setOnAction(e -> {
            String selected = languageCombo.getValue();
            String spanishOption = lang.get("settings.interface.language.spanish");
            lang.setLanguage(spanishOption.equals(selected) ? "es" : "en");
        });

        GridPane interfaceGrid = new GridPane();
        interfaceGrid.setHgap(12);
        interfaceGrid.setVgap(10);

        interfaceGrid.add(languageLabel, 0, 0);
        interfaceGrid.add(languageCombo, 1, 0);
        interfaceGrid.add(themeLabel, 0, 1);
        interfaceGrid.add(themeCombo, 1, 1);

        ColumnConstraints ic0 = new ColumnConstraints();
        ic0.setMinWidth(120);
        ColumnConstraints ic1 = new ColumnConstraints();
        ic1.setHgrow(Priority.ALWAYS);
        interfaceGrid.getColumnConstraints().addAll(ic0, ic1);

        VBox interfaceCard = new VBox(12, interfaceSectionTitle, interfaceGrid);
        interfaceCard.setPadding(new Insets(14));

        theme.addListener(dark -> {
            interfaceCard.setStyle(theme.cardStyle());
            interfaceSectionTitle.setStyle("-fx-text-fill: " + theme.text() + ";");
            languageLabel.setStyle("-fx-text-fill: " + theme.muted() + ";");
            themeLabel.setStyle("-fx-text-fill: " + theme.muted() + ";");
        });

        return interfaceCard;
    }

    private void applyComboBoxStyle(ComboBox<String> combo, ThemeManager theme) {
        if (theme.isDark()) {
            combo.setStyle(
                "-fx-background-color: " + theme.input() + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + theme.border() + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-font-size: 14;"
            );
            
            combo.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-text-fill: " + theme.text() + ";" +
                            "-fx-background-color: transparent;" +
                            "-fx-padding: 6 12;"
                        );
                    }
                }
            });
            
            combo.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-text-fill: " + theme.text() + ";" +
                            "-fx-background-color: transparent;" +
                            "-fx-padding: 10 12;"
                        );
                    }
                }
                
                @Override
                public void updateSelected(boolean selected) {
                    super.updateSelected(selected);
                    if (selected && !isEmpty()) {
                        setStyle(
                            "-fx-text-fill: #0f1419;" +
                            "-fx-background-color: " + theme.accent() + ";" +
                            "-fx-padding: 10 12;"
                        );
                    } else if (!isEmpty()) {
                        setStyle(
                            "-fx-text-fill: " + theme.text() + ";" +
                            "-fx-background-color: transparent;" +
                            "-fx-padding: 10 12;"
                        );
                    }
                }
            });
            
        } else {
            combo.setStyle(
                "-fx-background-color: " + theme.input() + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " + theme.border() + ";" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 8;" +
                "-fx-font-size: 14;"
            );
            
            combo.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-text-fill: " + theme.text() + ";" +
                            "-fx-background-color: transparent;" +
                            "-fx-padding: 6 12;"
                        );
                    }
                }
            });
            
            combo.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle(
                            "-fx-text-fill: " + theme.text() + ";" +
                            "-fx-background-color: transparent;" +
                            "-fx-padding: 10 12;"
                        );
                    }
                }
                
                @Override
                public void updateSelected(boolean selected) {
                    super.updateSelected(selected);
                    if (selected && !isEmpty()) {
                        setStyle(
                            "-fx-text-fill: white;" +
                            "-fx-background-color: " + theme.accent() + ";" +
                            "-fx-padding: 10 12;"
                        );
                    } else if (!isEmpty()) {
                        setStyle(
                            "-fx-text-fill: " + theme.text() + ";" +
                            "-fx-background-color: transparent;" +
                            "-fx-padding: 10 12;"
                        );
                    }
                }
            });
        }
    }

    private void updateTexts(LanguageManager lang) {
        title.setText(lang.get("settings.title"));
        subtitle.setText(lang.get("settings.subtitle"));
        
        credentialsSectionTitle.setText(lang.get("settings.credentials.title"));
        
        credentialsLabels[0].setText(lang.get("settings.credentials.host"));
        credentialsLabels[1].setText(lang.get("settings.credentials.port"));
        credentialsLabels[2].setText(lang.get("settings.credentials.user"));
        credentialsLabels[3].setText(lang.get("settings.credentials.pass"));
        credentialsLabels[4].setText(lang.get("settings.credentials.name"));
        
        saveBtn.setText(lang.get("settings.credentials.save"));
        
        interfaceSectionTitle.setText(lang.get("settings.interface.title"));
        languageLabel.setText(lang.get("settings.interface.language"));
        themeLabel.setText(lang.get("settings.interface.theme"));
        
        String currentLang = languageCombo.getValue();
        languageCombo.getItems().clear();
        languageCombo.getItems().addAll(
            lang.get("settings.interface.language.spanish"),
            lang.get("settings.interface.language.english")
        );
        if ("es".equals(lang.getCurrentLanguage())) {
            languageCombo.setValue(lang.get("settings.interface.language.spanish"));
        } else {
            languageCombo.setValue(lang.get("settings.interface.language.english"));
        }
        
        String currentTheme = themeCombo.getValue();
        themeCombo.getItems().clear();
        themeCombo.getItems().addAll(
            lang.get("settings.interface.theme.light"),
            lang.get("settings.interface.theme.dark")
        );
        ThemeManager theme = ThemeManager.getInstance();
        themeCombo.setValue(theme.isDark() ? 
            lang.get("settings.interface.theme.dark") : 
            lang.get("settings.interface.theme.light")
        );
        
        applyComboBoxStyle(languageCombo, theme);
        applyComboBoxStyle(themeCombo, theme);
    }
}