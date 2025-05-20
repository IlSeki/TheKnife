package com.example.theknife;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Node;

/**
 * La classe {@code LoginController} gestisce le interazioni dell'interfaccia di login.
 * In particolare, implementa il metodo per l'accesso diretto all'applicazione senza effettuare il login.
 *
 * <p>
 * Quando viene premuto il pulsante "Accedi senza login", il metodo {@link #handleSkipLogin(ActionEvent)}
 * carica il file FXML "lista.fxml", che rappresenta la schermata dei ristoranti. Per garantire che anche
 * questa schermata si adatti alle dimensioni del monitor, il controller calcola l'80% della larghezza ed
 * dell'altezza disponibili del monitor primario e crea la scena con queste dimensioni. Questa tecnica
 * è identica a quella utilizzata nel metodo {@link com.example.theknife.Main#start(Stage)}.
 * </p>
 *
 * <p>
 * Se la schermata dei ristoranti non viene caricata correttamente, è probabile che il problema derivi
 * dal fatto che in assenza di una specifica impostazione delle dimensioni dinamiche, la scena predefinita
 * venga creata con dimensioni minime o non ottimali. Con l'aggiornamento mostrato, la scena viene creata
 * specificando larghezza e altezza basate sullo schermo, garantendo così un'esperienza utente migliore.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class LoginController {

    /**
     * Metodo invocato alla pressione del pulsante "Accedi senza login".
     * <p>
     * Questo metodo esegue le seguenti operazioni:
     * <ul>
     *   <li>Carica il file FXML "lista.fxml", che definisce l'interfaccia della schermata dei ristoranti;</li>
     *   <li>Calcola le dimensioni dinamiche della scena, ottenendo l'80% della larghezza e dell'altezza
     *       del monitor primario tramite {@link Screen} e {@link Rectangle2D};</li>
     *   <li>Crea la scena con le dimensioni calcolate e applica il foglio di stile CSS presente in
     *       "/data/stile.css";</li>
     *   <li>Imposta la nuova scena nello stage corrente e la visualizza.</li>
     * </ul>
     * </p>
     *
     * @param event L'evento generato dal clic sul pulsante.
     */
    @FXML
    private void handleSkipLogin(ActionEvent event) {
        try {
            // Carica il file FXML "lista.fxml" che rappresenta la schermata dei ristoranti
            FXMLLoader loader = new FXMLLoader(getClass().getResource("lista.fxml"));
            Parent root = loader.load();

            // Calcola le dimensioni disponibili del monitor primario
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double width = screenBounds.getWidth() * 0.8;   // utilizza l'80% della larghezza disponibile
            double height = screenBounds.getHeight() * 0.8; // utilizza l'80% dell'altezza disponibile

            // Crea la scena con le dimensioni dinamiche
            Scene scene = new Scene(root, width, height);

            // Applica il foglio di stile CSS (stile.css) dalla cartella resources/data
            scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());

            // Ottieni lo stage corrente dall'evento sul bottone
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Imposta la nuova scena nello stage e la mostra
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
