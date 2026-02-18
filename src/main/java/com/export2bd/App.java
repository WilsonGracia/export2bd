package com.export2bd;

import com.export2bd.ui.MainView;
import com.export2bd.ui.SplashView;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
       
        try {
            Image icon = new Image(
                getClass().getResourceAsStream("/images/File_Manager_Windows-Logo.wine_.png")
            );
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        //  Show splash screen first
        showSplashScreen();
        
        stage.setTitle("Export2BD");
        stage.show();
    }

    private void showSplashScreen() {
        SplashView splash = new SplashView(this::showMainView);
        primaryStage.setScene(splash.build());
        primaryStage.setResizable(false);
    }

    private void showMainView() {
        MainView main = new MainView();
        primaryStage.setScene(main.build());
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}