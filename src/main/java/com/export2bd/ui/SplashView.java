package com.export2bd.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class SplashView {

    private final Runnable onComplete;

    public SplashView(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    public Scene build() {
        
        ImageView logo = new ImageView();
        try {
            Image logoImage = new Image(
                getClass().getResourceAsStream("/images/File_Manager_Windows-Logo.wine_.png")
            );
            logo.setImage(logoImage);
            logo.setFitWidth(200);
            logo.setFitHeight(200);
            logo.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }

        
        Label appName = new Label("Export2BD");
        appName.setFont(Font.font("System", FontWeight.BOLD, 32));
        appName.setStyle("-fx-text-fill: #111827;");

      
        Label loading = new Label("Cargando...");
        loading.setFont(Font.font(14));
        loading.setStyle("-fx-text-fill: #6b7280;");

        
        VBox content = new VBox(20, logo, appName, loading);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        StackPane root = new StackPane(content);
        root.setStyle("-fx-background-color: #f6f7fb;");

        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

         
        fadeIn.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), content);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(1.5));
            fadeOut.setOnFinished(ev -> onComplete.run());
            fadeOut.play();
        });

        return new Scene(root, 520, 420);
    }
}