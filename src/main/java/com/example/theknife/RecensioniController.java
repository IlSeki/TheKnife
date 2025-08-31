package com.example.theknife;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
 * <li>Visualizzare le recensioni in una TableView</li>
 * <li>Aggiungere, modificare o eliminare recensioni da parte degli utenti</li>
 * <li>Rispondere alle recensioni da parte dei ristoratori</li>
 * <li>Filtrare recensioni per numero di stelle</li>
 * <li>Mostrare un grafico a torta con la distribuzione delle stelle</li>
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

    private final GestioneRecensioni gestioneRecensioni = GestioneRecensioni.getInstance();
    private final GestionePossessoRistorante ownershipService = GestionePossessoRistorante.getInstance();
    private String ristoranteId;
    private ObservableList<Recensione> masterRecensioniList;
    private FilteredList<Recensione> filteredList;
    private RistoratoreDashboardController parentController;
    private Parent rootToRestore;
    private Runnable tornaAlMenuPrincipaleCallback;

    /**
     * Imposta il controller della dashboard del ristoratore come parent.
     *
     * @param controller il controller padre della dashboard
     */
    public void setParentController(RistoratoreDashboardController controller) {
        this.parentController = controller;
    }

    /**
     * Notifica il controller padre di eventuali aggiornamenti alle recensioni.
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

    /**
     * Inizializza il controller.
     */
    @FXML
    public void initialize() {
        setupUI();
        setupTable();
        setupListeners();
    }

    private void setupUI() {
        boolean isUtenteLoggato = SessioneUtente.isUtenteLoggato();
        boolean isRistoratore = SessioneUtente.isRistoratore();

        // Nasconde o mostra i bottoni in base al ruolo dell'utente
        inviaButton.setVisible(isUtenteLoggato && !isRistoratore);
        modificaButton.setVisible(false); // Inizialmente nascosto
        eliminaButton.setVisible(false); // Inizialmente nascosto
        rispondiButton.setVisible(isRistoratore);
        rispostaBox.setVisible(isRistoratore);

        // I campi di input per le recensioni sono visibili solo per i clienti
        recensioneTextArea.setVisible(isUtenteLoggato && !isRistoratore);
        stelleSlider.setVisible(isUtenteLoggato && !isRistoratore);
    }

    private void setupTable() {
        colStelle.setCellValueFactory(new PropertyValueFactory<>("stelle"));
        colTesto.setCellValueFactory(new PropertyValueFactory<>("testo"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colUtente.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRisposta.setCellValueFactory(new PropertyValueFactory<>("risposta"));

        masterRecensioniList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(masterRecensioniList, p -> true);
        tableView.setItems(filteredList);
    }

    private void setupListeners() {
        comboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));

        stelleSlider.setMin(1);
        stelleSlider.setMax(5);
        stelleSlider.setBlockIncrement(1);
        stelleSlider.setSnapToTicks(true);
        stelleSlider.setShowTickLabels(true);
        stelleSlider.setShowTickMarks(true);

        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filteredList.setPredicate(recensione -> recensione.getStelle() == newVal);
            } else {
                filteredList.setPredicate(recensione -> true);
            }
            aggiornaPieChart();
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isRistoratore = SessioneUtente.isRistoratore();
            if (newVal != null) {
                boolean isAutore = newVal.getUsername().equals(SessioneUtente.getUsernameUtente());

                // Visibilità per cliente
                modificaButton.setVisible(isAutore && !isRistoratore);
                eliminaButton.setVisible(isAutore && !isRistoratore);
                inviaButton.setVisible(false); // Invia non è possibile quando una recensione è selezionata

                // Visibilità per ristoratore
                rispondiButton.setVisible(isRistoratore && newVal.getRisposta().isEmpty());
                rispostaBox.setVisible(isRistoratore);

                if (isAutore) {
                    recensioneTextArea.setText(newVal.getTesto());
                    stelleSlider.setValue(newVal.getStelle());
                } else {
                    recensioneTextArea.clear();
                    stelleSlider.setValue(3);
                }
            } else {
                pulisciCampi();
                modificaButton.setVisible(false);
                eliminaButton.setVisible(false);
                rispondiButton.setVisible(false);
                inviaButton.setVisible(SessioneUtente.isUtenteLoggato() && !SessioneUtente.isRistoratore());
            }
        });
    }

    /**
     * Aggiorna la lista delle recensioni e il grafico a torta.
     */
    public void refreshData() {
        if (ristoranteId != null) {
            masterRecensioniList.setAll(gestioneRecensioni.getRecensioniRistorante(ristoranteId));
            aggiornaPieChart();
            pulisciCampi();
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
        if (tornaAlMenuPrincipaleCallback != null) {
            tornaAlMenuPrincipaleCallback.run();
        } else {
            // Fallback se il callback non è impostato
            if (rootToRestore != null) {
                Scene scene = pieChart.getScene();
                scene.setRoot(rootToRestore);
            }
        }
    }

    /**
     * Aggiorna il grafico a torta con il conteggio delle recensioni per ogni numero di stelle.
     */
    private void aggiornaPieChart() {
        pieChart.getData().clear();
        Map<Integer, Integer> recensioniMap = new HashMap<>();
        int totale = 0;

        // Conta le recensioni per ogni numero di stelle basandosi sulla lista filtrata
        for (Recensione r : filteredList) {
            recensioniMap.merge(r.getStelle(), 1, Integer::sum);
            totale++;
        }

        // Mostra sempre tutte le 5 quantità di stelle, anche se il conteggio è zero
        for (int stelle = 1; stelle <= 5; stelle++) {
            pieChart.getData().add(new PieChart.Data(stelle + " ⭐", recensioniMap.getOrDefault(stelle, 0)));
        }

        if (totaleRecensioniLabel != null) {
            totaleRecensioniLabel.setText("Totale recensioni: " + masterRecensioniList.size());
        }
    }

    /**
     * Gestisce l'invio di una nuova recensione.
     */
    @FXML
    private void handleInvia() {
        if (!SessioneUtente.isUtenteLoggato()) {
            mostraErrore("Accesso richiesto", "Per scrivere una recensione devi effettuare l'accesso.");
            return;
        }
        if (recensioneTextArea.getText().trim().isEmpty()) {
            mostraErrore("Errore", "Il testo della recensione non può essere vuoto.");
            return;
        }

        // Controllo se l'utente ha già recensito questo ristorante
        if (masterRecensioniList.stream().anyMatch(r -> Objects.equals(r.getUsername(), SessioneUtente.getUsernameUtente()))) {
            mostraErrore("Errore", "Hai già recensito questo ristorante. Puoi modificare o eliminare la tua recensione esistente.");
            return;
        }

        Recensione recensione = new Recensione(
                (int) stelleSlider.getValue(),
                recensioneTextArea.getText().trim(),
                ristoranteId,
                SessioneUtente.getUsernameUtente()
        );
        gestioneRecensioni.aggiungiRecensione(recensione);
        refreshData();
        notificaAggiornamentoRecensioni();
    }

    /**
     * Gestisce la modifica della recensione selezionata.
     */
    @FXML
    private void handleModifica() {
        Recensione selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraErrore("Selezione richiesta", "Seleziona una recensione da modificare.");
            return;
        }
        if (!Objects.equals(selected.getUsername(), SessioneUtente.getUsernameUtente())) {
            mostraErrore("Errore", "Puoi modificare solo le tue recensioni.");
            return;
        }

        gestioneRecensioni.modificaRecensione(
                selected.getUsername(),
                ristoranteId,
                recensioneTextArea.getText(),
                (int) stelleSlider.getValue()
        );
        refreshData();
        notificaAggiornamentoRecensioni();
    }

    /**
     * Gestisce l'eliminazione della recensione selezionata.
     */
    @FXML
    private void handleElimina() {
        Recensione selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostraErrore("Selezione richiesta", "Seleziona una recensione da eliminare.");
            return;
        }
        if (!Objects.equals(selected.getUsername(), SessioneUtente.getUsernameUtente())) {
            mostraErrore("Errore", "Puoi eliminare solo le tue recensioni.");
            return;
        }

        gestioneRecensioni.eliminaRecensione(selected.getUsername(), ristoranteId);
        refreshData();
        notificaAggiornamentoRecensioni();
    }

    /**
     * Gestisce l'invio di una risposta alla recensione selezionata.
     */
    @FXML
    private void handleRispondi() {
        Recensione selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null || !SessioneUtente.isRistoratore()) {
            mostraErrore("Errore", "Non puoi rispondere a questa recensione.");
            return;
        }
        if (rispostaTextArea.getText().trim().isEmpty()) {
            mostraErrore("Errore", "Il testo della risposta non può essere vuoto.");
            return;
        }

        selected.setRisposta(rispostaTextArea.getText().trim());
        gestioneRecensioni.salvaRispostaRecensione(selected);
        refreshData();
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
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}