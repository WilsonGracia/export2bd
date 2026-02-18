package com.export2bd.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class LoginView {

    private final Runnable onLogin;

    public LoginView(Runnable onLogin) {
        this.onLogin = onLogin;
    }

    public Scene build() {
        Label app = new Label("Export2BD");
        app.setFont(Font.font(22));

        Label hint = new Label("Ingrese cualquier usuario y contraseña");
        hint.setStyle("-fx-text-fill: #6b7280;");

        TextField user = new TextField();
        user.setPromptText("Usuario");

        PasswordField pass = new PasswordField();
        pass.setPromptText("Contraseña");

        Button loginBtn = new Button("Entrar");
        loginBtn.setDefaultButton(true);
        loginBtn.setPrefHeight(40);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> onLogin.run());

        VBox form = new VBox(10, app, hint, user, pass, loginBtn);
        form.setPadding(new Insets(24));
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(360);

        form.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;"
        );

        StackPane wrap = new StackPane(form);
        wrap.setPadding(new Insets(40));
        wrap.setStyle("-fx-background-color: #f6f7fb;");

        String inputStyle =
                "-fx-background-color: #f3f4f6;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: transparent;" +
                "-fx-padding: 10 12;";
        user.setStyle(inputStyle);
        pass.setStyle(inputStyle);

        loginBtn.setStyle(
                "-fx-background-color: #111827;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;"
        );

        return new Scene(wrap, 520, 420);
    }
}
