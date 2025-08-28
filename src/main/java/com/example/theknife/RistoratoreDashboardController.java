package com.example.theknife;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller per la dashboard del ristoratore.
 * Gestisce l'interfaccia che permette ai ristoratori di visualizzare e gestire
 * i propri ristoranti, le recensioni e le statistiche.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistoratoreDashboardController implements Initializable {

    @FXML private TableView<Ristorante> ristorantiTable;
    @FXML private TableColumn<Ristorante, String> nomeColumn;
    @FXML private TableColumn<Ristorante, String> indirizzoColumn;
    @FXML private TableColumn<Ristorante, String> localitaColumn;
    @FXML private TableColumn<Ristorante, String> cucinaColumn;
    @FXML private TableColumn<Ristorante, String> prezzoColumn;

    @FXML private VBox detailsContainer;
    @FXML private Label nomeLabel;
    @FXML private Label indirizzoLabel;
    @FXML private Label cucinaLabel;
    @FXML private Label mediaLabel;
    @FXML private Label totaleRecensioniLabel;
    @FXML private PieChart recensioniChart;
    @FXML private ListView<Recensione> recensioniList;

    private final RistoranteService ristoranteService = RistoranteService.getInstance();
    private final RistoranteOwnershipService ownershipService = RistoranteOwnershipService.getInstance();
    private final RecensioneService recensioneService = RecensioneService.getInstance();
    private Ristorante selectedRistorante;

    /**
     * Inizializza il controller della dashboard configurando la tabella dei ristoranti
     * e la lista delle recensioni.
     *
     * @param location  L'URL di localizzazione della risorsa FXML (non utilizzato)
     * @param resources Le risorse aggiuntive per l'inizializzazione (non utilizzate)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRistorantiTable();
        setupRecensioniList();
        detailsContainer.setVisible(false);

        // Carica i dati una sola volta all'inizializzazione
        loadRistoranti();
    }

    /**
     * Aggiorna dettagli, grafici, ecc. se necessario
     * Chiamato esplicitamente quando serve refreshare i dati
     */
    public void refreshData() {
        loadRistoranti();
        // Se c'è un ristorante selezionato, aggiorna anche i suoi dettagli
        if (selectedRistorante != null) {
            updateStatistiche();
            loadRecensioni();
        }
    }

    /**
     * Configura le colonne della tabella dei ristoranti e imposta il listener
     * per la selezione delle righe.
     */
    private void setupRistorantiTable() {
        // Configura le colonne della tabella
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        indirizzoColumn.setCellValueFactory(new PropertyValueFactory<>("indirizzo"));
        localitaColumn.setCellValueFactory(new PropertyValueFactory<>("localita"));
        cucinaColumn.setCellValueFactory(new PropertyValueFactory<>("cucina"));
        prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));

        // Gestisci la selezione di una riga
        ristorantiTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> onRistoranteSelected(newValue));
    }

    /**
     * Configura la lista delle recensioni, definendo come deve essere visualizzata
     * ogni recensione e gestendo il doppio click per la risposta.
     */
    private void setupRecensioniList() {
        // Setup recensioni rimane invariato
        recensioniList.setCellFactory(__ -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Recensione item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String stars = "⭐".repeat(item.getStelle());
                    String text = String.format("%s - %s\n%s", stars, item.getUsername(), item.getTesto());
                    if (!item.getRisposta().isEmpty()) {
                        text += "\n↳ Risposta: " + item.getRisposta();
                    }
                    setText(text);
                    setWrapText(true);
                }
            }
        });

        // Aggiungo il gestore del click sulla recensione
        recensioniList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Doppio click
                Recensione selectedRecensione = recensioniList.getSelectionModel().getSelectedItem();
                if (selectedRecensione != null) {
                    mostraDialogoRisposta(selectedRecensione);
                }
            }
        });
    }

    /**
     * Carica i ristoranti di proprietà del ristoratore corrente.
     * Utilizza RistoranteOwnershipService per ottenere la lista dei ristoranti
     * posseduti e RistoranteService per caricare i dettagli di ogni ristorante.
     */
    private void loadRistoranti() {
        String currentUser = SessioneUtente.getUsernameUtente();
        if (currentUser == null || currentUser.isEmpty()) {
            System.err.println("Nessun utente loggato");
            return;
        }

        // Forza il ricaricamento dei dati di proprietà
        ownershipService.refreshOwnershipData();

        // Ottieni tutti i ristoranti di proprietà del ristoratore corrente
        List<String> ownedRestaurants = ownershipService.getOwnedRestaurants(currentUser);

        if (ownedRestaurants.isEmpty()) {
            // Debug ridotto - solo un messaggio informativo
            System.out.println("Nessun ristorante trovato per l'utente: " + currentUser);
            ristorantiTable.setItems(FXCollections.observableArrayList());
            detailsContainer.setVisible(false);
            return;
        }

        List<Ristorante> ristoranti = ownedRestaurants.stream()
                .map(nome -> {
                    Ristorante r = ristoranteService.getRistorante(nome);
                    if (r == null) {
                        System.err.println("Ristorante non trovato nel database: " + nome);
                    }
                    return r;
                })
                .filter(Objects::nonNull)
                .toList();

        ristorantiTable.setItems(FXCollections.observableArrayList(ristoranti));

        // Debug ridotto - solo un messaggio di conferma
        if (ristoranti.isEmpty()) {
            detailsContainer.setVisible(false);
            selectedRistorante = null;
            System.out.println("Nessun ristorante valido trovato per l'utente: " + currentUser);
        } else {
            System.out.println("Caricati " + ristoranti.size() + " ristoranti per l'utente " + currentUser);
        }
    }

    /**
     * Gestisce la selezione di un ristorante nella tabella.
     * Aggiorna il pannello dei dettagli con le informazioni del ristorante selezionato,
     * le sue statistiche e le recensioni recenti.
     *
     * @param ristorante Il ristorante selezionato
     */
    private void onRistoranteSelected(Ristorante ristorante) {
        if (ristorante == null) {
            detailsContainer.setVisible(false);
            return;
        }

        selectedRistorante = ristorante;
        detailsContainer.setVisible(true);

        nomeLabel.setText(ristorante.getNome());
        indirizzoLabel.setText(ristorante.getIndirizzo());
        cucinaLabel.setText(ristorante.getCucina());

        updateStatistiche();
        loadRecensioni();
    }

    /**
     * Aggiorna le statistiche visualizzate per il ristorante selezionato.
     * Calcola e mostra:
     * <ul>
     *   <li>Media delle stelle delle recensioni</li>
     *   <li>Numero totale di recensioni</li>
     *   <li>Distribuzione delle stelle in un grafico a torta</li>
     * </ul>
     */
    private void updateStatistiche() {
        if (selectedRistorante == null) return;

        List<Recensione> recensioni = recensioneService.getRecensioniRistorante(selectedRistorante.getNome());

        double media = recensioni.stream()
                .mapToInt(Recensione::getStelle)
                .average()
                .orElse(0.0);
        mediaLabel.setText(String.format("%.1f", media));

        totaleRecensioniLabel.setText(String.valueOf(recensioni.size()));

        Map<Integer, Integer> stelleCount = new HashMap<>();
        recensioni.forEach(r -> stelleCount.merge(r.getStelle(), 1, Integer::sum));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        stelleCount.forEach((stelle, count) ->
                pieChartData.add(new PieChart.Data(stelle + " ⭐", count)));
        recensioniChart.setData(pieChartData);
    }

    /**
     * Carica le recensioni più recenti per il ristorante selezionato.
     * Mostra le ultime 5 recensioni ordinate per data.
     */
    private void loadRecensioni() {
        if (selectedRistorante == null) return;

        List<Recensione> recensioni = recensioneService.getRecensioniRistorante(selectedRistorante.getNome());

        recensioni.sort((r1, r2) -> r2.getData().compareTo(r1.getData()));
        if (recensioni.size() > 5) {
            recensioni = recensioni.subList(0, 5);
        }

        recensioniList.setItems(FXCollections.observableArrayList(recensioni));
    }

    /**
     * Mostra una finestra di dialogo per rispondere a una recensione specifica.
     * Permette al ristoratore di visualizzare la recensione e inserire una risposta,
     * che verrà salvata e mostrata insieme alla recensione originale.
     *
     * @param recensione La recensione a cui rispondere
     */
    private void mostraDialogoRisposta(Recensione recensione) {
        // Crea una finestra di dialogo
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(recensioniList.getScene().getWindow());
        dialogStage.setTitle("Rispondi alla recensione");

        // Crea i controlli
        VBox dialogContent = new VBox(10);
        dialogContent.setStyle("-fx-padding: 10;");

        Label recensioneLabel = new Label(String.format("Recensione di %s (%s):\n%s",
                recensione.getUsername(), "⭐".repeat(recensione.getStelle()), recensione.getTesto()));
        recensioneLabel.setWrapText(true);

        javafx.scene.control.TextArea rispostaArea = new javafx.scene.control.TextArea();
        rispostaArea.setPromptText("Scrivi qui la tua risposta...");
        rispostaArea.setWrapText(true);
        if (!recensione.getRisposta().isEmpty()) {
            rispostaArea.setText(recensione.getRisposta());
        }

        javafx.scene.control.Button salvaButton = new javafx.scene.control.Button("Salva Risposta");
        salvaButton.setOnAction(e -> {
            String risposta = rispostaArea.getText().trim();
            if (!risposta.isEmpty()) {
                recensione.setRisposta(risposta);
                recensioneService.salvaRispostaRecensione(recensione);
                loadRecensioni(); // Aggiorna la lista delle recensioni
                dialogStage.close();
            }
        });

        javafx.scene.control.Button annullaButton = new javafx.scene.control.Button("Annulla");
        annullaButton.setOnAction(e -> dialogStage.close());

        javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(10);
        buttons.getChildren().addAll(salvaButton, annullaButton);
        buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        dialogContent.getChildren().addAll(recensioneLabel, rispostaArea, buttons);

        Scene dialogScene = new Scene(dialogContent, 400, 300);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    /**
     * Gestisce il click sul pulsante "Menu Ricerca".
     * Reindirizza l'utente alla schermata di ricerca ristoranti.
     *
     * @param event L'evento di click
     */
    @FXML
    private void onMenuRicercaClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("lista.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());

            // Usa la stessa finestra invece di crearne una nuova
            Stage stage = (Stage) ristorantiTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Errore durante l'apertura del menu di ricerca", e);
        }
    }

    /**
     * Gestisce il click sul pulsante "Aggiungi Ristorante".
     * Apre la schermata per l'inserimento di un nuovo ristorante.
     *
     * @param event L'evento di click
     */
    @FXML
    private void onAggiungiRistoranteClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ristorante-input.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Aggiungi ristorante");
            stage.setScene(new Scene(root));
            stage.show();

            // Refresh dei dati quando la finestra viene chiusa
            stage.setOnHiding(__ -> refreshData());

        } catch (IOException e) {
            showError("Errore durante l'apertura del form di inserimento", e);
        }
    }

    /**
     * Handler per il pulsante Aggiorna: forza il refresh dei dati dei ristoranti posseduti.
     */
    @FXML
    private void onAggiornaClick(ActionEvent event) {
        refreshData(); // Usa refreshData() invece di loadRistoranti() direttamente
    }

    /**
     * Gestisce l'evento dopo che una recensione è stata aggiornata.
     * Aggiorna le statistiche e ricarica le recensioni visualizzate.
     */
    public void onRecensioneUpdated() {
        updateStatistiche();
        loadRecensioni();
    }

    /**
     * Gestisce il click sul pulsante "Apri Recensioni".
     * Apre la finestra delle recensioni per il ristorante selezionato.
     *
     * @param event L'evento che ha scatenato l'azione
     */
    @FXML
    private void onApriRecensioniClick(ActionEvent event) {
        if (selectedRistorante == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setHeaderText(null);
            alert.setContentText("Seleziona prima un ristorante.");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("recensioni.fxml"));
            Parent recensioniRoot = loader.load();
            RecensioniController controller = loader.getController();
            controller.setRistoranteId(selectedRistorante.getNome());
            controller.setParentController(this);
            // Passa il root di ritorno
            Parent rootToRestore = ristorantiTable.getScene().getRoot();
            controller.setRootToRestore(rootToRestore);
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                Scene scene = recensioniRoot.getScene();
                scene.setRoot(rootToRestore);
                this.refreshData();
            });
            // Scene switch (finestra singola)
            Scene scene = ristorantiTable.getScene();
            scene.setRoot(recensioniRoot);
        } catch (IOException e) {
            showError("Errore nell'apertura della finestra delle recensioni", e);
        }
    }

    /**
     * Mostra una finestra di dialogo di errore con un titolo fisso ("Errore"),
     * un'intestazione personalizzata e il messaggio dell'eccezione.
     * <p>
     * Utile per segnalare all'utente errori imprevisti in modo chiaro e immediato.
     * </p>
     *
     * @param header testo da visualizzare come intestazione della finestra
     * @param e      eccezione da cui estrarre il messaggio da mostrare nel contenuto
     */
    private void showError(String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}