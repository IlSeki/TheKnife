package com.example.theknife;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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

    public void setTornaAllaDashboardCallback(Runnable callback) {
        this.tornaAllaDashboardCallback = callback;
    }

    public void setAggiornaDatabaseRistorantiCallback(Runnable callback) {
        this.aggiornaDatabaseRistorantiCallback = callback;
    }

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

            // Passo cruciale: notifica alla dashboard di aggiornare il database principale
            if (aggiornaDatabaseRistorantiCallback != null) {
                aggiornaDatabaseRistorantiCallback.run();
            }

            // Torna alla dashboard
            if (tornaAllaDashboardCallback != null) {
                tornaAllaDashboardCallback.run();
            }

        } catch (NumberFormatException e) {
            mostraErrore("Errore", "Le coordinate devono essere numeri validi");
        }
    }

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

    @FXML
    private void handleAnnulla() {
        if (tornaAllaDashboardCallback != null) {
            tornaAllaDashboardCallback.run();
        }
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private void aggiungiRistoranteAlCSV(Ristorante ristorante) {
        String filePath = "data/michelin_my_maps.csv";
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

        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.append(csvLine);
            System.out.println("Ristorante aggiunto al file CSV.");
        } catch (IOException e) {
            System.err.println("Errore durante l'aggiunta del ristorante al CSV: " + e.getMessage());
        }
    }
}