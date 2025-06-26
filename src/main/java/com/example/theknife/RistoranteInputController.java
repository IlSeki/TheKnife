package com.example.theknife;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller per la gestione dell'input dei dati di un ristorante.
 * Gestisce l'interfaccia per l'inserimento e la modifica dei dati
 * di un ristorante, inclusi nome, indirizzo, tipologia di cucina,
 * servizi offerti e premi.
 *
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

    private final RistoranteService ristoranteService = RistoranteService.getInstance();
    private final RistoranteOwnershipService ownershipService = RistoranteOwnershipService.getInstance();

    /**
     * Inizializza il controller configurando gli elementi dell'interfaccia.
     * Popola i ComboBox e le ListView con le opzioni disponibili.
     *
     * @param location  L'URL di localizzazione della risorsa FXML (non utilizzato)
     * @param resources Le risorse aggiuntive per l'inizializzazione (non utilizzate)
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
     * Gestisce il salvataggio dei dati del ristorante.
     * Valida i dati inseriti e, se validi, crea un nuovo ristorante
     * e lo associa al ristoratore corrente.
     */
    @FXML
    private void handleSalva() {
        if (!validaInput()) {
            return;
        }

        try {
            double longitudine = longitudineField.getText().isEmpty() ? 0.0 : 
                Double.parseDouble(longitudineField.getText());
            double latitudine = latitudineField.getText().isEmpty() ? 0.0 : 
                Double.parseDouble(latitudineField.getText());

            String nome = nomeField.getText().trim();
            
            if (ristoranteService.getRistorante(nome) != null) {
                mostraErrore("Errore", "Esiste già un ristorante con questo nome. Scegli un nome diverso.");
                return;
            }

            // Ottieni i tipi di cucina selezionati
            String cucine = cucinaListView.getSelectionModel().getSelectedItems()
                .stream()
                .collect(Collectors.joining(", "));

            // Ottieni i servizi selezionati
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

            // Crea la directory se non esiste
            File dir = new File("src/main/resources/data");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Aggiungi il nuovo ristorante al CSV
            aggiungiRistoranteAlCSV(nuovoRistorante);

            // Associa il ristorante al proprietario corrente
            String username = SessioneUtente.getUsernameUtente();
            if (username != null && SessioneUtente.isRistoratore()) {
                ownershipService.associaRistoranteAProprietario(nome, username);
            }

            // Torna alla dashboard del ristoratore
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ristoratore-dashboard.fxml"));
                Parent root = loader.load();

                // Usa la scena corrente invece di crearne una nuova
                Scene currentScene = nomeField.getScene();
                Stage stage = (Stage) currentScene.getWindow();
                
                // Applica lo stile
                currentScene.setRoot(root);
                currentScene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());
                
                // Inizializza il controller per aggiornare la lista
                RistoratoreDashboardController controller = loader.getController();
                controller.refreshData();

            } catch (IOException e) {
                System.err.println("Errore nel ritorno alla dashboard: " + e.getMessage());
                mostraErrore("Errore", "Impossibile tornare alla dashboard del ristoratore");
            }

        } catch (NumberFormatException e) {
            mostraErrore("Errore", "Le coordinate devono essere numeri validi");
        } catch (IOException e) {
            mostraErrore("Errore", "Impossibile salvare il ristorante: " + e.getMessage());
        }
    }

    /**
     * Valida i dati inseriti nel form.
     * Controlla che i campi obbligatori siano stati compilati
     * e che i valori numerici siano validi.
     *
     * @return true se i dati sono validi, false altrimenti
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
     * Chiude la finestra corrente senza salvare le modifiche.
     */
    @FXML
    private void handleAnnulla() {
        Stage stage = (Stage) nomeField.getScene().getWindow();
        stage.close();
    }

    /**
     * Mostra un messaggio di errore all'utente.
     *
     * @param titolo   Il titolo della finestra di errore
     * @param messaggio Il messaggio di errore da mostrare
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private void aggiungiRistoranteAlCSV(Ristorante ristorante) throws IOException {
        String csvLine = String.format("%s,%s,%s,%s,%s,%.6f,%.6f,%s,%s,%s,%s,%s,%s,%s\n",
            ristorante.getNome().replace(",", ";"),
            ristorante.getIndirizzo().replace(",", ";"),
            ristorante.getLocalita().replace(",", ";"),
            ristorante.getPrezzo(),
            ristorante.getCucina().replace(",", ";"),
            ristorante.getLongitudine(),
            ristorante.getLatitudine(),
            ristorante.getNumeroTelefono().replace(",", ";"),
            ristorante.getUrl().replace(",", ";"),
            ristorante.getSitoWeb().replace(",", ";"),
            ristorante.getPremio().replace(",", ";"),
            ristorante.getStellaVerde(),
            ristorante.getServizi().replace(",", ";"),
            ristorante.getDescrizione().replace(",", ";")
        );

        try (FileWriter writer = new FileWriter("src/main/resources/data/michelin_my_maps.csv", true)) {
            writer.append(csvLine);
        }
    }
}