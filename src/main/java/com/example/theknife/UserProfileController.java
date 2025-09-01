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
 * <p>
 * Questa classe gestisce l'interfaccia utente dedicata al profilo personale,
 * mostrando informazioni dell'utente, i ristoranti preferiti e le recensioni
 * scritte. Fornisce inoltre i meccanismi per il logout, la navigazione
 * verso altre schermate e l'accesso alla dashboard del ristoratore.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class UserProfileController implements Initializable {
    /**
     * Etichetta che visualizza il nome completo dell'utente.
     */
    @FXML private Label nomeLabel;
    /**
     * Etichetta che mostra il ruolo dell'utente.
     */
    @FXML private Label ruoloLabel;
    /**
     * Tabella per visualizzare le recensioni scritte dall'utente.
     */
    @FXML private TableView<Recensione> recensioniTable;
    /**
     * Colonna della tabella delle recensioni per il nome del ristorante.
     */
    @FXML private TableColumn<Recensione, String> ristoranteColumn;
    /**
     * Colonna della tabella delle recensioni per il numero di stelle.
     */
    @FXML private TableColumn<Recensione, Integer> stelleColumn;
    /**
     * Colonna della tabella delle recensioni per il testo della recensione.
     */
    @FXML private TableColumn<Recensione, String> testoColumn;
    /**
     * Colonna della tabella delle recensioni per la data della recensione.
     */
    @FXML private TableColumn<Recensione, String> dataColumn;
    /**
     * Contenitore per la sezione dei ristoranti preferiti, visibile solo per i clienti.
     */
    @FXML private VBox preferitiBox;
    /**
     * Lista per visualizzare i nomi dei ristoranti preferiti.
     */
    @FXML private ListView<String> preferitiList;
    /**
     * Pulsante per eseguire il logout dall'applicazione.
     */
    @FXML private Button logoutButton;
    /**
     * Pulsante per tornare alla schermata del menu principale.
     */
    @FXML private Button tornaalMenuButton;
    /**
     * Pulsante per accedere alla dashboard del ristoratore, visibile solo per gli utenti con ruolo "ristoratore".
     */
    @FXML private Button dashboardButton;

    private final GestioneRecensioni gestioneRecensioni = GestioneRecensioni.getInstance();
    private final GestionePreferiti gestionePreferiti = com.example.theknife.GestionePreferiti.getInstance();
    private final GestioneRistorante gestioneRistorante = GestioneRistorante.getInstance();

    /**
     * Applica il foglio di stile CSS principale alla scena per uniformare l'aspetto dell'interfaccia utente.
     * Questo metodo verifica prima se lo stile è già stato applicato per evitare duplicazioni.
     *
     * @param scene La scena JavaFX alla quale applicare lo stile.
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

    /**
     * Inizializza il controller dopo che il file FXML è stato caricato.
     * Questo metodo viene chiamato automaticamente dal framework JavaFX.
     * Configura le informazioni e le viste del profilo utente in base al ruolo dell'utente,
     * imposta i gestori degli eventi per i pulsanti e le tabelle, e carica i dati iniziali.
     *
     * @param location L'URL utilizzato per risolvere percorsi relativi per l'oggetto root, o {@code null} se non noto.
     * @param resources Le risorse utilizzate per localizzare l'oggetto root, o {@code null} se l'oggetto root non è stato localizzato.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Imposta le informazioni personali dell'utente nelle etichette.
        nomeLabel.setText(SessioneUtente.getNomeCompleto());
        ruoloLabel.setText("Ruolo: " + SessioneUtente.getRuolo());

        // Configura le colonne della tabella delle recensioni associandole ai campi della classe Recensione.
        ristoranteColumn.setCellValueFactory(new PropertyValueFactory<>("ristoranteId"));
        stelleColumn.setCellValueFactory(new PropertyValueFactory<>("stelle"));
        testoColumn.setCellValueFactory(new PropertyValueFactory<>("testo"));
        dataColumn.setCellValueFactory(new PropertyValueFactory<>("data"));

        // Disabilita il riordino delle colonne
        ristoranteColumn.setReorderable(false);
        stelleColumn.setReorderable(false);
        testoColumn.setReorderable(false);
        dataColumn.setReorderable(false);

        // Imposta la politica di ridimensionamento per riempire la larghezza della tabella
        recensioniTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Carica e visualizza le recensioni dell'utente corrente.
        List<Recensione> recensioniUtente = gestioneRecensioni.getRecensioniUtente(SessioneUtente.getUsernameUtente());
        recensioniTable.setItems(FXCollections.observableArrayList(recensioniUtente));

        // Gestisce la visibilità delle sezioni in base al ruolo dell'utente.
        boolean isCliente = SessioneUtente.isCliente();
        boolean isRistoratore = SessioneUtente.isRistoratore();

        preferitiBox.setVisible(isCliente);
        dashboardButton.setVisible(isRistoratore);

        if (isCliente) {
            aggiornaListaPreferiti();
        }

        // Imposta i gestori degli eventi per i pulsanti.
        logoutButton.setOnAction(event -> handleLogout());
        tornaalMenuButton.setOnAction(event -> handleTornaAlMenu());

        // Aggiunge un gestore per gli eventi di doppio click sulle liste.
        preferitiList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedRistoranteId = preferitiList.getSelectionModel().getSelectedItem();
                if (selectedRistoranteId != null) {
                    openRistoranteDetail(selectedRistoranteId);
                }
            }
        });

        recensioniTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Recensione selectedRecensione = recensioniTable.getSelectionModel().getSelectedItem();
                if (selectedRecensione != null) {
                    openRistoranteDetail(selectedRecensione.getRistoranteId());
                }
            }
        });

        // Configura l'azione per il pulsante Dashboard.
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
     * Recupera i preferiti dalla classe di gestione e aggiorna la {@link ListView}.
     */
    private void aggiornaListaPreferiti() {
        preferitiList.setItems(FXCollections.observableArrayList(
                gestionePreferiti.getPreferiti(SessioneUtente.getUsernameUtente())
        ));
    }

    /**
     * Gestisce l'evento di click sul pulsante "Torna al menu principale".
     * Riporta l'utente alla schermata iniziale di visualizzazione dei ristoranti ({@code lista.fxml}).
     *
     * @throws IOException se il file FXML della schermata principale non viene trovato.
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
     * Gestisce l'operazione di logout dell'utente.
     * Resetta la sessione utente e riporta l'applicazione alla schermata di login ({@code login.fxml}).
     *
     * @throws IOException se il file FXML della schermata di login non viene trovato.
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
     * Apre la schermata dei dettagli di un ristorante.
     * Questo metodo tenta di trovare il ristorante tramite diversi approcci,
     * partendo dal nome, per garantire la massima robustezza anche in caso
     * di mancato caricamento iniziale dei dati.
     *
     * @param nomeRistorante Il nome del ristorante da visualizzare.
     */
    private void openRistoranteDetail(String nomeRistorante) {
        System.out.println("DEBUG: Cercando ristorante con nome: '" + nomeRistorante + "'");

        try {
            // Tentativo 1: Forza il ricaricamento dei ristoranti (se il metodo è disponibile).
            gestioneRistorante.caricaRistoranti();
        } catch (Exception e) {
            System.out.println("DEBUG: Metodo caricaRistoranti() non disponibile: " + e.getMessage());
        }

        Ristorante ristorante = gestioneRistorante.getRistorante(nomeRistorante);

        // Tentativo 2: Se il primo tentativo fallisce, cerca in tutti i ristoranti caricati.
        if (ristorante == null) {
            System.out.println("DEBUG: getRistorante() ha restituito null. Ricerca in tutti i ristoranti...");
            List<Ristorante> tuttiRistoranti = gestioneRistorante.getTuttiRistoranti();
            System.out.println("DEBUG: Totale ristoranti disponibili: " + tuttiRistoranti.size());
            for (Ristorante r : tuttiRistoranti) {
                if (r.getNome().equals(nomeRistorante)) {
                    ristorante = r;
                    System.out.println("DEBUG: Trovato match esatto!");
                    break;
                }
            }
        }

        // Se il ristorante non viene trovato in nessun modo, mostra un errore e interrompe l'esecuzione.
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

        // Se il ristorante è stato trovato, carica e visualizza la schermata dei dettagli.
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
            Scene originalScene = currentStage.getScene();

            // Imposta un callback per tornare a questa schermata dopo la chiusura dei dettagli.
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                System.out.println("DEBUG: Callback eseguita, torno al profilo utente");
                currentStage.setScene(originalScene);
                currentStage.show();
                this.refreshData();
            });

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
     * Aggiorna le liste di recensioni e preferiti mostrate nel profilo.
     * Questo metodo è utile per riflettere le modifiche apportate dall'utente in altre parti dell'applicazione
     * (es. aggiunta di un preferito dalla schermata dei dettagli del ristorante).
     */
    public void refreshData() {
        // Aggiorna la tabella delle recensioni.
        List<Recensione> recensioniUtente = gestioneRecensioni.getRecensioniUtente(SessioneUtente.getUsernameUtente());
        recensioniTable.setItems(FXCollections.observableArrayList(recensioniUtente));

        // Aggiorna la lista dei preferiti.
        aggiornaListaPreferiti();
    }

    /**
     * Mostra una finestra di dialogo di errore all'utente.
     *
     * @param header Il titolo dell'errore.
     * @param content Il messaggio descrittivo dell'errore.
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}