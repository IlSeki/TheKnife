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
 * Controller per la gestione delle recensioni di un ristorante.
 * <p>
 * Permette di:
 * <ul>
 *     <li>Visualizzare le recensioni in una TableView</li>
 *     <li>Aggiungere, modificare o eliminare recensioni da parte degli utenti</li>
 *     <li>Rispondere alle recensioni da parte dei ristoratori</li>
 *     <li>Filtrare recensioni per numero di stelle</li>
 *     <li>Mostrare un grafico a torta con la distribuzione delle stelle</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi
 * @author Flavio Marin
 * @author Matilde Lecchi
 * @author Davide Caccia
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
    private Runnable refreshParentCallback;

    /**
     * Imposta il controller della dashboard del ristoratore come parent.
     * <p>
     * Serve per notificare aggiornamenti quando le recensioni vengono aggiunte,
     * modificate o eliminate.
     * </p>
     *
     * @param controller il controller padre della dashboard
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
    public void setRefreshParentCallback(Runnable callback) {
        this.refreshParentCallback = callback;
    }

    /**
     * Inizializza il controller.
     * <p>
     * Configura la tabella delle recensioni, il grafico a torta, i filtri e i listener
     * per selezione, modifica e risposta.
     * </p>
     */
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
                boolean isAutore = newVal.getUsername().equals(SessioneUtente.getUsernameUtente());
                boolean isRistoratore = SessioneUtente.isRistoratore();
                if (isAutore) {
                    recensioneTextArea.setText(newVal.getTesto());
                    stelleSlider.setValue(newVal.getStelle());
                } else {
                    recensioneTextArea.clear();
                    stelleSlider.setValue(1);
                }
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

    /**
     * Aggiorna la lista delle recensioni e il grafico a torta.
     */
    public void refreshData() {
        if (ristoranteId != null) {
            List<Recensione> recensioni = recensioneService.getRecensioniRistorante(ristoranteId);
            masterRecensioniList = FXCollections.observableArrayList(recensioni);
            filteredList = new javafx.collections.transformation.FilteredList<>(masterRecensioniList, p -> true);
            tableView.setItems(filteredList);
            aggiornaPieChart();
        }
    }

    /**
     * Imposta l'ID del ristorante corrente e aggiorna i dati.
     *
     * @param id ID del ristorante
     */
    public void setRistoranteId(String id) {
        this.ristoranteId = id;
        refreshData();
        aggiornaPieChart();
    }

    /**
     * Torna al menu principale.
     */
    @FXML
    private void handleTornaAlMenuPrincipale() {
        // Torna sempre al dettaglio del ristorante
        if (rootToRestore != null) {
            Scene scene = pieChart.getScene();
            scene.setRoot(rootToRestore);
        }
    }

    /**
     * Aggiorna il grafico a torta con il conteggio delle recensioni per ogni numero di stelle.
     */
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

    /**
     * Gestisce l'invio di una nuova recensione.
     * <p>
     * Controlla se l'utente è loggato, se il testo non è vuoto e se l'utente
     * non ha già recensito il ristorante. Poi crea la recensione e aggiorna
     * la vista.
     */
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
        refreshData();
        if (refreshParentCallback != null) refreshParentCallback.run();
        pulisciCampi();
        notificaAggiornamentoRecensioni();
    }

    /**
     * Gestisce la modifica della recensione selezionata.
     * <p>
     * Permette all'utente di modificare solo le proprie recensioni.
     */
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
        String username = selected.getUsername();
        refreshData();
        if (refreshParentCallback != null) refreshParentCallback.run();
        tableView.getItems().stream()
            .filter(r -> r.getUsername().equals(username))
            .findFirst()
            .ifPresent(r -> tableView.getSelectionModel().select(r));
        pulisciCampi();
        notificaAggiornamentoRecensioni();
    }

    /**
     * Gestisce l'eliminazione della recensione selezionata.
     * <p>
     * Permette all'utente di eliminare solo le proprie recensioni.
     */
    @FXML
    private void handleElimina() {
        Recensione selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!selected.getUsername().equals(SessioneUtente.getUsernameUtente())) {
            mostraErrore("Errore", "Puoi eliminare solo le tue recensioni");
            return;
        }

        recensioneService.eliminaRecensione(selected.getUsername(), ristoranteId);
        refreshData();
        if (refreshParentCallback != null) refreshParentCallback.run();
        pulisciCampi();
        notificaAggiornamentoRecensioni();
    }

    /**
     * Gestisce l'invio di una risposta alla recensione selezionata.
     * <p>
     * Solo i ristoratori possono rispondere.
     */
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
        refreshData();
        if (refreshParentCallback != null) refreshParentCallback.run();
        rispostaTextArea.clear();
        notificaAggiornamentoRecensioni();
    }

    /**
     * Pulisce i campi di testo e resetta la selezione della tabella.
     */
    private void pulisciCampi() {
        recensioneTextArea.clear();
        rispostaTextArea.clear();
        stelleSlider.setValue(3);
        tableView.getSelectionModel().clearSelection();
    }

    /**
     * Mostra un alert di errore.
     *
     * @param titolo titolo dell'alert
     * @param messaggio contenuto dell'alert
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
