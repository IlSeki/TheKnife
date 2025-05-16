package com.example.theknife;
/**
 * @author [Samuele Secchi, 761031, Sede CO]
 * @author [Flavio Marin, 759910, Sede CO]
 * @author [Matilde Lecchi, 759875, Sede CO]
 * @author [Davide Caccia, 760742, Sede CO]
 * @version 1.0
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        // Carica il CSS dal classpath (il file ora si trova in src/main/resources/data/stile.css)
        scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());
        stage.setTitle("TheKnife");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
