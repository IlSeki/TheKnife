package com.example.theknife;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.application.Platform;
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
import javafx.scene.control.Button;
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
 * <p>
 * Questa classe gestisce l'interfaccia utente dedicata ai ristoratori, consentendo loro
 * di visualizzare e gestire i ristoranti di loro proprietà, esaminare le recensioni
 * ricevute, rispondere ad esse e visualizzare statistiche di base. Fornisce inoltre
 * funzionalità per aggiungere nuovi ristoranti e navigare verso altre sezioni dell'applicazione.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistoratoreDashboardController implements Initializable {

    /**
     * La tabella che visualizza l'elenco dei ristoranti di proprietà del ristoratore.
     */
    @FXML private TableView<Ristorante> ristorantiTable;
    /**
     * Colonna della tabella per il nome del ristorante.
     */
    @FXML private TableColumn<Ristorante, String> nomeColumn;
    /**
     * Colonna della tabella per l'indirizzo del ristorante.
     */
    @FXML private TableColumn<Ristorante, String> indirizzoColumn;
    /**
     * Colonna della tabella per la località del ristorante.
     */
    @FXML private TableColumn<Ristorante, String> localitaColumn;
    /**
     * Colonna della tabella per il tipo di cucina.
     */
    @FXML private TableColumn<Ristorante, String> cucinaColumn;
    /**
     * Colonna della tabella per la fascia di prezzo.
     */
    @FXML private TableColumn<Ristorante, String> prezzoColumn;

    /**
     * Contenitore per i dettagli e le statistiche del ristorante selezionato.
     */
    @FXML private VBox detailsContainer;
    /**
     * Etichetta che mostra il nome del ristorante selezionato.
     */
    @FXML private Label nomeLabel;
    /**
     * Etichetta che mostra l'indirizzo del ristorante.
     */
    @FXML private Label indirizzoLabel;
    /**
     * Etichetta che mostra il tipo di cucina del ristorante.
     */
    @FXML private Label cucinaLabel;
    /**
     * Etichetta che mostra la media delle stelle delle recensioni.
     */
    @FXML private Label mediaLabel;
    /**
     * Etichetta che mostra il numero totale di recensioni.
     */
    @FXML private Label totaleRecensioniLabel;
    /**
     * Grafico a torta che visualizza la distribuzione delle recensioni per stelle.
     */
    @FXML private PieChart recensioniChart;
    /**
     * Lista che visualizza le recensioni più recenti.
     */
    @FXML private ListView<Recensione> recensioniList;

    private final GestioneRistorante gestioneRistorante = GestioneRistorante.getInstance();
    private final GestionePossessoRistorante ownershipService = GestionePossessoRistorante.getInstance();
    private final GestioneRecensioni gestioneRecensioni = GestioneRecensioni.getInstance();
    private Ristorante selectedRistorante;

    /**
     * Inizializza il controller della dashboard del ristoratore.
     * Questo metodo viene chiamato automaticamente dal framework JavaFX dopo che il file FXML è stato caricato.
     * Configura le tabelle, i listener e carica i dati iniziali per i ristoranti.
     *
     * @param location  L'URL utilizzato per risolvere percorsi relativi per l'oggetto root, o {@code null} se non noto.
     * @param resources Le risorse utilizzate per localizzare l'oggetto root, o {@code null} se non localizzato.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inizializza i dati dei servizi di gestione
        gestioneRistorante.initializeData();
        ownershipService.initialize();
        // Configura le viste (tabelle, liste)
        setupRistorantiTable();
        setupRecensioniList();
        // Nasconde il pannello dei dettagli finché un ristorante non viene selezionato
        detailsContainer.setVisible(false);
        // Carica i ristoranti di proprietà dell'utente
        loadRistoranti();
    }

    /**
     * Aggiorna tutti i dati della dashboard forzando il ricaricamento completo.
     * Questo metodo è utile per riflettere immediatamente le modifiche apportate
     * in altre sezioni dell'applicazione (es. dopo aver aggiunto un nuovo ristorante).
     * Ricarica la lista dei ristoranti e aggiorna le statistiche del ristorante selezionato, se presente.
     */
    public void refreshData() {
        System.out.println("Debug: Inizio refreshData");

        gestioneRistorante.forceRefresh();
        ownershipService.refreshOwnershipData();

        loadRistoranti();
        System.out.println("Debug: Ristoranti ricaricati");

        if (selectedRistorante != null) {
            selectedRistorante = gestioneRistorante.getRistorante(selectedRistorante.getNome());
            if (selectedRistorante != null) {
                updateStatistiche();
                loadRecensioni();
                System.out.println("Debug: Statistiche e recensioni aggiornate");
            }
        }
    }

    /**
     * Configura le colonne della tabella dei ristoranti e aggiunge un listener per
     * rilevare la selezione di un elemento.
     */
    private void setupRistorantiTable() {
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        indirizzoColumn.setCellValueFactory(new PropertyValueFactory<>("indirizzo"));
        localitaColumn.setCellValueFactory(new PropertyValueFactory<>("localita"));
        cucinaColumn.setCellValueFactory(new PropertyValueFactory<>("cucina"));
        prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));

        // Aggiunge un listener alla selezione della tabella
        ristorantiTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> onRistoranteSelected(newValue));
    }

    /**
     * Configura la lista delle recensioni, impostando un `cell factory` personalizzato
     * per la visualizzazione delle recensioni e un listener per il doppio click che apre
     * il dialogo di risposta.
     */
    private void setupRecensioniList() {
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

        recensioniList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Recensione selectedRecensione = recensioniList.getSelectionModel().getSelectedItem();
                if (selectedRecensione != null) {
                    mostraDialogoRisposta(selectedRecensione);
                }
            }
        });
    }

    /**
     * Carica i ristoranti di proprietà dell'utente corrente e li visualizza nella tabella.
     * Utilizza il servizio di gestione della proprietà per filtrare i ristoranti.
     */
    private void loadRistoranti() {
        String currentUser = SessioneUtente.getUsernameUtente();
        if (currentUser == null || currentUser.isEmpty()) {
            System.err.println("Nessun utente loggato");
            ristorantiTable.setItems(FXCollections.observableArrayList());
            return;
        }

        ristorantiTable.getItems().clear();

        List<String> ownedRestaurants = ownershipService.getOwnedRestaurants(currentUser);

        if (ownedRestaurants.isEmpty()) {
            System.out.println("Nessun ristorante trovato per l'utente: " + currentUser);
            ristorantiTable.setItems(FXCollections.observableArrayList());
            detailsContainer.setVisible(false);
            return;
        }

        List<Ristorante> ristoranti = ownedRestaurants.stream()
                .map(nome -> {
                    Ristorante r = gestioneRistorante.getRistorante(nome);
                    if (r == null) {
                        System.err.println("Ristorante non trovato nel database: " + nome);
                    }
                    return r;
                })
                .filter(Objects::nonNull)
                .toList();

        ristorantiTable.setItems(FXCollections.observableArrayList(ristoranti));

        if (ristoranti.isEmpty()) {
            detailsContainer.setVisible(false);
            selectedRistorante = null;
            System.out.println("Nessun ristorante valido trovato per l'utente: " + currentUser);
        } else {
            System.out.println("Caricati " + ristoranti.size() + " ristoranti per l'utente " + currentUser);
        }
    }

    /**
     * Gestisce l'evento di selezione di un ristorante dalla tabella.
     * Quando un ristorante viene selezionato, aggiorna il pannello dei dettagli con
     * le sue informazioni e statistiche.
     *
     * @param ristorante Il ristorante selezionato; {@code null} se la selezione viene annullata.
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
     * Calcola e aggiorna le statistiche del ristorante attualmente selezionato.
     * Le statistiche includono la media delle stelle, il numero totale di recensioni
     * e un grafico a torta che mostra la distribuzione delle valutazioni.
     */
    private void updateStatistiche() {
        if (selectedRistorante == null) return;

        List<Recensione> recensioni = gestioneRecensioni.getRecensioniRistorante(selectedRistorante.getNome());

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
     * Carica le recensioni più recenti del ristorante selezionato e le visualizza.
     * La lista è limitata alle 5 recensioni più recenti per mantenere l'interfaccia compatta.
     */
    private void loadRecensioni() {
        if (selectedRistorante == null) return;

        List<Recensione> recensioni = gestioneRecensioni.getRecensioniRistorante(selectedRistorante.getNome());

        recensioni.sort((r1, r2) -> r2.getData().compareTo(r1.getData()));
        if (recensioni.size() > 5) {
            recensioni = recensioni.subList(0, 5);
        }

        recensioniList.setItems(FXCollections.observableArrayList(recensioni));
    }

    /**
     * Apre un dialogo modale per consentire al ristoratore di scrivere una risposta a una recensione
     * o di modificarne una già esistente.
     *
     * @param recensione La recensione a cui il ristoratore vuole rispondere.
     */
    private void mostraDialogoRisposta(Recensione recensione) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(recensioniList.getScene().getWindow());

        boolean hasRisposta = !recensione.getRisposta().isEmpty();
        dialogStage.setTitle(hasRisposta ? "Modifica risposta alla recensione" : "Rispondi alla recensione");

        VBox dialogContent = new VBox(10);
        dialogContent.setStyle("-fx-padding: 10;");

        Label recensioneLabel = new Label(String.format("Recensione di %s (%s):\n%s",
                recensione.getUsername(), "⭐".repeat(recensione.getStelle()), recensione.getTesto()));
        recensioneLabel.setWrapText(true);

        javafx.scene.control.TextArea rispostaArea = new javafx.scene.control.TextArea();
        rispostaArea.setPromptText("Scrivi qui la tua risposta...");
        rispostaArea.setWrapText(true);

        if (hasRisposta) {
            rispostaArea.setText(recensione.getRisposta());
        }

        javafx.scene.control.Button salvaButton = new javafx.scene.control.Button(
                hasRisposta ? "Modifica Risposta" : "Salva Risposta");
        salvaButton.setOnAction(e -> {
            String risposta = rispostaArea.getText().trim();
            if (!risposta.isEmpty()) {
                recensione.setRisposta(risposta);
                gestioneRecensioni.salvaRispostaRecensione(recensione);
                loadRecensioni();
                dialogStage.close();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Successo");
                    alert.setHeaderText(null);
                    alert.setContentText(hasRisposta ? "Risposta modificata con successo!" : "Risposta salvata con successo!");
                    alert.showAndWait();
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText(null);
                alert.setContentText("Il testo della risposta non può essere vuoto.");
                alert.showAndWait();
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
     * Gestisce l'evento di click sul pulsante "Menu di ricerca".
     * Reindirizza l'utente alla schermata principale di ricerca dei ristoranti (lista.fxml).
     *
     * @param event L'evento di click del pulsante.
     */
    @FXML
    private void onMenuRicercaClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("lista.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());
            Stage stage = (Stage) ristorantiTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Errore durante l'apertura del menu di ricerca", e);
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Aggiungi Ristorante".
     * Apre la schermata per l'inserimento di un nuovo ristorante (ristorante-input.fxml).
     * Imposta un callback per tornare alla dashboard e aggiornare i dati dopo il salvataggio.
     *
     * @param event L'evento di click del pulsante.
     */
    @FXML
    private void onAggiungiRistoranteClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ristorante-input.fxml"));
            Parent root = loader.load();

            RistoranteInputController controller = loader.getController();
            Scene currentScene = ristorantiTable.getScene();
            Parent dashboardRoot = currentScene.getRoot();

            final RistoratoreDashboardController dashboard = this;

            controller.setAggiornaDatabaseRistorantiCallback(() -> {
                System.out.println("Debug: Inizia aggiornamento database");
                gestioneRistorante.initializeData();
            });

            controller.setTornaAllaDashboardCallback(() -> {
                System.out.println("Debug: Inizia ritorno alla dashboard");
                currentScene.setRoot(dashboardRoot);
                Platform.runLater(() -> {
                    System.out.println("Debug: Esecuzione refresh data");
                    dashboard.refreshData();
                });
            });

            currentScene.setRoot(root);
            Stage stage = (Stage) currentScene.getWindow();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore nell'apertura della finestra per l'aggiunta di un ristorante", e);
        }
    }

    /**
     * Callback utilizzato per aggiornare la dashboard quando una recensione viene modificata
     * dalla schermata di gestione completa delle recensioni.
     */
    public void onRecensioneUpdated() {
        updateStatistiche();
        loadRecensioni();
    }

    /**
     * Gestisce l'evento di click sul pulsante "Apri Recensioni".
     * Apre una nuova schermata per visualizzare tutte le recensioni del ristorante selezionato.
     *
     * @param event L'evento di click del pulsante.
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
            Parent rootToRestore = ristorantiTable.getScene().getRoot();
            controller.setRootToRestore(rootToRestore);
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                Scene scene = recensioniRoot.getScene();
                scene.setRoot(rootToRestore);
                this.refreshData();
            });
            Scene scene = ristorantiTable.getScene();
            scene.setRoot(recensioniRoot);
        } catch (IOException e) {
            showError("Errore nell'apertura della finestra delle recensioni", e);
        }
    }

    /**
     * Mostra una finestra di dialogo di errore con un messaggio personalizzato.
     *
     * @param header Il testo dell'intestazione dell'errore.
     * @param e      L'eccezione che ha causato l'errore, il cui messaggio sarà mostrato nel contenuto.
     */
    private void showError(String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}