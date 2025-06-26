package com.example.theknife;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * Controller per la gestione delle recensioni.
 * Gestisce l'interfaccia che mostra la lista delle recensioni di un ristorante
 * e permette di aggiungere nuove recensioni o rispondere a quelle esistenti.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RecensioniController {
    @FXML private PieChart pieChart;
    @FXML private ComboBox<Integer> comboBox;
    @FXML private TableView<Recensione> tableView;
    @FXML private TableColumn<Recensione, Integer> colStelle;
    @FXML private TableColumn<Recensione, String> colTesto;
    @FXML private TableColumn<Recensione, String> colData;
    @FXML private TableColumn<Recensione, String> colUtente;
    @FXML private TableColumn<Recensione, String> colRisposta;
    @FXML private TextArea recensioneTextArea;
    @FXML private Slider stelleSlider;
    @FXML private Button inviaButton;
    @FXML private Button modificaButton;
    @FXML private Button eliminaButton;
    @FXML private Button rispondiButton;
    @FXML private VBox rispostaBox;
    @FXML private TextArea rispostaTextArea;

    private final RecensioneService recensioneService = RecensioneService.getInstance();
    private final RistoranteOwnershipService ownershipService = RistoranteOwnershipService.getInstance();
    private String ristoranteId;
    private final Map<Integer, Integer> recensioniMap = new HashMap<>();
    private ObservableList<Recensione> masterRecensioniList;
    private FilteredList<Recensione> filteredList;

    @FXML
    public void initialize() {
        comboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        
        colStelle.setCellValueFactory(new PropertyValueFactory<>("stelle"));
        colTesto.setCellValueFactory(new PropertyValueFactory<>("testo"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colUtente.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRisposta.setCellValueFactory(new PropertyValueFactory<>("risposta"));

        stelleSlider.setMin(1);
        stelleSlider.setMax(5);
        stelleSlider.setBlockIncrement(1);
        stelleSlider.setSnapToTicks(true);
        stelleSlider.setShowTickLabels(true);
        stelleSlider.setShowTickMarks(true);

        masterRecensioniList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(masterRecensioniList, p -> true);
        tableView.setItems(filteredList);

        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filteredList.setPredicate(recensione -> recensione.getStelle() == newVal);
            } else {
                filteredList.setPredicate(recensione -> true);
            }
            aggiornaPieChart();
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                recensioneTextArea.setText(newVal.getTesto());
                stelleSlider.setValue(newVal.getStelle());
                
                boolean isAutore = newVal.getUsername().equals(SessioneUtente.getUsernameUtente());
                boolean isRistoratore = SessioneUtente.isRistoratore();
                
                modificaButton.setVisible(isAutore);
                eliminaButton.setVisible(isAutore);
                rispondiButton.setVisible(isRistoratore && newVal.getRisposta().isEmpty());
                rispostaBox.setVisible(isRistoratore);
            }
        });

        // Nascondi i controlli per la risposta se non sei ristoratore
        boolean isRistoratore = SessioneUtente.isRistoratore();
        rispondiButton.setVisible(isRistoratore);
        rispostaBox.setVisible(isRistoratore);
    }

    public void setRistoranteId(String id) {
        this.ristoranteId = id;
        caricaRecensioni();
    }

    private void caricaRecensioni() {
        masterRecensioniList.clear();
        masterRecensioniList.addAll(recensioneService.getRecensioniRistorante(ristoranteId));
        aggiornaPieChart();
    }

    private void aggiornaPieChart() {
        pieChart.getData().clear();
        recensioniMap.clear();

        // Conta le recensioni per ogni numero di stelle
        for (Recensione r : masterRecensioniList) {
            recensioniMap.merge(r.getStelle(), 1, Integer::sum);
        }

        // Aggiorna il PieChart
        recensioniMap.forEach((stelle, numero) -> 
            pieChart.getData().add(new PieChart.Data(stelle + " ⭐", numero))
        );
    }

    @FXML
    private void handleInvia() {
        if (!SessioneUtente.isUtenteLoggato()) {
            mostraErrore("Accesso richiesto", "Per scrivere una recensione devi effettuare l'accesso.");
            return;
        }

        if (recensioneTextArea.getText().trim().isEmpty()) {
            mostraErrore("Errore", "Il testo della recensione non può essere vuoto");
            return;
        }

        // Controlla se l'utente ha già recensito questo ristorante
        if (masterRecensioniList.stream()
                .anyMatch(r -> r.getUsername().equals(SessioneUtente.getUsernameUtente()))) {
            mostraErrore("Errore", "Hai già recensito questo ristorante. Puoi modificare la tua recensione esistente.");
            return;
        }

        Recensione recensione = new Recensione(
            (int) stelleSlider.getValue(),
            recensioneTextArea.getText().trim(),
            ristoranteId,
            SessioneUtente.getUsernameUtente()
        );

        recensioneService.aggiungiRecensione(recensione);
        caricaRecensioni();
        pulisciCampi();
    }

    @FXML
    private void handleModifica() {
        Recensione selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!selected.getUsername().equals(SessioneUtente.getUsernameUtente())) {
            mostraErrore("Errore", "Puoi modificare solo le tue recensioni");
            return;
        }

        recensioneService.modificaRecensione(
            selected.getUsername(),
            ristoranteId,
            recensioneTextArea.getText(),
            (int) stelleSlider.getValue()
        );

        caricaRecensioni();
        pulisciCampi();
    }

    @FXML
    private void handleElimina() {
        Recensione selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!selected.getUsername().equals(SessioneUtente.getUsernameUtente())) {
            mostraErrore("Errore", "Puoi eliminare solo le tue recensioni");
            return;
        }

        recensioneService.eliminaRecensione(selected.getUsername(), ristoranteId);
        caricaRecensioni();
        pulisciCampi();
    }

    @FXML
    private void handleRispondi() {
        Recensione selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String currentUser = SessioneUtente.getUsernameUtente();
        if (!ownershipService.isOwner(currentUser, selected.getRistoranteId())) {
            mostraErrore("Errore", "Puoi rispondere solo alle recensioni dei tuoi ristoranti");
            return;
        }

        if (rispostaTextArea.getText().trim().isEmpty()) {
            mostraErrore("Errore", "La risposta non può essere vuota");
            return;
        }

        recensioneService.aggiungiRisposta(
            selected.getUsername(),
            selected.getRistoranteId(),
            rispostaTextArea.getText()
        );

        caricaRecensioni();
        pulisciCampi();
    }

    private void pulisciCampi() {
        recensioneTextArea.clear();
        rispostaTextArea.clear();
        stelleSlider.setValue(3);
        tableView.getSelectionModel().clearSelection();
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
