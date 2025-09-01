package com.example.theknife;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import com.opencsv.CSVWriter;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller per la gestione dell'interfaccia di inserimento di un nuovo ristorante.
 * Questa classe gestisce l'interazione tra l'interfaccia utente FXML e la logica di business
 * per l'aggiunta di nuovi ristoranti nel sistema.
 *
 * Il controller si occupa di:
 * - Inizializzazione dei componenti dell'interfaccia
 * - Validazione degli input dell'utente
 * - Salvataggio dei dati del ristorante
 * - Gestione degli errori e feedback utente
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */


public class RistoranteInputController implements Initializable {
    @FXML private TextField nomeField;
    @FXML private TextField indirizzoField;
    @FXML private TextField localitaField;
    @FXML private ComboBox<String> prezzoComboBox;
    @FXML private ListView<String> cucinaListView;
    @FXML private ComboBox<String> premiComboBox;
    @FXML private CheckBox stellaVerdeCheckBox;
    @FXML private TextField longitudineField;
    @FXML private TextField latitudineField;
    @FXML private TextField telefonoField;
    @FXML private TextField urlField;
    @FXML private TextField sitoWebField;
    @FXML private ListView<String> checkBoxServizi;
    @FXML private TextArea descrizioneArea;

    private final GestioneRistorante gestioneRistorante = GestioneRistorante.getInstance();
    private final GestionePossessoRistorante ownershipService = GestionePossessoRistorante.getInstance();

    private Runnable tornaAllaDashboardCallback;
    private Runnable aggiornaDatabaseRistorantiCallback;

    /**
     * Imposta il callback per tornare alla dashboard principale.
     *
     * @param callback il callback da eseguire per tornare alla dashboard
     */
    public void setTornaAllaDashboardCallback(Runnable callback) {
        this.tornaAllaDashboardCallback = callback;
    }
    /**
     * Imposta il callback per aggiornare il database dei ristoranti.
     *
     * @param callback il callback da eseguire per aggiornare il database
     */
    public void setAggiornaDatabaseRistorantiCallback(Runnable callback) {
        this.aggiornaDatabaseRistorantiCallback = callback;
    }

