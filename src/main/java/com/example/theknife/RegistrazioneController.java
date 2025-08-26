package com.example.theknife;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * Controller per la gestione della registrazione di nuovi utenti.
 * <p>
 * Permette l'inserimento dei dati personali e la scelta del ruolo (cliente o ristoratore),
 * valida tutti i campi e salva l'utente nel file CSV.
 * </p>
 * <p>
 * Gestisce inoltre il passaggio alla schermata di login dopo la registrazione.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RegistrazioneController {

    @FXML
    private TextField campoNome;

    @FXML
    private TextField campoCognome;

    @FXML
    private TextField campoUsername;

    @FXML
    private PasswordField campoPassword;

    @FXML
    private PasswordField campoConfermaPassword;

    @FXML
    private DatePicker campoDataNascita;

    @FXML
    private TextField campoLuogoDomicilio;

    @FXML
    private ComboBox<String> comboRuolo;

    private Runnable onUserRegistered;

    /**
     * Inizializza il controller impostando i valori della ComboBox per il ruolo.
     */
    @FXML
    private void initialize() {
        // Popola la ComboBox con i ruoli disponibili
        comboRuolo.getItems().addAll("cliente", "ristoratore");
        comboRuolo.setValue("cliente"); // Valore di default
    }

    /**
     * Gestisce la registrazione di un nuovo utente.
     * <p>
     * Valida tutti i campi obbligatori, verifica la disponibilità dell'username,
     * cifra la password e salva l'utente nel file CSV. Infine, ritorna al login.
     * </p>
     *
     * @param evento L'evento generato dal clic sul pulsante di registrazione.
     */
    @FXML
    private void gestisciRegistrazione(ActionEvent evento) {
        try {
            // Validazione dei campi
            if (!validaCampi()) {
                return;
            }

            String nome = campoNome.getText().trim();
            String cognome = campoCognome.getText().trim();
            String username = campoUsername.getText().trim();
            String password = campoPassword.getText();
            LocalDate dataNascita = campoDataNascita.getValue();
            String luogoDomicilio = campoLuogoDomicilio.getText().trim();
            String ruolo = comboRuolo.getValue();

            // Verifica che l'username non esista già
            if (verificaUsernameEsistente(username)) {
                mostraAvviso("Errore", "L'username '" + username + "' è già in uso. Scegli un altro username.",
                        Alert.AlertType.WARNING);
                return;
            }

            // Cifra la password
            String passwordCifrata = cifraPassword(password);

            // Crea il nuovo utente
            Utente nuovoUtente = new Utente(
                    nome, cognome, username, passwordCifrata,
                    dataNascita.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    luogoDomicilio, ruolo
            );

            // Salva l'utente nel file CSV
            if (salvaUtenteNelCSV(nuovoUtente)) {
                mostraAvviso("Successo", "Registrazione completata con successo!\nPuoi ora effettuare il login.",
                        Alert.AlertType.INFORMATION);
                if (onUserRegistered != null) onUserRegistered.run();
                // Torna al login
                tornaAlLogin(evento);
            } else {
                mostraAvviso("Errore", "Errore durante la registrazione. Riprova.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostraAvviso("Errore", "Errore imprevisto durante la registrazione: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Torna alla schermata di login.
     * <p>
     * Carica il file FXML del login, applica il CSS se disponibile
     * e imposta la scena nella finestra corrente.
     * </p>
     *
     * @param evento L'evento generato dal clic sul pulsante.
     */
    @FXML
    private void tornaAlLogin(ActionEvent evento) {
        try {
            // Carica il file FXML del login
            FXMLLoader caricatore = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent radice = caricatore.load();

            // Calcola le dimensioni della finestra
            Rectangle2D limitiSchermo = Screen.getPrimary().getVisualBounds();
            double larghezza = Math.min(600, limitiSchermo.getWidth() * 0.6);
            double altezza = Math.min(500, limitiSchermo.getHeight() * 0.7);

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
            palcoscenico.setTitle("TheKnife - Login");
            palcoscenico.setScene(scena);
            palcoscenico.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostraAvviso("Errore", "Impossibile tornare al login: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Valida tutti i campi di input.
     * <p>
     * Controlla la presenza dei dati, la lunghezza minima e la correttezza di username e password.
     * Inoltre verifica età minima e dati del domicilio.
     * </p>
     *
     * @return true se tutti i campi sono validi, false altrimenti.
     */
    private boolean validaCampi() {
        List<String> errori = new ArrayList<>();

        // Valida nome
        if (campoNome.getText().trim().isEmpty()) {
            errori.add("Il nome è obbligatorio");
        } else if (campoNome.getText().trim().length() < 2) {
            errori.add("Il nome deve avere almeno 2 caratteri");
        }

        // Valida cognome
        if (campoCognome.getText().trim().isEmpty()) {
            errori.add("Il cognome è obbligatorio");
        } else if (campoCognome.getText().trim().length() < 2) {
            errori.add("Il cognome deve avere almeno 2 caratteri");
        }

        // Valida username
        if (campoUsername.getText().trim().isEmpty()) {
            errori.add("L'username è obbligatorio");
        } else if (campoUsername.getText().trim().length() < 3) {
            errori.add("L'username deve avere almeno 3 caratteri");
        } else if (!campoUsername.getText().matches("^[a-zA-Z0-9._-]+$")) {
            errori.add("L'username può contenere solo lettere, numeri, punti, underscore e trattini");
        }

        // Valida password
        if (campoPassword.getText().isEmpty()) {
            errori.add("La password è obbligatoria");
        } else if (campoPassword.getText().length() < 5) {
            errori.add("La password deve avere almeno 5 caratteri");
        }

        // Valida conferma password
        if (!campoPassword.getText().equals(campoConfermaPassword.getText())) {
            errori.add("Le password non corrispondono");
        }

        // Valida data di nascita
        if (campoDataNascita.getValue() == null) {
            errori.add("La data di nascita è obbligatoria");
        } else if (campoDataNascita.getValue().isAfter(LocalDate.now().minusYears(16))) {
            errori.add("Devi avere almeno 16 anni per registrarti");
        } else if (campoDataNascita.getValue().isBefore(LocalDate.now().minusYears(120))) {
            errori.add("Data di nascita non valida");
        }

        // Valida luogo di domicilio
        if (campoLuogoDomicilio.getText().trim().isEmpty()) {
            errori.add("Il luogo di domicilio è obbligatorio");
        } else if (campoLuogoDomicilio.getText().trim().length() < 2) {
            errori.add("Il luogo di domicilio deve avere almeno 2 caratteri");
        }

        // Valida ruolo
        if (comboRuolo.getValue() == null || comboRuolo.getValue().isEmpty()) {
            errori.add("Seleziona un ruolo");
        }

        if (!errori.isEmpty()) {
            String messaggioErrore = "Correggi i seguenti errori:\n\n" + String.join("\n", errori);
            mostraAvviso("Errori di Validazione", messaggioErrore, Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /**
     * Verifica se un username esiste già nel file CSV.
     *
     * @param username L'username da verificare.
     * @return true se l'username esiste già, false altrimenti.
     */
    private boolean verificaUsernameEsistente(String username) {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/utenti.csv");
             BufferedReader lettore = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            if (inputStream == null) {
                return false; // Se il file non esiste, l'username non può esistere
            }

            String riga = lettore.readLine(); // Salta l'header
            while ((riga = lettore.readLine()) != null) {
                if (!riga.trim().isEmpty()) {
                    String[] parti = riga.split(",");
                    if (parti.length >= 3 && parti[2].trim().equals(username)) {
                        return true; // Username trovato
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Salva un nuovo utente nel file CSV.
     *
     * @param utente L'utente da salvare.
     * @return true se il salvataggio è riuscito, false altrimenti.
     */
    private boolean salvaUtenteNelCSV(Utente utente) {
        try {
            // Percorso del file CSV
            String percorsoFile = "src/main/resources/data/utenti.csv";

            // Crea la stringa CSV per il nuovo utente
            String rigaCSV = String.format("%s,%s,%s,%s,%s,%s,%s%n",
                    utente.getNome(),
                    utente.getCognome(),
                    utente.getUsername(),
                    utente.getPasswordHash(),
                    utente.getDataNascita(),
                    utente.getLuogoDomicilio(),
                    utente.getRuolo()
            );

            // Aggiungi la riga al file CSV
            Files.write(Paths.get(percorsoFile), rigaCSV.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            System.out.println("DEBUG: Utente salvato nel CSV: " + utente.toString());
            return true;

        } catch (IOException e) {
            System.out.println("ERRORE: Impossibile salvare l'utente nel CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cifra la password con SHA-256.
     *
     * @param password La password in chiaro.
     * @return La password cifrata in formato esadecimale.
     * @throws NoSuchAlgorithmException Se SHA-256 non è disponibile.
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
     * Mostra un alert all'utente.
     *
     * @param titolo Titolo dell'alert.
     * @param messaggio Messaggio da visualizzare.
     * @param tipoAvviso Tipo di alert (INFO, WARNING, ERROR).
     */
    private void mostraAvviso(String titolo, String messaggio, Alert.AlertType tipoAvviso) {
        Alert avviso = new Alert(tipoAvviso);
        avviso.setTitle(titolo);
        avviso.setHeaderText(null);
        avviso.setContentText(messaggio);
        avviso.showAndWait();
    }

    /**
     * Imposta un callback da eseguire quando un utente viene registrato con successo.
     *
     * @param callback La funzione da eseguire dopo la registrazione.
     */
    public void setOnUserRegistered(Runnable callback) {
        this.onUserRegistered = callback;
    }
}