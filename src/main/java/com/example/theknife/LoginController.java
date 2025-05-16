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
     * Questo metodo viene invocato quando l'utente preme il bottone "Accedi senza login".
     * Carica il file lista.fxml e sostituisce la scena corrente.
     *
     * @param event l'evento generato dalla pressione del bottone
     */
    @FXML
    private void handleSkipLogin(ActionEvent event) {
        try {
            // Carica il file FXML con la vista successiva
            FXMLLoader loader = new FXMLLoader(getClass().getResource("lista.fxml"));
            Parent root = loader.load();

            // Ottieni lo stage corrente dall'evento (dal source del bottone)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Imposta una nuova scena con il layout caricato
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
