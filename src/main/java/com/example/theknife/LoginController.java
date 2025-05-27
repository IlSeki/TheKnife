package com.example.theknife;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * La classe {@code LoginController} gestisce le interazioni dell'interfaccia di login.
 * Implementa l'autenticazione degli utenti tramite lettura del file CSV "utenti.csv"
 * e la navigazione verso diverse interfacce in base al ruolo dell'utente.
 *
 * <p>
 * Il controller supporta due modalità di accesso:
 * <ul>
 *   <li>Accesso con credenziali: verifica username e password cifrata tramite SHA-256</li>
 *   <li>Accesso diretto senza login: accesso come utente non registrato</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @version 2.0
 * @since 2025-05-27
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Metodo invocato alla pressione del pulsante "Accedi".
     * Verifica le credenziali inserite dall'utente confrontandole con il file CSV.
     * In caso di successo, reindirizza l'utente all'interfaccia appropriata in base al ruolo.
     *
     * @param event L'evento generato dal clic sul pulsante di login.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validazione input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Errore", "Inserisci username e password!", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Carica gli utenti dal file CSV
            List<User> users = loadUsersFromCSV();

            // Cifra la password inserita per il confronto
            String hashedPassword = hashPassword(password);

            // Cerca l'utente con username e password corrispondenti
            User authenticatedUser = null;
            for (User user : users) {
                if (user.username.equals(username) && user.password.equals(hashedPassword)) {
                    authenticatedUser = user;
                    break;
                }
            }

            if (authenticatedUser != null) {
                // Autenticazione riuscita - salva i dati utente e reindirizza
                UserSession.setCurrentUser(authenticatedUser.nome, authenticatedUser.cognome,
                        authenticatedUser.username, authenticatedUser.ruolo);
                redirectToMainInterface(event, authenticatedUser.ruolo);
            } else {
                // Credenziali errate
                showAlert("Errore di Autenticazione", "Username o password non corretti!", Alert.AlertType.ERROR);
                passwordField.clear(); // Pulisce il campo password per sicurezza
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore", "Errore durante l'autenticazione: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Metodo invocato alla pressione del pulsante "Accedi senza login".
     * Permette l'accesso come utente non registrato con funzionalità limitate.
     *
     * @param event L'evento generato dal clic sul pulsante.
     */
    @FXML
    private void handleSkipLogin(ActionEvent event) {
        try {
            // Imposta la sessione come utente non registrato
            UserSession.setCurrentUser("Ospite", "", "", "ospite");
            redirectToMainInterface(event, "ospite");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore", "Errore durante l'accesso: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Carica la lista degli utenti dal file CSV "utenti.csv".
     *
     * @return Lista degli utenti caricati dal file CSV.
     * @throws IOException Se si verifica un errore durante la lettura del file.
     */
    private List<User> loadUsersFromCSV() throws IOException {
        List<User> users = new ArrayList<>();

        try (InputStream is = getClass().getResourceAsStream("/data/utenti.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                throw new IOException("File utenti.csv non trovato in /data/");
            }

            String line = reader.readLine(); // Salta l'header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    users.add(new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));
                }
            }
        }

        return users;
    }

    /**
     * Calcola l'hash SHA-256 di una password.
     *
     * @param password La password in chiaro da cifrare.
     * @return L'hash SHA-256 della password in formato esadecimale.
     * @throws NoSuchAlgorithmException Se l'algoritmo SHA-256 non è disponibile.
     */
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * Reindirizza l'utente all'interfaccia principale appropriata in base al ruolo.
     *
     * @param event L'evento ActionEvent per ottenere lo stage corrente.
     * @param ruolo Il ruolo dell'utente ("cliente", "ristoratore", "ospite").
     * @throws IOException Se si verifica un errore durante il caricamento dell'interfaccia.
     */
    private void redirectToMainInterface(ActionEvent event, String ruolo) throws IOException {
        String fxmlFile;
        String windowTitle;

        // Determina quale interfaccia caricare in base al ruolo
        switch (ruolo.toLowerCase()) {
            case "cliente":
                fxmlFile = "lista.fxml"; // Interfaccia per i clienti
                windowTitle = "TheKnife - Area Cliente";
                break;
            case "ristoratore":
                fxmlFile = "ristoratore.fxml"; // Interfaccia per i ristoratori
                windowTitle = "TheKnife - Area Ristoratore";
                break;
            case "ospite":
            default:
                fxmlFile = "lista.fxml"; // Interfaccia per utenti non registrati
                windowTitle = "TheKnife - Ospite";
                break;
        }

        // Carica il file FXML appropriato
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Calcola le dimensioni dinamiche dello schermo
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth() * 0.8;
        double height = screenBounds.getHeight() * 0.8;

        // Crea la scena con le dimensioni calcolate
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());

        // Ottieni lo stage corrente e imposta la nuova scena
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(windowTitle);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Mostra un dialog di avviso all'utente.
     *
     * @param title Il titolo del dialog.
     * @param message Il messaggio da visualizzare.
     * @param alertType Il tipo di alert (INFO, WARNING, ERROR).
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}