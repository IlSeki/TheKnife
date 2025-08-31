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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    private final GestioneRistorante gestioneRistorante = GestioneRistorante.getInstance();
    private final GestionePossessoRistorante ownershipService = GestionePossessoRistorante.getInstance();
    private final GestioneRecensioni gestioneRecensioni = GestioneRecensioni.getInstance();
    private Ristorante selectedRistorante;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestioneRistorante.initializeData();
        ownershipService.initialize();
        setupRistorantiTable();
        setupRecensioniList();
        detailsContainer.setVisible(false);
        loadRistoranti();
    }


    public void refreshData() {
        System.out.println("Debug: Inizio refreshData");

        // Forza il refresh completo di entrambi i servizi
        gestioneRistorante.forceRefresh();
        ownershipService.refreshOwnershipData();

        // Ricarica i ristoranti nella tabella
        loadRistoranti();
        System.out.println("Debug: Ristoranti ricaricati");

        // Aggiorna i dettagli se c'è un ristorante selezionato
        if (selectedRistorante != null) {
            selectedRistorante = gestioneRistorante.getRistorante(selectedRistorante.getNome());
            if (selectedRistorante != null) {
                updateStatistiche();
                loadRecensioni();
                System.out.println("Debug: Statistiche e recensioni aggiornate");
            }
        }
    }

    private void setupRistorantiTable() {
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        indirizzoColumn.setCellValueFactory(new PropertyValueFactory<>("indirizzo"));
        localitaColumn.setCellValueFactory(new PropertyValueFactory<>("localita"));
        cucinaColumn.setCellValueFactory(new PropertyValueFactory<>("cucina"));
        prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));

        ristorantiTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> onRistoranteSelected(newValue));
    }

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

    private void loadRecensioni() {
        if (selectedRistorante == null) return;

        List<Recensione> recensioni = gestioneRecensioni.getRecensioniRistorante(selectedRistorante.getNome());

        recensioni.sort((r1, r2) -> r2.getData().compareTo(r1.getData()));
        if (recensioni.size() > 5) {
            recensioni = recensioni.subList(0, 5);
        }

        recensioniList.setItems(FXCollections.observableArrayList(recensioni));
    }

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

        // Precompila con la risposta esistente se presente
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

                // Mostra conferma
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


    @FXML
    private void onAggiungiRistoranteClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ristorante-input.fxml"));
            Parent root = loader.load();

            RistoranteInputController controller = loader.getController();
            Scene currentScene = ristorantiTable.getScene();
            Parent dashboardRoot = currentScene.getRoot();

            // Creiamo una reference alla dashboard per il refresh
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
            stage.setTitle("Aggiungi ristorante");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Errore nell'apertura della finestra per l'aggiunta di un ristorante", e);
        }
    }

    @FXML
    private void onAggiornaClick(ActionEvent event) {
        refreshData();
    }

    public void onRecensioneUpdated() {
        updateStatistiche();
        loadRecensioni();
    }

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

    private void showError(String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}