package com.example.theknife;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

public class LoginController {

    /**
     * Metodo invocato alla pressione del bottone "Accedi senza login".
     * Carica il file lista.fxml e lo imposta nella scena corrente.
     *
     * @param event L'evento generato dal clic sul bottone.
     */
    @FXML
    private void handleSkipLogin(ActionEvent event) {
        try {
            // Carica il file FXML "lista.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("lista.fxml"));
            Parent root = loader.load();

            // Ottieni lo stage corrente dall'azione dell'evento sul bottone
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            // Aggiungi il CSS (stile.css) dalla cartella resources/data
            scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());

            // Imposta la nuova scena nello stage e mostra la finestra
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
