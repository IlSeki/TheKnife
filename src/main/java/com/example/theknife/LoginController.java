package com.example.theknife;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Controller per la gestione del login degli utenti.
 * Gestisce l'autenticazione degli utenti e il reindirizzamento alla schermata
 * appropriata in base al ruolo dell'utente.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class LoginController {

    @FXML
    private TextField campoUsername;

    @FXML
    private PasswordField campoPassword;

    private Runnable onLoginSuccess;

    /**
     * Imposta il callback da eseguire al login avvenuto con successo
     * @param callback il callback da eseguire
     */
    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    /**
     * Metodo invocato alla pressione del pulsante "Accedi".
     * Verifica le credenziali inserite dall'utente confrontandole con il file CSV.
     * In caso di successo, reindirizza l'utente all'interfaccia appropriata in base al ruolo.
     *
     * @param evento L'evento generato dal clic sul pulsante di login.
     */
    @FXML
    private void gestisciAccesso(ActionEvent evento) {
        String username = campoUsername.getText().trim();
        String password = campoPassword.getText();

        // Validazione input
        if (username.isEmpty() || password.isEmpty()) {
            mostraAvviso("Errore", "Inserisci username e password!", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Carica gli utenti dal file CSV
            List<Utente> utenti = caricaUtentiDaCSV();

            System.out.println("DEBUG: Utenti caricati: " + utenti.size());

            // Cifra la password inserita per il confronto
            String passwordCifrata = cifraPassword(password);
            System.out.println("DEBUG: Password cifrata: " + passwordCifrata);

            // Cerca l'utente con username e password corrispondenti
            Utente utenteAutenticato = null;
            for (Utente utente : utenti) {
                System.out.println("DEBUG: Confronto con utente: " + utente.getUsername() +
                        " - Password nel file: " + utente.getPasswordHash());

                if (utente.getUsername().equals(username) && utente.getPasswordHash().equals(passwordCifrata)) {
                    utenteAutenticato = utente;
                    break;
                }
            }

            if (utenteAutenticato != null) {
                // Autenticazione riuscita - salva i dati utente e reindirizza
                SessioneUtente.impostaUtenteCorrente(
                        utenteAutenticato.getNome(),
                        utenteAutenticato.getCognome(),
                        utenteAutenticato.getUsername(),
                        utenteAutenticato.getRuolo()
                );

                System.out.println("DEBUG: Utente autenticato: " + utenteAutenticato.toString());

                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }

                reindirizzaAllInterfacciaPrincipale(evento, utenteAutenticato.getRuolo());
            } else {
                // Credenziali errate
                mostraAvviso("Errore di Autenticazione", "Username o password non corretti!", Alert.AlertType.ERROR);
                campoPassword.clear(); // Pulisce il campo password per sicurezza
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostraAvviso("Errore", "Errore durante l'autenticazione: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Metodo invocato alla pressione del pulsante "Accedi senza login".
     * Permette l'accesso come utente non registrato con funzionalità limitate.
     *
     * @param evento L'evento generato dal clic sul pulsante.
     */
    @FXML
    private void gestisciAccessoSenzaLogin(ActionEvent evento) {
        try {
            // Imposta la sessione come utente non registrato
            SessioneUtente.impostaUtenteCorrente("Ospite", "", "", "ospite");
            reindirizzaAllInterfacciaPrincipale(evento, "ospite");
        } catch (Exception e) {
            e.printStackTrace();
            mostraAvviso("Errore", "Errore durante l'accesso: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Metodo invocato alla pressione del pulsante "Registrati".
     * Reindirizza l'utente alla schermata di registrazione.
     *
     * @param evento L'evento generato dal clic sul pulsante di registrazione.
     */
    @FXML
    private void gestisciRegistrazione(ActionEvent evento) {
        try {
            // Debug: verifica se il file esiste
            System.out.println("DEBUG: Tentativo di caricare registrazione.fxml");
            if (getClass().getResource("registrazione.fxml") == null) {
                System.out.println("ERROR: File registrazione.fxml non trovato nel classpath");
                mostraAvviso("Errore", "File registrazione.fxml non trovato!\n" +
                        "Verifica che il file sia presente nella directory src/main/resources/", Alert.AlertType.ERROR);
                return;
            }

            // Carica il file FXML della registrazione
            FXMLLoader caricatore = new FXMLLoader(getClass().getResource("registrazione.fxml"));
            Parent radice = caricatore.load();

            // Calcola le dimensioni della finestra
            Rectangle2D limitiSchermo = Screen.getPrimary().getVisualBounds();
            double larghezza = Math.min(700, limitiSchermo.getWidth() * 0.6);
            double altezza = Math.min(600, limitiSchermo.getHeight() * 0.8);

            // Crea la scena
            Scene scena = new Scene(radice, larghezza, altezza);

            // Applica il CSS se disponibile
            try {
                String cssPath = "/data/stile.css";
                if (getClass().getResource(cssPath) != null) {
                    scena.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                } else {
                    System.out.println("WARNING: File CSS non trovato: " + cssPath);
                }
            } catch (Exception e) {
                System.out.println("WARNING: Errore nel caricamento CSS: " + e.getMessage());
            }

            // Imposta la nuova scena
            Stage palcoscenico = (Stage) ((Node) evento.getSource()).getScene().getWindow();
            palcoscenico.setTitle("TheKnife - Registrazione");
            palcoscenico.setScene(scena);
            palcoscenico.show();

            System.out.println("DEBUG: Schermata di registrazione caricata con successo");

        } catch (IOException e) {
            System.out.println("ERROR: Impossibile caricare registrazione.fxml - " + e.getMessage());
            e.printStackTrace();
            mostraAvviso("Errore", "Impossibile caricare la schermata di registrazione.\n" +
                    "Dettagli: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Carica la lista degli utenti dal file CSV "utenti.csv".
     *
     * @return Lista degli utenti caricati dal file CSV.
     * @throws IOException Se si verifica un errore durante la lettura del file.
     */
    public List<Utente> caricaUtentiDaCSV() throws IOException {
        List<Utente> utenti = new ArrayList<>();

        // Percorso del file CSV esterno, nella cartella 'data'
        String filePath = "data/utenti.csv";
        File csvFile = new File(filePath);

        // Controlla se la cartella 'data' esiste, se no la crea
        File parentDir = csvFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("DEBUG: Cartella 'data' creata.");
        }

        // Controlla se il file utenti.csv esiste
        if (!csvFile.exists()) {
            System.err.println("File utenti.csv non trovato. Creazione di un nuovo file.");
            // Se non esiste, lo crea e aggiunge l'header
            try (FileWriter writer = new FileWriter(csvFile, StandardCharsets.UTF_8)) {
                writer.append("nome,cognome,username,passwordHash,dataNascita,luogoDomicilio,ruolo\n");
                System.out.println("DEBUG: Nuovo file utenti.csv creato con header.");
            }
            return utenti; // Restituisce una lista vuota dopo aver creato il file
        }

        // Se il file esiste, procedi con la lettura come prima
        try (FileReader reader = new FileReader(csvFile, StandardCharsets.UTF_8);
             BufferedReader lettore = new BufferedReader(reader)) {

            String riga = lettore.readLine(); // Salta l'header
            System.out.println("DEBUG: Header CSV: " + riga);

            while ((riga = lettore.readLine()) != null) {
                // ... (il tuo codice di parsing rimane invariato) ...
                if (!riga.trim().isEmpty()) {
                    String[] parti = riga.split(",");
                    if (parti.length >= 7) {
                        Utente utente = new Utente(
                                parti[0].trim(),
                                parti[1].trim(),
                                parti[2].trim(),
                                parti[3].trim(),
                                parti[4].trim(),
                                parti[5].trim(),
                                parti[6].trim()
                        );
                        utenti.add(utente);
                    } else {
                        System.out.println("DEBUG: Riga ignorata (parti insufficienti): " + riga);
                    }
                }
            }
        }

        System.out.println("DEBUG: Totale utenti caricati: " + utenti.size());
        return utenti;
    }

    /**
     * Calcola l'hash SHA-256 di una password.
     *
     * @param password La password in chiaro da cifrare.
     * @return L'hash SHA-256 della password in formato esadecimale.
     * @throws NoSuchAlgorithmException Se l'algoritmo SHA-256 non è disponibile.
     */
    private String cifraPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        StringBuilder stringaEsadecimale = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                stringaEsadecimale.append('0');
            }
            stringaEsadecimale.append(hex);
        }

        return stringaEsadecimale.toString();
    }

    /**
     * Reindirizza l'utente all'interfaccia principale appropriata in base al ruolo.
     *
     * @param evento L'evento ActionEvent per ottenere lo stage corrente.
     * @param ruolo Il ruolo dell'utente ("cliente", "ristoratore", "ospite").
     * @throws IOException Se si verifica un errore durante il caricamento dell'interfaccia.
     */
    private void reindirizzaAllInterfacciaPrincipale(ActionEvent evento, String ruolo) throws IOException {
        // Tutti gli utenti vengono portati alla schermata di ricerca
        String fileFxml = "lista.fxml";
        String titoloFinestra = "TheKnife - Ricerca Ristoranti";

        // Carica l'interfaccia
        FXMLLoader caricatore = new FXMLLoader(getClass().getResource(fileFxml));
        Parent radice = caricatore.load();

        // Calcola le dimensioni della finestra
        Rectangle2D limitiSchermo = Screen.getPrimary().getVisualBounds();
        double larghezza = Math.min(1024, limitiSchermo.getWidth() * 0.8);
        double altezza = Math.min(768, limitiSchermo.getHeight() * 0.8);

        // Crea la scena
        Scene scena = new Scene(radice, larghezza, altezza);

        // Applica il CSS se disponibile
        try {
            String cssPath = "/data/stile.css";
            if (getClass().getResource(cssPath) != null) {
                scena.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("WARNING: Errore nel caricamento CSS: " + e.getMessage());
        }

        // Imposta la nuova scena
        Stage palcoscenico = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        palcoscenico.setTitle(titoloFinestra);
        palcoscenico.setScene(scena);
        palcoscenico.show();
    }

    /**
     * Mostra un dialog di avviso all'utente.
     *
     * @param titolo Il titolo del dialog.
     * @param messaggio Il messaggio da visualizzare.
     * @param tipoAvviso Il tipo di alert (INFO, WARNING, ERROR).
     */
    private void mostraAvviso(String titolo, String messaggio, Alert.AlertType tipoAvviso) {
        Alert avviso = new Alert(tipoAvviso);
        avviso.setTitle(titolo);
        avviso.setHeaderText(null);
        avviso.setContentText(messaggio);
        avviso.showAndWait();
    }
}