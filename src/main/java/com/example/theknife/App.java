package com.example.theknife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La classe {@code Main} è il punto di ingresso principale per l'applicazione JavaFX "TheKnife".
 * Essa estende {@link javafx.application.Application} e si occupa di:
 * <ul>
 *   <li>Caricare il file FXML "login.fxml" che definisce l'interfaccia utente della schermata di login;</li>
 *   <li>Creare una scena che si adatta automaticamente alle dimensioni dello schermo;</li>
 *   <li>Supportare modalità a schermo intero e ridimensionamento dinamico;</li>
 *   <li>Applicare il foglio di stile CSS per la personalizzazione dell'interfaccia;</li>
 *   <li>Impostare e visualizzare lo {@link Stage} principale.</li>
 * </ul>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class App extends Application {

    private static final double MIN_WIDTH = 800;
    private static final double MIN_HEIGHT = 600;

    /**
     * Avvia l'applicazione JavaFX, inizializzando l'interfaccia utente.
     *
     * <p>
     * Le operazioni eseguite in questo metodo sono:
     * <ul>
     *   <li>Utilizzare la classe {@link javafx.stage.Screen} per ottenere le dimensioni del monitor primario;</li>
     *   <li>Configurare la finestra per adattarsi a diverse risoluzioni e supportare schermo intero;</li>
     *   <li>Caricare il file FXML "login.fxml" tramite {@link FXMLLoader};</li>
     *   <li>Creare una {@link Scene} con dimensioni responsive;</li>
     *   <li>Applicare il foglio di stile CSS e configurare scorciatoie da tastiera;</li>
     *   <li>Impostare proprietà di ridimensionamento e visualizzare la finestra.</li>
     * </ul>
     * </p>
     *
     * @param stage lo {@link Stage} primario fornito dal framework JavaFX.
     * @throws IOException se si verifica un errore durante il caricamento delle risorse FXML o CSS.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Ottieni le dimensioni del monitor primario
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Calcola le dimensioni iniziali (90% dello schermo per una migliore adattabilità)
        double initialWidth = screenBounds.getWidth() * 0.9;
        double initialHeight = screenBounds.getHeight() * 0.9;

        // Assicurati che le dimensioni non siano inferiori ai valori minimi
        double windowWidth = Math.max(initialWidth, MIN_WIDTH);
        double windowHeight = Math.max(initialHeight, MIN_HEIGHT);

        System.out.println("Risoluzione schermo: " + screenBounds.getWidth() + "x" + screenBounds.getHeight());
        System.out.println("Dimensioni finestra: " + windowWidth + "x" + windowHeight);

        // Carica il file FXML per la schermata di login
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Applica il foglio di stile CSS dal classpath
        scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());

        // Configura lo stage
        stage.setTitle("TheKnife");
        stage.setScene(scene);

        // Imposta dimensioni iniziali
        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);

        // Imposta dimensioni minime per evitare finestre troppo piccole
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        // Imposta dimensioni massime basate sullo schermo
        stage.setMaxWidth(screenBounds.getWidth());
        stage.setMaxHeight(screenBounds.getHeight());

        // Centra la finestra sullo schermo
        stage.setX((screenBounds.getWidth() - windowWidth) / 2);
        stage.setY((screenBounds.getHeight() - windowHeight) / 2);

        // Permetti il ridimensionamento
        stage.setResizable(true);

        // Configura il supporto per schermo intero
        setupFullScreenSupport(stage, scene);

        // Mostra la finestra
        stage.show();

        // Opzionale: avvia direttamente in modalità massimizzata
        // stage.setMaximized(true);

        // Opzionale: avvia direttamente a schermo intero
        // stage.setFullScreen(true);
    }

    /**
     * Configura il supporto per la modalità schermo intero.
     *
     * @param stage lo stage principale
     * @param scene la scena dell'applicazione
     */
    private void setupFullScreenSupport(Stage stage, Scene scene) {
        // Scorciatoia F11 per attivare/disattivare schermo intero
        KeyCombination fullScreenKey = new KeyCodeCombination(KeyCode.F11);
        scene.setOnKeyPressed(event -> {
            if (fullScreenKey.match(event)) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });

        // Scorciatoia ESC per uscire dallo schermo intero
        KeyCombination escapeKey = new KeyCodeCombination(KeyCode.ESCAPE);
        scene.setOnKeyPressed(event -> {
            if (escapeKey.match(event) && stage.isFullScreen()) {
                stage.setFullScreen(false);
            }
        });

        // Personalizza il messaggio di uscita dallo schermo intero
        stage.setFullScreenExitHint("Premi ESC o F11 per uscire dalla modalità schermo intero");

        // Listener per cambiamenti di modalità schermo intero
        stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Modalità schermo intero: " + (newValue ? "ATTIVA" : "DISATTIVA"));
        });
    }

    /**
     * Punto di ingresso principale dell'applicazione.
     *
     * <p>
     * Il metodo {@code main} invoca il metodo {@link #launch(String...)} che avvia il ciclo di vita
     * dell'applicazione JavaFX.
     * </p>
     *
     * @param args gli argomenti della riga di comando.
     */
    public static void main(String[] args) {
        launch();
    }
}