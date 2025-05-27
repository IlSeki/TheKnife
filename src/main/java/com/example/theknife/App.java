package com.example.theknife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * La classe {@code Main} è il punto di ingresso principale per l'applicazione JavaFX "TheKnife".
 * Essa estende {@link javafx.application.Application} e si occupa di:
 * <ul>
 *   <li>Caricare il file FXML "login.fxml" che definisce l'interfaccia utente della schermata di login;</li>
 *   <li>Creare una scena che si adatta automaticamente alle dimensioni dello schermo, calcolando
 *       l'80% delle dimensioni visibili del monitor primario;</li>
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

    /**
     * Avvia l'applicazione JavaFX, inizializzando l'interfaccia utente.
     *
     * <p>
     * Le operazioni eseguite in questo metodo sono:
     * <ul>
     *   <li>Utilizzare la classe {@link javafx.stage.Screen} per ottenere le dimensioni visibili dello schermo
     *       e calcolare la larghezza e l'altezza della scena (80% delle dimensioni disponibili).</li>
     *   <li>Caricare il file FXML "login.fxml" tramite {@link FXMLLoader}, che definisce la schermata di login;</li>
     *   <li>Creare una nuova {@link Scene} con le dimensioni calcolate;</li>
     *   <li>Applicare il foglio di stile CSS primo dal percorso "/data/stile.css";</li>
     *   <li>Impostare il titolo dello stage su "TheKnife" e rendere visibile la finestra principale.</li>
     * </ul>
     * </p>
     *
     * @param stage lo {@link Stage} primario fornito dal framework JavaFX.
     * @throws IOException se si verifica un errore durante il caricamento delle risorse FXML o CSS.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Calcola le dimensioni disponibili del monitor primario
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth() * 0.8;   // usa l'80% della larghezza disponibile
        double height = screenBounds.getHeight() * 0.8; // usa l'80% dell'altezza disponibile
        System.out.println(getClass().getResource("/loghi/Astolfo.png"));

        // Carica il file FXML per la schermata di login
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("recensioni.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), width, height);

        // Applica il foglio di stile CSS dal classpath
        scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());

        stage.setTitle("TheKnife");
        stage.setScene(scene);
        stage.show();
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
