package com.example.theknife;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Controller per la visualizzazione dettagliata di un ristorante.
 * Gestisce l'interfaccia grafica per mostrare tutte le informazioni di un singolo ristorante
 * caricato da file CSV con un design moderno e accattivante.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 2.1
 * @since 2025-05-27
 */
public class RistoranteDetailController implements Initializable {

    @FXML private Label nomeLabel;
    @FXML private Label indirizzoLabel;
    @FXML private Label localitaLabel;
    @FXML private Label prezzoLabel;
    @FXML private Label cucinaLabel;
    @FXML private Label telefonoLabel;
    @FXML private Hyperlink sitoWebLink;
    @FXML private Label premioLabel;
    @FXML private ImageView stellaVerdeIcon;
    @FXML private Label stellaVerdeLabel;
    @FXML private TextArea serviziTextArea;
    @FXML private TextArea descrizioneTextArea;
    @FXML private HBox prezzoContainer;
    @FXML private HBox premioContainer;
    @FXML private VBox stellaVerdeContainer;
    @FXML private ImageView ristoranteImage;
    @FXML private Circle prezzoCircle1;
    @FXML private Circle prezzoCircle2;
    @FXML private Circle prezzoCircle3;
    @FXML private Circle prezzoCircle4;

    private Ristorante ristorante;
    private HostServices hostServices;
    private static final String CSV_FILE_PATH = "/data/michelin_my_maps.csv";
    private static List<Map<String, String>> csvData = new ArrayList<>();
    private static boolean csvLoaded = false;

    /**
     * Imposta i servizi host per aprire link esterni
     * @param hostServices servizi host dell'applicazione
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inizializzazione dei componenti
        setupTextAreas();
        setupPriceCircles();

        // Carica i dati CSV se non già caricati
        if (!csvLoaded) {
            loadCSVData();
        }
    }

    /**
     * Carica i dati dal file CSV
     */
    private void loadCSVData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    csvData.clear();

                    // Prova prima a caricare dalle risorse
                    InputStream inputStream = getClass().getResourceAsStream(CSV_FILE_PATH);

                    // Se non trovato nelle risorse, prova nel percorso del progetto
                    if (inputStream == null) {
                        File csvFile = new File(CSV_FILE_PATH);
                        if (csvFile.exists()) {
                            inputStream = new FileInputStream(csvFile);
                        } else {
                            throw new FileNotFoundException("File CSV non trovato: " + CSV_FILE_PATH);
                        }
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String line;
                        String[] headers = null;
                        boolean isFirstLine = true;

                        while ((line = reader.readLine()) != null) {
                            if (isFirstLine) {
                                headers = parseCSVLine(line);
                                isFirstLine = false;
                                continue;
                            }

                            String[] values = parseCSVLine(line);
                            if (values.length >= headers.length) {
                                Map<String, String> record = new HashMap<>();
                                for (int i = 0; i < headers.length && i < values.length; i++) {
                                    record.put(headers[i].trim(), values[i].trim());
                                }
                                csvData.add(record);
                            }
                        }
                    }

