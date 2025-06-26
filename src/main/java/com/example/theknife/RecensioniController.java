package com.example.theknife;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    @FXML private Label totaleRecensioniLabel;

    private final RecensioneService recensioneService = RecensioneService.getInstance();
    private final RistoranteOwnershipService ownershipService = RistoranteOwnershipService.getInstance();
    private String ristoranteId;
    private final Map<Integer, Integer> recensioniMap = new HashMap<>();
    private ObservableList<Recensione> masterRecensioniList;
    private FilteredList<Recensione> filteredList;
    private RistoratoreDashboardController parentController;
    private Parent rootToRestore;
    private Runnable tornaAlMenuPrincipaleCallback;

    /**
     * Imposta il controller della dashboard del ristoratore come parent.
     * Questo permette di aggiornare la dashboard quando vengono fatte modifiche alle recensioni.
     *
     * @param controller Il controller della dashboard del ristoratore
     */
    public void setParentController(RistoratoreDashboardController controller) {
        this.parentController = controller;
    }

    /**
     * Notifica il controller padre di eventuali aggiornamenti alle recensioni
     * in modo che possa aggiornare le statistiche e la lista.
     */
    private void notificaAggiornamentoRecensioni() {
        if (parentController != null) {
            parentController.onRecensioneUpdated();
        }
    }

    public void setRootToRestore(Parent root) {
        this.rootToRestore = root;
    }
    public void setTornaAlMenuPrincipaleCallback(Runnable callback) {
        this.tornaAlMenuPrincipaleCallback = callback;
    }

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

    public void refreshData() {
        if (ristoranteId != null) {
            List<Recensione> recensioni = recensioneService.getRecensioniRistorante(ristoranteId);
            masterRecensioniList = FXCollections.observableArrayList(recensioni);
            filteredList = new javafx.collections.transformation.FilteredList<>(masterRecensioniList, p -> true);
            tableView.setItems(filteredList);
            aggiornaPieChart();
        }
    }

    public void setRistoranteId(String id) {
        this.ristoranteId = id;
        refreshData();
        aggiornaPieChart();
    }

    private void caricaRecensioni() {
        masterRecensioniList.clear();
        masterRecensioniList.addAll(recensioneService.getRecensioniRistorante(ristoranteId));
        aggiornaPieChart();
    }

    @FXML
    private void handleTornaAlMenuPrincipale() {
        // Torna sempre al dettaglio del ristorante
        if (rootToRestore != null) {
            Scene scene = pieChart.getScene();
            scene.setRoot(rootToRestore);
        }
    }

    private void aggiornaPieChart() {
        pieChart.getData().clear();
        recensioniMap.clear();
        int totale = 0;
        // Conta le recensioni per ogni numero di stelle
        for (Recensione r : masterRecensioniList) {
            recensioniMap.merge(r.getStelle(), 1, Integer::sum);
            totale++;
        }
        // Mostra sempre tutte le 5 quantità di stelle
        for (int stelle = 1; stelle <= 5; stelle++) {
            int count = recensioniMap.getOrDefault(stelle, 0);
            pieChart.getData().add(new PieChart.Data(stelle + " ⭐", count));
        }
        if (totaleRecensioniLabel != null) {
            totaleRecensioniLabel.setText("Totale recensioni: " + totale);
        }
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
        notificaAggiornamentoRecensioni();
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
        notificaAggiornamentoRecensioni();
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
        notificaAggiornamentoRecensioni();
    }

    @FXML
    private void handleRispondi() {
        Recensione selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!SessioneUtente.isRistoratore() || rispostaTextArea.getText().trim().isEmpty()) {
            mostraErrore("Errore", "Non puoi rispondere a questa recensione");
            return;
        }

        selected.setRisposta(rispostaTextArea.getText().trim());
        recensioneService.salvaRispostaRecensione(selected);
        caricaRecensioni();
        rispostaTextArea.clear();
        notificaAggiornamentoRecensioni();
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
