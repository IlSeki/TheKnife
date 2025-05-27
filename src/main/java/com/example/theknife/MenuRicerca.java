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
 * Controller per la gestione della ricerca dei ristoranti.
 * Permette di cercare ristoranti dal file CSV michelin_my_maps.csv
 * tramite un TextField che filtra dinamicamente i risultati e un MenuButton per ordinare.
 *
 * Inoltre, per ogni ristorante viene aggiunto un pulsante [info] che, una volta cliccato, ti porterà alla pagina
 * con le informazioni sul ristorante.
 *
 * @author Samuele
 * @version 2.0
 */
public class MenuRicerca implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Ristorante> resultList;

    @FXML
    private MenuButton filterMenu;

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
        // Carica i ristoranti dal file CSV
        caricaRistorantiDaCSV();

        // Crea la FilteredList utilizzando la lista principale
        ristorantiFiltrati = new FilteredList<>(tuttiRistoranti, ristorante -> true);

        // Crea la SortedList incapsulando la FilteredList
        sortedData = new SortedList<>(ristorantiFiltrati);

        // Imposta la ListView per mostrare la SortedList
        resultList.setItems(sortedData);

        // Imposta un CellFactory personalizzato per aggiungere il pulsante [info] in ogni cella
        resultList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Ristorante item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Crea una label con le informazioni del ristorante
                    Label infoLabel = new Label(item.getNome() + " - " + item.getLocalita() + " (" + item.getCucina() + ")");

                    // Crea il pulsante [info] e definisci l'azione da eseguire al click
                    Button infoButton = new Button("[info]");
                    infoButton.setOnAction(e -> openRestaurantInfo(item));

                    // Inserisci label e pulsante in un contenitore orizzontale
                    HBox container = new HBox(infoLabel, infoButton);
                    container.setSpacing(10);

                    setGraphic(container);
                }
            }
        });

        // Aggiunge il listener per la ricerca in tempo reale: modifica il predicato della FilteredList
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                filtraRistoranti(newValue);
            }
        });

        // Imposta gli handler per i MenuItem del MenuButton per l'ordinamento
        for (MenuItem item : filterMenu.getItems()) {
            item.setOnAction(event -> {
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
                // Aggiorna il testo del MenuButton per indicare il criterio di ordinamento selezionato
                filterMenu.setText(filterText);
            });
        }
    }

    /**
     * Metodo per aprire la pagina di informazioni sul ristorante.
     * Carica l'FXML della pagina dettagli e passa i dati del ristorante.
     *
     * @param ristorante il ristorante di cui visualizzare le informazioni.
     */
    private void openRestaurantInfo(Ristorante ristorante) {
        try {
            // Carica l'FXML della pagina dettagli
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ristorante-detail.fxml"));
            Parent root = loader.load();

            // Ottieni il controller della pagina dettagli
            RistoranteDetailController controller = loader.getController();

            // Passa i servizi host se disponibili
            if (hostServices != null) {
                controller.setHostServices(hostServices);
            }

            // Imposta il ristorante da visualizzare
            controller.setRistorante(ristorante);

            // Crea una nuova finestra per mostrare i dettagli
            Stage stage = new Stage();
            stage.setTitle("Dettagli Ristorante - " + ristorante.getNome());
            stage.setScene(new Scene(root));
            stage.show();

            System.out.println("Apertura info per: " + ristorante.getNome());

        } catch (IOException e) {
            System.err.println("Errore nel caricamento della pagina dettagli: " + e.getMessage());
            e.printStackTrace();
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
                aggiungiRistorantiEsempio();
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
            aggiungiRistorantiEsempio();
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
            ristorantiFiltrati.setPredicate(ristorante -> true);
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
     * Aggiunge alcuni ristoranti di esempio se il CSV non viene trovato.
     */
    private void aggiungiRistorantiEsempio() {
        tuttiRistoranti.addAll(
                new Ristorante("Osteria Francescana", "Via Stella 22", "Modena", "€€€€", "Italiana",
                        10.9258, 44.6478, "+39 059 210118", "", "https://osteriafrancescana.it",
                        "3 Stelle Michelin", "No", "WiFi, Parcheggio", "Ristorante stellato di Massimo Bottura"),
                new Ristorante("Le Bernardin", "155 West 51st Street", "New York", "€€€€", "Francese",
                        -73.9826, 40.7614, "+1 212-554-1515", "", "https://le-bernardin.com",
                        "3 Stelle Michelin", "No", "WiFi, Aria Condizionata", "Rinomato ristorante di pesce"),
                new Ristorante("Noma", "Refshalevej 96", "Copenhagen", "€€€€", "Nordica",
                        12.5989, 55.7005, "+45 32 96 32 97", "", "https://noma.dk",
                        "2 Stelle Michelin", "Sì", "WiFi, Vista mare", "Cucina nordica innovativa"),
                new Ristorante("El Celler de Can Roca", "Can Sunyer 48", "Girona", "€€€€", "Spagnola",
                        2.8237, 41.9794, "+34 972 22 21 57", "", "https://cellercanroca.com",
                        "3 Stelle Michelin", "No", "WiFi, Giardino", "Cucina catalana d'avanguardia"),
                new Ristorante("Eleven Madison Park", "11 Madison Avenue", "New York", "€€€€", "Americana",
                        -73.9876, 40.7420, "+1 212-889-0905", "", "https://elevenmadisonpark.com",
                        "3 Stelle Michelin", "Sì", "WiFi, Vista parco", "Cucina americana contemporanea")
        );
    }
}