                    csvLoaded = true;
                    Platform.runLater(() -> {
                        System.out.println("CSV caricato con successo. Numero di ristoranti: " + csvData.size());
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showError("Errore nel caricamento del CSV",
                                "Impossibile caricare il file " + CSV_FILE_PATH + ": " + e.getMessage());
                    });
                    throw e;
                }
                return null;
            }
        };

        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * Parsa una linea CSV gestendo le virgole all'interno delle virgolette
     * @param line linea da parsare
     * @return array di valori
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Doppia virgolette escaped
                    currentField.append('"');
                    i++; // Skip prossima virgoletta
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }

        result.add(currentField.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Carica un ristorante specifico dal CSV usando il nome
     * @param nomeRistorante nome del ristorante da cercare
     */
    public void loadRistoranteByName(String nomeRistorante) {
        if (!csvLoaded) {
            showError("CSV non caricato", "I dati CSV non sono ancora stati caricati. Riprovare.");
            return;
        }

        Optional<Map<String, String>> recordOpt = csvData.stream()
                .filter(record -> record.get("Name") != null &&
                        record.get("Name").equalsIgnoreCase(nomeRistorante))
                .findFirst();

        if (recordOpt.isPresent()) {
            Ristorante ristorante = createRistoranteFromRecord(recordOpt.get());
            setRistorante(ristorante);
        } else {
            showError("Ristorante non trovato", "Nessun ristorante trovato con il nome: " + nomeRistorante);
        }
    }

    /**
     * Carica un ristorante specifico dal CSV usando l'indice
     * @param index indice del ristorante nella lista
     */
    public void loadRistoranteByIndex(int index) {
        if (!csvLoaded) {
            showError("CSV non caricato", "I dati CSV non sono ancora stati caricati. Riprovare.");
            return;
        }

        if (index >= 0 && index < csvData.size()) {
            Ristorante ristorante = createRistoranteFromRecord(csvData.get(index));
            setRistorante(ristorante);
        } else {
            showError("Indice non valido", "Indice ristorante non valido: " + index);
        }
    }

    /**
     * Carica un ristorante casuale dal CSV
     */
    public void loadRandomRistorante() {
        if (!csvLoaded || csvData.isEmpty()) {
            showError("CSV non disponibile", "I dati CSV non sono disponibili o vuoti.");
            return;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(csvData.size());
        loadRistoranteByIndex(randomIndex);
    }

    /**
     * Restituisce la lista di tutti i nomi dei ristoranti
     * @return lista dei nomi
     */
    public List<String> getAllRistoranteNames() {
        if (!csvLoaded) return new ArrayList<>();

        return csvData.stream()
                .map(record -> record.get("Name"))
                .filter(Objects::nonNull)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Crea un oggetto Ristorante da un record CSV
     * @param record mappa con i dati del ristorante
     * @return oggetto Ristorante
     */
    private Ristorante createRistoranteFromRecord(Map<String, String> record) {
        // Estrai tutti i valori necessari per il costruttore
        String nome = getValueOrDefault(record, "Name", "Nome non disponibile");
        String indirizzo = getValueOrDefault(record, "Address", "Indirizzo non disponibile");
        String localita = getValueOrDefault(record, "Location", "Località non disponibile");
        String prezzo = getValueOrDefault(record, "Price", "€");
        String cucina = getValueOrDefault(record, "Cuisine", "Non specificata");
        String numeroTelefono = getValueOrDefault(record, "PhoneNumber", "");
        String url = getValueOrDefault(record, "Url", ""); // Campo Url dal CSV
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
            longitudine = 0.0; // Valore di default
        }

        try {
            String latitudeStr = record.get("Latitude");
            if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
                latitudine = Double.parseDouble(latitudeStr);
            }
        } catch (NumberFormatException e) {
            latitudine = 0.0; // Valore di default
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
    }

    /**
     * Restituisce il valore dal record o un valore di default
     * @param record mappa con i dati
     * @param key chiave da cercare
     * @param defaultValue valore di default
     * @return valore trovato o default
     */
    private String getValueOrDefault(Map<String, String> record, String key, String defaultValue) {
        String value = record.get(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    /**
     * Mostra un messaggio di errore
     * @param title titolo dell'errore
     * @param message messaggio di errore
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Configura le text area per una migliore visualizzazione
     */
    private void setupTextAreas() {
        if (serviziTextArea != null) {
            serviziTextArea.setWrapText(true);
            serviziTextArea.setEditable(false);
        }

        if (descrizioneTextArea != null) {
            descrizioneTextArea.setWrapText(true);
            descrizioneTextArea.setEditable(false);
        }
    }

    /**
     * Configura i cerchi per l'indicatore di prezzo
     */
    private void setupPriceCircles() {
        Circle[] circles = {prezzoCircle1, prezzoCircle2, prezzoCircle3, prezzoCircle4};
        for (Circle circle : circles) {
            if (circle != null) {
                circle.setFill(Color.LIGHTGRAY);
                circle.setStroke(Color.GRAY);
            }
        }
    }

    /**
     * Imposta il ristorante da visualizzare e aggiorna l'interfaccia
     * @param ristorante il ristorante da visualizzare
     */
    public void setRistorante(Ristorante ristorante) {
        this.ristorante = ristorante;
        updateUI();
    }

    /**
     * Aggiorna tutti i componenti dell'interfaccia con i dati del ristorante
     */
    private void updateUI() {
        if (ristorante == null) return;

        Platform.runLater(() -> {
            // Informazioni base
            nomeLabel.setText(ristorante.getNome());
            indirizzoLabel.setText(ristorante.getIndirizzo());
            localitaLabel.setText(ristorante.getLocalita());
            cucinaLabel.setText("Cucina " + ristorante.getCucina());

            // Telefono
            String telefono = ristorante.getNumeroTelefono();
            if (telefono != null && !telefono.trim().isEmpty()) {
                telefonoLabel.setText(telefono);
            } else {
                telefonoLabel.setText("Non disponibile");
            }

            // Sito web
            String sitoWeb = ristorante.getSitoWeb();
            if (sitoWeb != null && !sitoWeb.trim().isEmpty()) {
                sitoWebLink.setText("Visita il sito web");
                sitoWebLink.setVisible(true);
            } else {
                sitoWebLink.setVisible(false);
            }

            // Prezzo
            updatePrezzoDisplay();

            // Premio
            String premio = ristorante.getPremio();
            if (premio != null && !premio.trim().isEmpty()) {
                premioLabel.setText(premio);
                premioContainer.setVisible(true);
            } else {
                premioContainer.setVisible(false);
            }

            // Stella Verde - Correzione per l'elemento FXML
            updateStellaVerdeDisplay();

            // Servizi
            String servizi = ristorante.getServizi();
            if (servizi != null && !servizi.trim().isEmpty()) {
                serviziTextArea.setText(formatServizi(servizi));
            } else {
                serviziTextArea.setText("Nessun servizio specificato");
            }

            // Descrizione
            String descrizione = ristorante.getDescrizione();
            if (descrizione != null && !descrizione.trim().isEmpty()) {
                descrizioneTextArea.setText(descrizione);
            } else {
                descrizioneTextArea.setText("Nessuna descrizione disponibile");
            }

            // Immagine placeholder
            setPlaceholderImage();
        });
    }

    /**
     * Aggiorna la visualizzazione del prezzo con indicatori circolari
     */
    private void updatePrezzoDisplay() {
        String prezzo = ristorante.getPrezzo();
        prezzoLabel.setText(prezzo != null ? prezzo : "€");

        // Calcola il numero di cerchi da riempire basandosi sul prezzo
        int livelloPrezzo = calcolaLivelloPrezzo(prezzo);

        Circle[] circles = {prezzoCircle1, prezzoCircle2, prezzoCircle3, prezzoCircle4};
        for (int i = 0; i < circles.length; i++) {
            if (circles[i] != null) {
                if (i < livelloPrezzo) {
                    circles[i].setFill(Color.web("#FF6B35")); // Arancione per prezzo
                } else {
                    circles[i].setFill(Color.LIGHTGRAY);
                }
            }
        }
    }

    /**
     * Calcola il livello di prezzo basandosi sulla stringa del prezzo
     * @param prezzo stringa del prezzo
     * @return numero da 1 a 4 rappresentante il livello di prezzo
     */
    private int calcolaLivelloPrezzo(String prezzo) {
        if (prezzo == null || prezzo.trim().isEmpty()) return 1;

        // Conta il numero di simboli € nella stringa
        long count = prezzo.chars().filter(ch -> ch == '€').count();
        return Math.max(1, Math.min(4, (int) count));
    }

    /**
     * Aggiorna la visualizzazione della stella verde
     */
    private void updateStellaVerdeDisplay() {
        String stellaVerde = ristorante.getStellaVerde();
        if (stellaVerde != null && !stellaVerde.trim().isEmpty() &&
                !"No".equalsIgnoreCase(stellaVerde.trim())) {
            stellaVerdeLabel.setText("Stella Verde");

            // Trova il container della stella verde usando il lookup nell'FXML
            VBox container = (VBox) stellaVerdeLabel.getParent().getParent();
            if (container != null) {
                container.setVisible(true);
            }

            // Carica icona stella verde se disponibile
            try {
                Image starImage = new Image(getClass().getResourceAsStream("/icons/green_star.png"));
                if (stellaVerdeIcon != null) {
                    stellaVerdeIcon.setImage(starImage);
                }
            } catch (Exception e) {
                // Se l'immagine non è disponibile, nascondi l'icona
                if (stellaVerdeIcon != null) {
                    stellaVerdeIcon.setVisible(false);
                }
            }
        } else {
            // Trova il container della stella verde e nascondilo
            if (stellaVerdeLabel != null && stellaVerdeLabel.getParent() != null) {
                VBox container = (VBox) stellaVerdeLabel.getParent().getParent();
                if (container != null) {
                    container.setVisible(false);
                }
            }
        }
    }

    /**
     * Formatta la stringa dei servizi per una migliore leggibilità
     * @param servizi stringa grezza dei servizi
     * @return stringa formattata
     */
    private String formatServizi(String servizi) {
        if (servizi == null || servizi.trim().isEmpty()) {
            return "Nessun servizio specificato";
        }

        // Sostituisce virgole e punti e virgola con a capo per migliore leggibilità
        return servizi.replaceAll("[,;]", "\n• ").trim();
    }

    /**
     * Imposta un'immagine placeholder per il ristorante
     */
    private void setPlaceholderImage() {
        try {
            Image placeholder = new Image(getClass().getResourceAsStream("/images/restaurant_placeholder.jpg"));
            ristoranteImage.setImage(placeholder);
        } catch (Exception e) {
            // Se l'immagine placeholder non è disponibile, nasconde l'ImageView
            ristoranteImage.setVisible(false);
        }
    }

    /**
     * Gestisce il click sul link del sito web
     * @param event evento del click
     */
    @FXML
    private void handleSitoWebClick(ActionEvent event) {
        if (ristorante != null && ristorante.getSitoWeb() != null &&
                !ristorante.getSitoWeb().trim().isEmpty() && hostServices != null) {

            String url = ristorante.getSitoWeb().trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            try {
                hostServices.showDocument(url);
            } catch (Exception e) {
                System.err.println("Errore nell'apertura del sito web: " + e.getMessage());
            }
        }
    }

    /**
     * Gestisce il click sul numero di telefono (evento del mouse anziché Action)
     */
    @FXML
    private void handleTelefonoClick() {
        // Implementazione futura per chiamate dirette
        if (ristorante != null && ristorante.getNumeroTelefono() != null) {
            System.out.println("Chiamata a: " + ristorante.getNumeroTelefono());
        }
    }

    /**
     * Restituisce il ristorante attualmente visualizzato
     * @return il ristorante corrente
     */
    public Ristorante getRistorante() {
        return ristorante;
    }

    /**
     * Verifica se i dati CSV sono stati caricati
     * @return true se i dati sono caricati
     */
    public boolean isCSVLoaded() {
        return csvLoaded;
    }

    /**
     * Restituisce il numero totale di ristoranti nel CSV
     * @return numero di ristoranti
     */
    public int getTotalRistoranti() {
        return csvLoaded ? csvData.size() : 0;
    }
}