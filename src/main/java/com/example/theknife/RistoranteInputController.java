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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller per la gestione dell'interfaccia di inserimento di un nuovo ristorante.
 * <p>
 * Questa classe gestisce l'interazione tra l'interfaccia utente FXML (`ristorante-input.fxml`)
 * e la logica di business per l'aggiunta di nuovi ristoranti nel sistema. Si occupa di:
 * <ul>
 * <li>Inizializzazione dei componenti dell'interfaccia (ComboBox, ListView).</li>
 * <li>Validazione degli input dell'utente per garantire la correttezza dei dati.</li>
 * <li>Creazione di un oggetto {@link Ristorante} a partire dagli input.</li>
 * <li>Salvataggio del nuovo ristorante nel file CSV.</li>
 * <li>Associazione del ristorante all'utente ristoratore corrente.</li>
 * <li>Gestione dei feedback per l'utente, sia in caso di successo che di errore.</li>
 * <li>Gestione della navigazione tramite callback per tornare alla dashboard principale.</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistoranteInputController implements Initializable {
    /** Campo di testo per il nome del ristorante. */
    @FXML private TextField nomeField;
    /** Campo di testo per l'indirizzo del ristorante. */
    @FXML private TextField indirizzoField;
    /** Campo di testo per la località del ristorante. */
    @FXML private TextField localitaField;
    /** ComboBox per la selezione della fascia di prezzo. */
    @FXML private ComboBox<String> prezzoComboBox;
    /** ListView per la selezione multipla dei tipi di cucina. */
    @FXML private ListView<String> cucinaListView;
    /** ComboBox per la selezione del premio Michelin. */
    @FXML private ComboBox<String> premiComboBox;
    /** CheckBox per indicare la presenza della stella verde. */
    @FXML private CheckBox stellaVerdeCheckBox;
    /** Campo di testo per la longitudine. */
    @FXML private TextField longitudineField;
    /** Campo di testo per la latitudine. */
    @FXML private TextField latitudineField;
    /** Campo di testo per il numero di telefono. */
    @FXML private TextField telefonoField;
    /** Campo di testo per l'URL del ristorante. */
    @FXML private TextField urlField;
    /** Campo di testo per il sito web del ristorante. */
    @FXML private TextField sitoWebField;
    /** ListView per la selezione multipla dei servizi offerti. */
    @FXML private ListView<String> checkBoxServizi;
    /** Area di testo per la descrizione del ristorante. */
    @FXML private TextArea descrizioneArea;
    /** Pulsante per avviare il salvataggio dei dati. */
    @FXML private Button salvaButton;
    /** Pulsante per annullare l'operazione. */
    @FXML private Button annullaButton;

    private final GestioneRistorante gestioneRistorante = GestioneRistorante.getInstance();
    private final GestionePossessoRistorante ownershipService = GestionePossessoRistorante.getInstance();

    private Runnable tornaAllaDashboardCallback;
    private Runnable aggiornaDatabaseRistorantiCallback;

    /**
     * Imposta il callback da eseguire per tornare alla dashboard principale.
     * Questo metodo viene utilizzato per implementare la navigazione tra le scene.
     *
     * @param callback L'oggetto {@link Runnable} che esegue l'operazione di ritorno.
     */
    public void setTornaAllaDashboardCallback(Runnable callback) {
        this.tornaAllaDashboardCallback = callback;
    }

    /**
     * Imposta il callback da eseguire per forzare l'aggiornamento dei dati
     * dopo il salvataggio di un nuovo ristorante.
     *
     * @param callback L'oggetto {@link Runnable} che esegue l'operazione di aggiornamento.
     */
    public void setAggiornaDatabaseRistorantiCallback(Runnable callback) {
        this.aggiornaDatabaseRistorantiCallback = callback;
    }

    /**
     * Inizializza i componenti dell'interfaccia utente.
     * Questo metodo è chiamato automaticamente dal framework JavaFX dopo il caricamento
     * del file FXML. Si occupa di popolare le liste e le ComboBox con i valori
     * predefiniti e di configurare la modalità di selezione.
     *
     * @param location L'URL utilizzato per risolvere i percorsi relativi, o {@code null}.
     * @param resources Le risorse utilizzate per la localizzazione, o {@code null}.
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
     * Gestisce l'evento di click sul pulsante "Salva".
     * Esegue la validazione degli input, crea un nuovo oggetto {@link Ristorante},
     * lo salva in modo persistente nel file CSV e lo associa all'utente ristoratore corrente.
     * Al termine, mostra un messaggio di successo e naviga verso la dashboard.
     */
    @FXML
    private void handleSalva() {
        if (!validaInput()) {
            return;
        }

        try {
            double longitudine = longitudineField.getText().isEmpty() ? 0.0 : Double.parseDouble(longitudineField.getText());
            double latitudine = latitudineField.getText().isEmpty() ? 0.0 : Double.parseDouble(latitudineField.getText());

            String nome = nomeField.getText().trim();
            if (gestioneRistorante.getRistorante(nome) != null) {
                mostraErrore("Errore", "Esiste già un ristorante con questo nome. Scegli un nome diverso.");
                return;
            }

            String cucine = cucinaListView.getSelectionModel().getSelectedItems()
                    .stream()
                    .collect(Collectors.joining(", "));

            String servizi = checkBoxServizi.getSelectionModel().getSelectedItems()
                    .stream()
                    .collect(Collectors.joining(", "));

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

            // Aggiorna i servizi dopo il salvataggio
            GestioneRistorante.getInstance().forceRefresh();
            GestionePossessoRistorante.getInstance().refreshOwnershipData();

            // Esegue il callback per tornare alla dashboard
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
     * Valida tutti gli input forniti dall'utente nei campi di testo e nelle selezioni.
     * <p>
     * Questo metodo controlla che i campi obbligatori (nome, indirizzo, località, prezzo,
     * cucina) non siano vuoti e che le coordinate, se inserite, siano numeri validi.
     * </p>
     *
     * @return {@code true} se tutti gli input sono validi, {@code false} altrimenti.
     * In caso di fallimento, mostra un messaggio di errore all'utente.
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
     * Gestisce l'evento di click sul pulsante "Annulla".
     * Abbandona la schermata di inserimento e torna alla dashboard principale
     * senza salvare alcun dato.
     */
    @FXML
    private void handleAnnulla() {
        if (tornaAllaDashboardCallback != null) {
            tornaAllaDashboardCallback.run();
        }
    }

    /**
     * Mostra una finestra di dialogo di errore all'utente.
     *
     * @param titolo Il titolo della finestra di dialogo.
     * @param messaggio Il messaggio di errore da visualizzare.
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    /**
     * Aggiunge un nuovo ristorante al file CSV `michelin_my_maps.csv`.
     * <p>
     * Il metodo scrive i dati del ristorante in una nuova riga del file CSV,
     * assicurando che i dati vengano persistiti. Utilizza la modalità di append
     * per non sovrascrivere i dati esistenti.
     * </p>
     *
     * @param ristorante L'oggetto {@link Ristorante} da salvare.
     * @throws IOException se si verifica un errore durante la scrittura del file.
     */
    private void aggiungiRistoranteAlCSV(Ristorante ristorante) throws IOException {
        String filePath = "data/michelin_my_maps.csv";
        // Controlla e crea il file e le directory necessarie
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("nome,indirizzo,localita,prezzo,cucina,longitudine,latitudine,numeroTelefono,url,sitoWeb,premio,stellaVerde,servizi,descrizione\n");
            }
        }

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
            throw e; // Rilancia l'eccezione per la gestione a livello superiore
        }
    }
}