    /**
     * Inizializza i componenti dell'interfaccia utente.
     * Questo metodo viene chiamato automaticamente dal framework JavaFX dopo il caricamento del file FXML.
     *
     * Si occupa di:
     * - Popolare le ComboBox con i valori predefiniti
     * - Configurare le ListView per la selezione multipla
     * - Impostare i valori di default
     *
     * @param location L'URL utilizzato per risolvere i percorsi relativi per l'oggetto root, o null se non conosciuto
     * @param resources Le risorse utilizzate per localizzare l'oggetto root, o null se non localizzato
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prezzoComboBox.setItems(FXCollections.observableArrayList("€", "€€", "€€€", "€€€€"));

        cucinaListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        cucinaListView.setItems(FXCollections.observableArrayList(
                "Creative", "Contemporary", "Korean", "French", "Italian",
                "Japanese", "Chinese", "Indian", "Mediterranean", "Fusion",
                "American", "Mexican", "Thai", "Vegetarian", "Vegan",
                "Seafood", "Steakhouse", "Traditional", "Modern", "International"
        ));

        premiComboBox.setItems(FXCollections.observableArrayList(
                "Nessun premio", "1 Star", "2 Stars",
                "3 Stars", "Bib Gourmand"
        ));
        premiComboBox.setValue("Nessun premio");

        checkBoxServizi.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        checkBoxServizi.setItems(FXCollections.observableArrayList(
                "Air conditioning", "Garden or park", "Interesting wine list",
                "Terrace", "Wheelchair access", "Great view",
                "Restaurant offering vegetarian menus", "Private dining room",
                "Valet parking", "Bar", "Notable wine list", "Outdoor seating"
        ));
    }

    /**
     * Gestisce il salvataggio di un nuovo ristorante.
     * Questo metodo viene chiamato quando l'utente clicca il pulsante "Salva".
     * Esegue le seguenti operazioni:
     * 1. Valida tutti gli input dell'utente
     * 2. Verifica che non esista già un ristorante con lo stesso nome
     * 3. Crea un nuovo oggetto Ristorante con i dati inseriti
     * 4. Salva il ristorante nel file CSV
     * 5. Associa il ristorante al proprietario se l'utente è un ristoratore
     * 6. Aggiorna il database e torna alla dashboard
     *
     * @throws Exception se si verifica un errore durante il salvataggio
     */
    @FXML
    private void handleSalva() {
        if (!validaInput()) {
            return;
        }

        try {
            // Parsing delle coordinate con valori di default
            double longitudine = longitudineField.getText().isEmpty() ? 0.0 :
                    Double.parseDouble(longitudineField.getText());
            double latitudine = latitudineField.getText().isEmpty() ? 0.0 :
                    Double.parseDouble(latitudineField.getText());
            // Verifica unicità del nome
            String nome = nomeField.getText().trim();
            if (gestioneRistorante.getRistorante(nome) != null) {
                mostraErrore("Errore", "Esiste già un ristorante con questo nome. Scegli un nome diverso.");
                return;
            }
            // Costruzione stringhe per campi multipli
            String cucine = cucinaListView.getSelectionModel().getSelectedItems()
                    .stream()
                    .collect(Collectors.joining(", "));

            String servizi = checkBoxServizi.getSelectionModel().getSelectedItems()
                    .stream()
                    .collect(Collectors.joining(", "));
            // Creazione del nuovo ristorante
            Ristorante nuovoRistorante = new Ristorante(
                    nome,
                    indirizzoField.getText().trim(),
                    localitaField.getText().trim(),
                    prezzoComboBox.getValue(),
                    cucine,
                    longitudine,
                    latitudine,
                    telefonoField.getText().trim(),
                    urlField.getText().trim(),
                    sitoWebField.getText().trim(),
                    premiComboBox.getValue(),
                    stellaVerdeCheckBox.isSelected() ? "Sì" : "No",
                    servizi,
                    descrizioneArea.getText().trim()
            );
            // Salvataggio nel CSV
            aggiungiRistoranteAlCSV(nuovoRistorante);

            String username = SessioneUtente.getUsernameUtente();
            if (username != null && SessioneUtente.isRistoratore()) {
                ownershipService.associaRistoranteAProprietario(nome, username);
            }

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Successo");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Il ristorante è stato aggiunto con successo!");
            successAlert.showAndWait();

            System.out.println("Debug: Salvataggio ristorante completato");
            // Salvataggio nel CSV
            GestioneRistorante.getInstance().forceRefresh();

            if (aggiornaDatabaseRistorantiCallback != null) {
                aggiornaDatabaseRistorantiCallback.run();
            }

            GestionePossessoRistorante.getInstance().refreshOwnershipData();

            if (tornaAllaDashboardCallback != null) {
                tornaAllaDashboardCallback.run();
            }

        } catch (Exception e) {
            System.err.println("Errore durante il salvataggio: " + e.getMessage());
            e.printStackTrace();
            mostraErrore("Errore", e.getMessage());
        }
    }
    /**
     * Valida tutti gli input dell'utente prima del salvataggio.
     *
     * Controlla che:
     * - I campi obbligatori non siano vuoti (nome, indirizzo, località, prezzo, cucina)
     * - Le coordinate, se inserite, siano numeri validi
     *
     * @return true se tutti gli input sono validi, false altrimenti
     */
    private boolean validaInput() {
        StringBuilder errori = new StringBuilder();
        if (nomeField.getText().trim().isEmpty()) {
            errori.append("- Il nome è obbligatorio\n");
        }
        if (indirizzoField.getText().trim().isEmpty()) {
            errori.append("- L'indirizzo è obbligatorio\n");
        }
        if (localitaField.getText().trim().isEmpty()) {
            errori.append("- La località è obbligatoria\n");
        }
        if (prezzoComboBox.getValue() == null) {
            errori.append("- La fascia di prezzo è obbligatoria\n");
        }
        if (cucinaListView.getSelectionModel().getSelectedItems().isEmpty()) {
            errori.append("- Il tipo di cucina è obbligatorio\n");
        }
        if (!longitudineField.getText().trim().isEmpty() || !latitudineField.getText().trim().isEmpty()) {
            try {
                if (!longitudineField.getText().trim().isEmpty()) {
                    Double.parseDouble(longitudineField.getText());
                }
                if (!latitudineField.getText().trim().isEmpty()) {
                    Double.parseDouble(latitudineField.getText());
                }
            } catch (NumberFormatException e) {
                errori.append("- Le coordinate devono essere numeri validi\n");
            }
        }
        if (errori.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Correggi i seguenti errori:");
            alert.setContentText(errori.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }
    /**
     * Gestisce l'annullamento dell'inserimento del ristorante.
     * Questo metodo viene chiamato quando l'utente clicca il pulsante "Annulla".
     * Riporta l'utente alla dashboard principale senza salvare i dati.
     */
    @FXML
    private void handleAnnulla() {
        if (tornaAllaDashboardCallback != null) {
            tornaAllaDashboardCallback.run();
        }
    }
    /**
     * Mostra un messaggio di errore all'utente tramite una finestra di dialogo.
     *
     * @param titolo il titolo della finestra di errore
     * @param messaggio il messaggio di errore da visualizzare
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    /**
     * Aggiunge un nuovo ristorante al file CSV.
     * Questo metodo scrive i dati del ristorante in formato CSV nel file specificato,
     * mantenendo la persistenza dei dati del sistema.
     *
     * Il file CSV utilizzato è "data/michelin_my_maps.csv" e viene aperto in modalità append
     * per preservare i dati esistenti.
     *
     * @param ristorante l'oggetto Ristorante da salvare nel file CSV
     * @throws IOException se si verifica un errore durante la scrittura del file
     */
    private void aggiungiRistoranteAlCSV(Ristorante ristorante) {
        String filePath = "data/michelin_my_maps.csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, true))) {
            String[] record = new String[]{
                    ristorante.getNome(),
                    ristorante.getIndirizzo(),
                    ristorante.getLocalita(),
                    ristorante.getPrezzo(),
                    ristorante.getCucina(),
                    String.valueOf(ristorante.getLongitudine()),
                    String.valueOf(ristorante.getLatitudine()),
                    ristorante.getNumeroTelefono(),
                    ristorante.getUrl(),
                    ristorante.getSitoWeb(),
                    ristorante.getPremio(),
                    ristorante.getStellaVerde(),
                    ristorante.getServizi(),
                    ristorante.getDescrizione()
            };
            writer.writeNext(record);
            System.out.println("Ristorante aggiunto al file CSV.");
        } catch (IOException e) {
            System.err.println("Errore durante l'aggiunta del ristorante al CSV: " + e.getMessage());
        }
    }
}