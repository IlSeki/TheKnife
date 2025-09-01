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
 * Gestisce l'autenticazione degli utenti e il reindirizzamento alla schermata principale.
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class LoginController {
    private static final String USERS_FILE = "data/utenti.csv";
    private static final String CSS_PATH = "/data/stile.css";

    @FXML private TextField campoUsername;
    @FXML private PasswordField campoPassword;

    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    /**
     * Gestisce il processo di login verificando username e password.
     *
     * @param evento evento generato dal click sul pulsante di login
     */
    @FXML
    private void gestisciAccesso(ActionEvent evento) {
        String username = campoUsername.getText().trim();
        String password = campoPassword.getText();

        if (!validaInput(username, password)) {
            return;
        }

        try {
            Utente utenteAutenticato = autenticaUtente(username, password);

            if (utenteAutenticato != null) {
                SessioneUtente.impostaUtenteCorrente(
                        utenteAutenticato.getNome(),
                        utenteAutenticato.getCognome(),
                        utenteAutenticato.getUsername(),
                        utenteAutenticato.getRuolo()
                );

                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }

                reindirizzaAllInterfacciaPrincipale(evento);
            } else {
                mostraAvviso("Errore di Autenticazione",
                        "Username o password non corretti!", Alert.AlertType.ERROR);
                campoPassword.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostraAvviso("Errore", "Errore durante l'autenticazione: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Gestisce l’accesso come ospite senza credenziali.
     *
     * @param evento evento generato dal click sul pulsante di accesso ospite
     */
    @FXML
    private void gestisciAccessoSenzaLogin(ActionEvent evento) {
        try {
            SessioneUtente.impostaUtenteCorrente("Ospite", "", "", "ospite");
            reindirizzaAllInterfacciaPrincipale(evento);
        } catch (Exception e) {
            e.printStackTrace();
            mostraAvviso("Errore", "Errore durante l'accesso: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Gestisce la navigazione alla schermata di registrazione.
     *
     * @param evento evento generato dal click sul pulsante di registrazione
     */
    @FXML
    private void gestisciRegistrazione(ActionEvent evento) {
        try {
            caricaScena(evento, "registrazione.fxml", "TheKnife - Registrazione", 700, 600);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della registrazione: " + e.getMessage());
            e.printStackTrace();
            mostraAvviso("Errore", "Impossibile caricare la schermata di registrazione.",
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Valida i dati di input dell’utente.
     *
     * @param username nome utente inserito
     * @param password password inserita
     * @return {@code true} se i campi sono validi, {@code false} altrimenti
     */
    private boolean validaInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            mostraAvviso("Errore", "Inserisci username e password!", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /**
     * Autentica un utente verificando le credenziali rispetto al file CSV.
     *
     * @param username nome utente
     * @param password password in chiaro
     * @return oggetto {@link Utente} se autenticato, altrimenti {@code null}
     * @throws Exception se si verifica un errore di lettura o cifratura
     */
    private Utente autenticaUtente(String username, String password) throws Exception {
        List<Utente> utenti = caricaUtentiDaCSV();
        String passwordCifrata = cifraPassword(password);

        return utenti.stream()
                .filter(utente -> utente.getUsername().equals(username) &&
                        utente.getPasswordHash().equals(passwordCifrata))
                .findFirst()
                .orElse(null);
    }

    /**
     * Carica la lista degli utenti dal file CSV.
     *
     * @return lista di {@link Utente}
     * @throws IOException se si verifica un errore di lettura del file
     */
    private List<Utente> caricaUtentiDaCSV() throws IOException {
        List<Utente> utenti = new ArrayList<>();
        File csvFile = new File(USERS_FILE);

        if (!csvFile.exists()) {
            createUsersFile(csvFile);
            return utenti;
        }

        try (BufferedReader lettore = new BufferedReader(
                new FileReader(csvFile, StandardCharsets.UTF_8))) {

            String riga = lettore.readLine(); // Skip header
            while ((riga = lettore.readLine()) != null) {
                if (!riga.trim().isEmpty()) {
                    Utente utente = parseUserFromCsv(riga);
                    if (utente != null) {
                        utenti.add(utente);
                    }
                }
            }
        }
        return utenti;
    }

    /**
     * Crea il file utenti con l’header se non esiste.
     *
     * @param file file CSV da creare
     * @throws IOException se si verifica un errore in scrittura
     */
    private void createUsersFile(File file) throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.append("nome,cognome,username,passwordHash,dataNascita,luogoDomicilio,ruolo\n");
        }
    }

    /**
     * Converte una riga CSV in un oggetto {@link Utente}.
     *
     * @param riga stringa CSV contenente i dati di un utente
     * @return oggetto {@link Utente} oppure {@code null} se i dati non sono validi
     */
    private Utente parseUserFromCsv(String riga) {
        String[] parti = riga.split(",");
        if (parti.length >= 7) {
            return new Utente(
                    parti[0].trim(), parti[1].trim(), parti[2].trim(),
                    parti[3].trim(), parti[4].trim(), parti[5].trim(), parti[6].trim()
            );
        }
        return null;
    }

    /**
     * Cifra una password in SHA-256.
     *
     * @param password password in chiaro
     * @return hash SHA-256 della password
     * @throws NoSuchAlgorithmException se l’algoritmo non è disponibile
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
     * Reindirizza l’utente all’interfaccia principale.
     *
     * @param evento evento che ha generato il cambio scena
     * @throws IOException se si verifica un errore nel caricamento della scena
     */
    private void reindirizzaAllInterfacciaPrincipale(ActionEvent evento) throws IOException {
        caricaScena(evento, "lista.fxml", "TheKnife - Ricerca Ristoranti", 1024, 768);
    }


    /**
     * Metodo helper per caricare una nuova scena.
     *
     * @param evento evento che ha generato il cambio scena
     * @param fxml file FXML da caricare
     * @param titolo titolo della finestra
     * @param defaultWidth larghezza di default
     * @param defaultHeight altezza di default
     * @throws IOException se si verifica un errore di caricamento
     */
    private void caricaScena(ActionEvent evento, String fxml, String titolo,
                             int defaultWidth, int defaultHeight) throws IOException {

        FXMLLoader caricatore = new FXMLLoader(getClass().getResource(fxml));
        Parent radice = caricatore.load();

        Rectangle2D limitiSchermo = Screen.getPrimary().getVisualBounds();
        double larghezza = Math.min(defaultWidth, limitiSchermo.getWidth() * 0.8);
        double altezza = Math.min(defaultHeight, limitiSchermo.getHeight() * 0.8);

        Scene scena = new Scene(radice, larghezza, altezza);

        // Applica CSS se disponibile
        try {
            if (getClass().getResource(CSS_PATH) != null) {
                scena.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("WARNING: Errore nel caricamento CSS: " + e.getMessage());
        }

        Stage palcoscenico = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        palcoscenico.setScene(scena);
        palcoscenico.show();
    }

    /**
     * Mostra un dialogo di avviso all’utente.
     *
     * @param titolo titolo della finestra di dialogo
     * @param messaggio testo del messaggio da mostrare
     * @param tipoAvviso tipo di {@link Alert}
     */
    private void mostraAvviso(String titolo, String messaggio, Alert.AlertType tipoAvviso) {
        Alert avviso = new Alert(tipoAvviso);
        avviso.setTitle(titolo);
        avviso.setHeaderText(null);
        avviso.setContentText(messaggio);
        avviso.showAndWait();
    }
}