package com.example.theknife;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller per la gestione del profilo utente.
 * Gestisce l'interfaccia che mostra le informazioni dell'utente,
 * i suoi ristoranti preferiti e le sue recensioni.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */

public class UserProfileController implements Initializable {
    @FXML private Label nomeLabel;
    @FXML private Label ruoloLabel;
    @FXML private TableView<Recensione> recensioniTable;
    @FXML private TableColumn<Recensione, String> ristoranteColumn;
    @FXML private TableColumn<Recensione, Integer> stelleColumn;
    @FXML private TableColumn<Recensione, String> testoColumn;
    @FXML private TableColumn<Recensione, String> dataColumn;
    @FXML private VBox preferitiBox;
    @FXML private ListView<String> preferitiList;
    @FXML private Button logoutButton;
    @FXML private Button tornaalMenuButton;
    @FXML private Button dashboardButton;

    private final RecensioneService recensioneService = RecensioneService.getInstance();
    private final PreferenceService preferenceService = PreferenceService.getInstance();
    private final RistoranteService ristoranteService = RistoranteService.getInstance();

    private void addStylesheet(Scene scene) {
        try {
            String cssPath = getClass().getResource("/data/stile.css").toExternalForm();
            if (!scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            System.err.println("Impossibile caricare il CSS: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup informazioni utente
        nomeLabel.setText(SessioneUtente.getNomeCompleto());
        ruoloLabel.setText("Ruolo: " + SessioneUtente.getRuolo());

        // Setup colonne tabella recensioni
        ristoranteColumn.setCellValueFactory(new PropertyValueFactory<>("ristoranteId"));
        stelleColumn.setCellValueFactory(new PropertyValueFactory<>("stelle"));
        testoColumn.setCellValueFactory(new PropertyValueFactory<>("testo"));
        dataColumn.setCellValueFactory(new PropertyValueFactory<>("data"));

        // Carica recensioni utente
        List<Recensione> recensioniUtente = recensioneService.getRecensioniUtente(SessioneUtente.getUsernameUtente());
        recensioniTable.setItems(FXCollections.observableArrayList(recensioniUtente));

        // Configura vista in base al ruolo
        boolean isCliente = SessioneUtente.isCliente();
        boolean isRistoratore = SessioneUtente.isRistoratore();

        preferitiBox.setVisible(isCliente);
        dashboardButton.setVisible(isRistoratore);

        if (isCliente) {
            aggiornaListaPreferiti();
        } 

        // Setup pulsante logout
        logoutButton.setOnAction(event -> handleLogout());

        // Configura il click sui preferiti
        preferitiList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedRistoranteId = preferitiList.getSelectionModel().getSelectedItem();
                if (selectedRistoranteId != null) {
                    openRistoranteDetail(selectedRistoranteId);
                }
            }
        });

        // Configura il click sulle recensioni
        recensioniTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Recensione selectedRecensione = recensioniTable.getSelectionModel().getSelectedItem();
                if (selectedRecensione != null) {
                    openRistoranteDetail(selectedRecensione.getRistoranteId());
                }
            }
        });

        // Aggiungi pulsante per tornare al menu
        tornaalMenuButton.setOnAction(event -> {
            try {
                URL resourceUrl = getClass().getResource("/com/example/theknife/lista.fxml");
                if (resourceUrl == null) {
                    throw new IOException("FXML file not found: lista.fxml");
                }
                FXMLLoader loader = new FXMLLoader(resourceUrl);
                Parent root = loader.load();
                Stage currentStage = (Stage) tornaalMenuButton.getScene().getWindow();
                Scene scene = new Scene(root);
                addStylesheet(scene);
                currentStage.setScene(scene);
                currentStage.show();
            } catch (IOException e) {
                System.err.println("Error loading menu: " + e.getMessage());
                showError("Errore", "Impossibile tornare al menu principale");
            }
        });

        // Aggiungi handler per il pulsante dashboard
        dashboardButton.setOnAction(event -> {
            try {
                URL resourceUrl = getClass().getResource("/com/example/theknife/ristoratore-dashboard.fxml");
                if (resourceUrl == null) {
                    throw new IOException("FXML file not found: ristoratore-dashboard.fxml");
                }
                FXMLLoader loader = new FXMLLoader(resourceUrl);
                Parent root = loader.load();
                Stage currentStage = (Stage) dashboardButton.getScene().getWindow();
                Scene scene = new Scene(root);
                addStylesheet(scene);
                currentStage.setScene(scene);
                currentStage.show();
            } catch (IOException e) {
                System.err.println("Error loading dashboard: " + e.getMessage());
                showError("Errore", "Impossibile aprire la dashboard ristoratore: " + e.getMessage());
            }
        });
    }

    private void aggiornaListaPreferiti() {
        preferitiList.setItems(FXCollections.observableArrayList(
            preferenceService.getPreferiti(SessioneUtente.getUsernameUtente())
        ));
    }

    @FXML
    private void handleLogout() {
        SessioneUtente.eseguiLogout();
        try {
            URL resourceUrl = getClass().getResource("/com/example/theknife/login.fxml");
            if (resourceUrl == null) {
                throw new IOException("FXML file not found: login.fxml");
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            addStylesheet(scene);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            showError("Errore", "Impossibile tornare alla schermata di login");
        }
    }

    private void openRistoranteDetail(String nomeRistorante) {
        Ristorante ristorante = ristoranteService.getRistorante(nomeRistorante);
        if (ristorante == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Ristorante non trovato");
            alert.setContentText("Il ristorante selezionato non è più disponibile.");
            alert.showAndWait();
            return;
        }
        try {
            URL resourceUrl = getClass().getResource("/com/example/theknife/ristorante-detail.fxml");
            if (resourceUrl == null) {
                throw new IOException("FXML file not found: ristorante-detail.fxml");
            }
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            RistoranteDetailController controller = loader.getController();
            controller.setRistorante(ristorante);
            // Passa la callback di refresh per aggiornare il profilo al ritorno
            Parent rootToRestore = nomeLabel.getScene().getRoot();
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                Scene scene = root.getScene();
                scene.setRoot(rootToRestore);
                this.refreshData();
            });
            Stage currentStage = (Stage) nomeLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            addStylesheet(scene);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Error loading restaurant details: " + e.getMessage());
            showError("Errore", "Impossibile aprire i dettagli del ristorante: " + e.getMessage());
        }
    }

    /**
     * Aggiorna dinamicamente la lista delle recensioni e dei preferiti utente.
     */
    public void refreshData() {
        // Aggiorna recensioni
        List<Recensione> recensioniUtente = recensioneService.getRecensioniUtente(SessioneUtente.getUsernameUtente());
        recensioniTable.setItems(FXCollections.observableArrayList(recensioniUtente));
        // Aggiorna preferiti
        aggiornaListaPreferiti();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}