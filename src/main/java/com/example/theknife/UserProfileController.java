package com.example.theknife;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

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

    private final GestioneRecensioni gestioneRecensioni = GestioneRecensioni.getInstance();
    private final GestionePreferiti gestionePreferiti = com.example.theknife.GestionePreferiti.getInstance();
    private final GestioneRistorante gestioneRistorante = GestioneRistorante.getInstance();

    /**
     * Aggiunge lo stylesheet CSS principale alla scena, se non già presente.
     *
     * @param scene scena JavaFX a cui applicare lo stile
     */
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
        List<Recensione> recensioniUtente = gestioneRecensioni.getRecensioniUtente(SessioneUtente.getUsernameUtente());
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

        // Setup pulsante torna al menu
        tornaalMenuButton.setOnAction(event -> handleTornaAlMenu());

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
    /**
     * Aggiorna la lista dei ristoranti preferiti dell'utente corrente.
     */
    private void aggiornaListaPreferiti() {
        preferitiList.setItems(FXCollections.observableArrayList(
            gestionePreferiti.getPreferiti(SessioneUtente.getUsernameUtente())
        ));
    }
    /**
     * Gestisce il click sul pulsante "Torna al menu principale".
     * Riporta l'utente alla schermata principale dell'applicazione.
     */
    @FXML
    private void handleTornaAlMenu() {
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
            System.err.println("Error loading main menu: " + e.getMessage());
            showError("Errore", "Impossibile tornare al menu principale: " + e.getMessage());
        }
    }

    /**
     * Gestisce l'operazione di logout dell'utente:
     * <ul>
     *   <li>Termina la sessione utente</li>
     *   <li>Mostra la schermata di login</li>
     * </ul>
     */
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

    /**
     * Apre i dettagli di un ristorante a partire dal suo nome.
     * Se il ristorante non è più disponibile, mostra un messaggio di errore.
     *
     * @param nomeRistorante identificativo o nome del ristorante da mostrare
     */
    private void openRistoranteDetail(String nomeRistorante) {
        System.out.println("DEBUG: Cercando ristorante con nome: '" + nomeRistorante + "'");

        // FORZARE IL CARICAMENTO DEI RISTORANTI
        // Questo potrebbe essere necessario se GestioneRistorante ha un lazy loading
        try {
            // Prova a chiamare un metodo che forza il caricamento
            gestioneRistorante.caricaRistoranti(); // Se questo metodo esiste
        } catch (Exception e) {
            System.out.println("DEBUG: Metodo caricaRistoranti() non disponibile: " + e.getMessage());
        }

        // Primo tentativo: usa il metodo diretto
        Ristorante ristorante = gestioneRistorante.getRistorante(nomeRistorante);
        System.out.println("DEBUG: getRistorante() returned: " + (ristorante != null ? ristorante.getNome() : "null"));

        // Se non trova il ristorante con il metodo diretto, prova con getRistorantiByNomi
        if (ristorante == null) {
            Set<String> nomiSet = new HashSet<>();
            nomiSet.add(nomeRistorante);
            List<Ristorante> ristoranti = gestioneRistorante.getRistorantiByNomi(nomiSet);
            System.out.println("DEBUG: getRistorantiByNomi() returned " + ristoranti.size() + " risultati");
            if (!ristoranti.isEmpty()) {
                ristorante = ristoranti.get(0);
                System.out.println("DEBUG: Trovato ristorante: " + ristorante.getNome());
            }
        }

        // Prova anche a cercare in tutti i ristoranti disponibili
        if (ristorante == null) {
            System.out.println("DEBUG: Tentativo di ricerca in tutti i ristoranti...");
            List<Ristorante> tuttiRistoranti = gestioneRistorante.getTuttiRistoranti();
            System.out.println("DEBUG: Totale ristoranti disponibili: " + tuttiRistoranti.size());

            if (tuttiRistoranti.isEmpty()) {
                // Se non ci sono ristoranti, proviamo approcci alternativi
                System.out.println("DEBUG: Nessun ristorante caricato, usando approccio alternativo...");

                // Usa lo stesso approccio del PreferitiController
                Set<String> preferiti = gestionePreferiti.getPreferiti(SessioneUtente.getUsernameUtente());
                System.out.println("DEBUG: Preferiti utente: " + preferiti);

                if (preferiti.contains(nomeRistorante)) {
                    List<Ristorante> ristoranti = gestioneRistorante.getRistorantiByNomi(preferiti);
                    System.out.println("DEBUG: getRistorantiByNomi con tutti i preferiti returned: " + ristoranti.size());

                    for (Ristorante r : ristoranti) {
                        System.out.println("DEBUG: Ristorante nei preferiti: '" + r.getNome() + "'");
                        if (r.getNome().equals(nomeRistorante)) {
                            ristorante = r;
                            System.out.println("DEBUG: Match trovato!");
                            break;
                        }
                    }
                }
            } else {
                for (Ristorante r : tuttiRistoranti) {
                    System.out.println("DEBUG: Ristorante disponibile: '" + r.getNome() + "'");
                    if (r.getNome().equals(nomeRistorante)) {
                        ristorante = r;
                        System.out.println("DEBUG: Trovato match esatto!");
                        break;
                    }
                }
            }
        }

        // Se ancora non lo trova, mostra errore con più dettagli
        if (ristorante == null) {
            String debugMessage = "Ristorante cercato: '" + nomeRistorante + "'\n";
            debugMessage += "Username corrente: " + SessioneUtente.getUsernameUtente() + "\n";
            debugMessage += "Preferiti dell'utente: " + gestionePreferiti.getPreferiti(SessioneUtente.getUsernameUtente());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore Debug");
            alert.setHeaderText("Ristorante non trovato");
            alert.setContentText(debugMessage);
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

            Stage currentStage = (Stage) nomeLabel.getScene().getWindow();

            // 🔹 Salvo la scena originale
            Scene originalScene = currentStage.getScene();

            // 🔹 Callback: ripristina la scena originale
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                System.out.println("DEBUG: Callback eseguita, torno al menu principale");
                currentStage.setScene(originalScene);
                currentStage.show();
                this.refreshData();
            });

            // 🔹 Creo e setto la nuova scena dei dettagli
            Scene newScene = new Scene(root);
            addStylesheet(newScene);
            currentStage.setScene(newScene);
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
        List<Recensione> recensioniUtente = gestioneRecensioni.getRecensioniUtente(SessioneUtente.getUsernameUtente());
        recensioniTable.setItems(FXCollections.observableArrayList(recensioniUtente));
        // Aggiorna preferiti
        aggiornaListaPreferiti();
    }
    /**
     * Mostra un messaggio di errore tramite finestra di dialogo.
     *
     * @param header  titolo dell'errore
     * @param content messaggio descrittivo dell'errore
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}