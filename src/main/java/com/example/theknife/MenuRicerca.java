package com.example.theknife;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.application.HostServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Classe che gestisce il menu di ricerca avanzata dei ristoranti.
 * Permette di filtrare i ristoranti in base a vari criteri come cucina,
 * servizi offerti, premi e fascia di prezzo.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class MenuRicerca implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Ristorante> resultList;

    @FXML
    private MenuButton filterMenu;

    @FXML
    private Button preferitiButton; // Pulsante per i preferiti

    @FXML
    private Button dashboardButton; // Pulsante per la dashboard ristoratore

    @FXML
    private Button userMenuButton;

    // Lista principale di tutti i ristoranti
    private ObservableList<Ristorante> tuttiRistoranti;

    // Lista filtrata per la ricerca
    private FilteredList<Ristorante> ristorantiFiltrati;

    // Lista ordinata che incapsula la lista filtrata
    private SortedList<Ristorante> sortedData;

    // HostServices per aprire link esterni
    private HostServices hostServices;

    /**
     * Imposta i servizi host per aprire link esterni
     * @param hostServices servizi host dell'applicazione
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Inizializza il controller caricando i dati dal CSV, impostando il filtro di ricerca ed
     * configurando gli eventi per il MenuButton che gestisce l'ordinamento.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Aggiorna l'interfaccia utente in base al ruolo dell'utente
        preferitiButton.setVisible(SessioneUtente.isCliente());
        dashboardButton.setVisible(SessioneUtente.isRistoratore());

        // Carica i ristoranti dal file CSV
        caricaRistorantiDaCSV();

        // Crea la FilteredList utilizzando la lista principale
        ristorantiFiltrati = new FilteredList<>(tuttiRistoranti, _ -> true);

        // Crea la SortedList incapsulando la FilteredList
        sortedData = new SortedList<>(ristorantiFiltrati);

        // Imposta la ListView per mostrare la SortedList
        resultList.setItems(sortedData);

        // Imposta un CellFactory personalizzato per aggiungere il pulsante [info] in ogni cella
        updateListCell();

        // Aggiunge il listener per la ricerca in tempo reale: modifica il predicato della FilteredList
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                filtraRistoranti(newValue);
            }
        });

        // Imposta gli handler per i MenuItem del MenuButton per l'ordinamento
        handleMenuItems();
    }

    /**
     * Metodo per aprire la pagina di informazioni sul ristorante.
     * Carica l'FXML della pagina dettagli e passa i dati del ristorante.
     *
     * @param ristorante il ristorante di cui visualizzare le informazioni.
     */
    private void openRestaurantInfo(Ristorante ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/theknife/ristorante-detail.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("File ristorante-detail.fxml non trovato nel classpath");
            }
            Parent root = loader.load();

            RistoranteDetailController controller = loader.getController();
            controller.setHostServices(hostServices);
            controller.setRistorante(ristorante);
            
            // Crea e configura la finestra
            Stage stage = new Stage();
            stage.setTitle("TheKnife - " + ristorante.getNome());
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(searchField.getScene().getWindow());
            stage.setResizable(true);
            stage.setOnCloseRequest(event -> {
                // Aggiorna la lista quando si chiude la finestra
                filtraRistoranti(searchField.getText());
            });
            stage.show();

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
            mostraErrore("Errore", "Impossibile aprire i dettagli del ristorante: " + e.getMessage());
        }
    }

    /**
     * Carica i ristoranti dal file CSV michelin_my_maps.csv
     * utilizzando la classe Ristorante principale del progetto
     */
    private void caricaRistorantiDaCSV() {
        tuttiRistoranti = FXCollections.observableArrayList();

        try {
            // Carica il file CSV dal classpath
            InputStream inputStream = getClass().getResourceAsStream("/data/michelin_my_maps.csv");
            if (inputStream == null) {
                System.err.println("File michelin_my_maps.csv non trovato in /data/");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String[] headers = null;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    headers = parseCSVLine(line);
                    isFirstLine = false;
                    continue;
                }

                // Parsing della riga CSV usando la classe Ristorante principale
                Ristorante ristorante = parseRigaCSV(line, headers);
                if (ristorante != null) {
                    tuttiRistoranti.add(ristorante);
                }
            }

            reader.close();
            System.out.println("Caricati " + tuttiRistoranti.size() + " ristoranti dal CSV");

        } catch (IOException e) {
            System.err.println("Errore nel caricamento del file CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parsing di una riga del CSV per creare un oggetto Ristorante utilizzando
     * la classe Ristorante principale del progetto.
     */
    private Ristorante parseRigaCSV(String line, String[] headers) {
        try {
            String[] values = parseCSVLine(line);

            if (values.length < headers.length) {
                return null;
            }

            // Crea una mappa per facilitare l'accesso ai valori
            Map<String, String> record = new HashMap<>();
            for (int i = 0; i < headers.length && i < values.length; i++) {
                record.put(headers[i].trim(), values[i].trim());
            }

            // Estrai tutti i valori necessari per il costruttore
            String nome = getValueOrDefault(record, "Name", "Nome non disponibile");
            String indirizzo = getValueOrDefault(record, "Address", "Indirizzo non disponibile");
            String localita = getValueOrDefault(record, "Location", "Località non disponibile");
            String prezzo = getValueOrDefault(record, "Price", "€");
            String cucina = getValueOrDefault(record, "Cuisine", "Non specificata");
            String numeroTelefono = getValueOrDefault(record, "PhoneNumber", "");
            String url = getValueOrDefault(record, "Url", "");
            String sitoWeb = getValueOrDefault(record, "WebsiteUrl", "");
            String premio = getValueOrDefault(record, "Award", "");
            String stellaVerde = getValueOrDefault(record, "GreenStar", "No");
            String servizi = getValueOrDefault(record, "FacilitiesAndServices", "");
            String descrizione = getValueOrDefault(record, "Description", "");

            // Gestione coordinate con valori di default
            double longitudine = 0.0;
            double latitudine = 0.0;

            try {
                String longitudeStr = record.get("Longitude");
                if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
                    longitudine = Double.parseDouble(longitudeStr);
                }
            } catch (NumberFormatException e) {
                longitudine = 0.0;
            }

            try {
                String latitudeStr = record.get("Latitude");
                if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
                    latitudine = Double.parseDouble(latitudeStr);
                }
            } catch (NumberFormatException e) {
                latitudine = 0.0;
            }

            // Crea l'oggetto Ristorante usando il costruttore parametrizzato
            return new Ristorante(
                    nome,
                    indirizzo,
                    localita,
                    prezzo,
                    cucina,
                    longitudine,
                    latitudine,
                    numeroTelefono,
                    url,
                    sitoWeb,
                    premio,
                    stellaVerde,
                    servizi,
                    descrizione
            );

        } catch (Exception e) {
            System.err.println("Errore nel parsing della riga: " + line);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Restituisce il valore dal record o un valore di default
     */
    private String getValueOrDefault(Map<String, String> record, String key, String defaultValue) {
        String value = record.get(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    /**
     * Parsing più robusto per gestire CSV con virgolette.
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        result.add(currentField.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Filtra i ristoranti in base al testo inserito nel campo di ricerca.
     */
    private void filtraRistoranti(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            ristorantiFiltrati.setPredicate(_ -> true);
        } else {
            String filtroLower = filtro.toLowerCase().trim();
            ristorantiFiltrati.setPredicate(ristorante ->
                    ristorante.getNome().toLowerCase().contains(filtroLower) ||
                    ristorante.getLocalita().toLowerCase().contains(filtroLower) ||
                    ristorante.getCucina().toLowerCase().contains(filtroLower) ||
                    ristorante.getIndirizzo().toLowerCase().contains(filtroLower)
            );
        }
    }

    /**
     * Metodo per gestire la navigazione ai preferiti.
     * Carica l'FXML della pagina preferiti e apre una nuova finestra.
     */
    @FXML
    private void handlePreferiti() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/theknife/preferiti.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("File preferiti.fxml non trovato nel classpath");
            }
            Parent root = loader.load();

            PreferitiController controller = loader.getController();
            if (hostServices != null) {
                controller.setHostServices(hostServices);
            }

            Stage stage = new Stage();
            stage.setTitle("I Miei Preferiti");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(preferitiButton.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            System.err.println("Errore nel caricamento della vista preferiti: " + e.getMessage());
            e.printStackTrace();
            mostraErrore("Errore", "Impossibile aprire la vista preferiti: " + e.getMessage());
        }
    }

    /**
     * Metodo per gestire la navigazione alla dashboard del ristoratore.
     * Carica l'FXML della dashboard e apre una nuova finestra.
     */
    @FXML
    private void handleRistoratoreDashboard() {
        if (!SessioneUtente.isRistoratore()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Accesso Negato");
            alert.setHeaderText(null);
            alert.setContentText("Solo i ristoratori possono accedere a questa funzionalità.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/theknife/ristoratore-dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("File ristoratore-dashboard.fxml non trovato nel classpath");
            }
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dashboard Ristoratore");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(dashboardButton.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            System.err.println("Errore nel caricamento della dashboard: " + e.getMessage());
            e.printStackTrace();
            mostraErrore("Errore", "Impossibile aprire la dashboard del ristoratore: " + e.getMessage());
        }
    }

    /**
     * Metodo per gestire il menu utente.
     * Carica l'FXML della pagina profilo utente o login a seconda dello stato di autenticazione.
     */
    @FXML
    private void handleUserMenu() {
        try {
            String resource = SessioneUtente.isUtenteLoggato() ? "user-profile.fxml" : "login.fxml";
            String title = SessioneUtente.isUtenteLoggato() ? "Il Tuo Profilo" : "Accedi";
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            if (loader.getLocation() == null) {
                throw new IOException("File " + resource + " non trovato nel classpath");
            }
            Parent root = loader.load();
            
            // Usa la finestra esistente invece di crearne una nuova
            Stage currentStage = (Stage) userMenuButton.getScene().getWindow();
            currentStage.setTitle(title);
            currentStage.setScene(new Scene(root));
            
            // Se è la finestra di login, imposta il callback
            if (!SessioneUtente.isUtenteLoggato()) {
                LoginController controller = loader.getController();
                controller.setOnLoginSuccess(() -> {
                    // Aggiorna l'interfaccia dopo il login
                    preferitiButton.setVisible(SessioneUtente.isCliente());
                    dashboardButton.setVisible(SessioneUtente.isRistoratore());
                    currentStage.setTitle("TheKnife - Menu (" + SessioneUtente.getUsernameUtente() + ")");
                });
            }
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
            e.printStackTrace();
            mostraErrore("Errore", "Impossibile aprire la finestra: " + e.getMessage());
        }
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private void updateListCell() {
        resultList.setCellFactory(_ -> new ListCell<>() {
            private final Button infoButton = new Button("[info]");
            private final HBox container = new HBox();
            private final Label infoLabel = new Label();
            
            {
                container.setSpacing(10);
                infoButton.setOnAction(event -> {
                    Ristorante item = getItem();
                    if (item != null) {
                        openRestaurantInfo(item);
                    }
                });
            }

            @Override
            protected void updateItem(Ristorante item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    infoLabel.setText(item.getNome() + " - " + item.getLocalita() + " (" + item.getCucina() + ")");
                    container.getChildren().setAll(infoLabel, infoButton);
                    setGraphic(container);
                }
            }
        });
    }

    private void handleMenuItems() {
        for (MenuItem item : filterMenu.getItems()) {
            item.setOnAction(_ -> {
                String filterText = item.getText();
                switch (filterText) {
                    case "Per Nome":
                        sortedData.setComparator((r1, r2) -> r1.getNome().compareToIgnoreCase(r2.getNome()));
                        break;
                    case "Per Città":
                        sortedData.setComparator((r1, r2) -> r1.getLocalita().compareToIgnoreCase(r2.getLocalita()));
                        break;
                    case "Per Tipo Cucina":
                        sortedData.setComparator((r1, r2) -> r1.getCucina().compareToIgnoreCase(r2.getCucina()));
                        break;
                    default:
                        sortedData.setComparator(null);
                        break;
                }
                filterMenu.setText(filterText);
            });
        }
    }

    private void openRistoranteDetail(Ristorante ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/theknife/ristorante-detail.fxml"));
            Parent root = loader.load();
            RistoranteDetailController controller = loader.getController();
            controller.setRistorante(ristorante);
            // Passa la callback di refresh per aggiornare la ricerca al ritorno
            Parent rootToRestore = resultList.getScene().getRoot();
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                Scene scene = root.getScene();
                scene.setRoot(rootToRestore);
                this.refreshData();
            });
            Stage currentStage = (Stage) resultList.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            showError("Errore", "Impossibile aprire i dettagli del ristorante: " + e.getMessage());
        }
    }

    /**
     * Aggiorna dinamicamente la lista dei ristoranti filtrati.
     */
    public void refreshData() {
        // Ricarica i ristoranti dal servizio e aggiorna la lista
        tuttiRistoranti.setAll(RistoranteService.getInstance().getTuttiRistoranti());
        ristorantiFiltrati = new FilteredList<>(tuttiRistoranti, p -> true);
        sortedData = new SortedList<>(ristorantiFiltrati);
        resultList.setItems(sortedData);
    }
    
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}