package com.example.theknife;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller per la visualizzazione dei dettagli di un ristorante.
 * Gestisce l'interfaccia che mostra tutte le informazioni di un ristorante,
 * incluse le recensioni, e permette di aggiungere il ristorante ai preferiti
 * o di lasciare una recensione.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistoranteDetailController implements Initializable {

    @FXML private Label nomeLabel;
    @FXML private Label indirizzoLabel;
    @FXML private Label localitaLabel;
    @FXML private Label prezzoLabel;
    @FXML private Label cucinaLabel;
    @FXML private Label telefonoLabel;
    @FXML private Hyperlink sitoWebLink;
    @FXML private Button posizioneButton; // Nuovo bottone per Google Maps
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
    @FXML private Button preferitoButton;
    @FXML private ListView<Recensione> recensioniRecentList;
    @FXML private Button mostraRecensioniButton;
    @FXML private Button scriviRecensioneButton;

    private Ristorante ristorante;
    private HostServices hostServices;
    private final PreferenceService preferenceService = PreferenceService.getInstance();
    private final RecensioneService recensioneService = RecensioneService.getInstance();

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
        setupPosizioneButton();
        setupPreferitoButton();

        // Inizializza i componenti con valori di default
        initializeDefaultValues();

        // Setup recensioni list cell factory
        recensioniRecentList.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Recensione item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox(5);
                    container.getStyleClass().add("recensione-cell");

                    HBox headerBox = new HBox(10);
                    Label stelleLabel = new Label("⭐".repeat(item.getStelle()));
                    Label dataLabel = new Label(item.getData());
                    dataLabel.getStyleClass().add("data-label");
                    Label utenteLabel = new Label("da " + item.getUsername());
                    utenteLabel.getStyleClass().add("utente-label");
                    headerBox.getChildren().addAll(stelleLabel, dataLabel, utenteLabel);

                    Label testoLabel = new Label(item.getTesto());
                    testoLabel.setWrapText(true);

                    container.getChildren().addAll(headerBox, testoLabel);

                    if (!item.getRisposta().isEmpty()) {
                        VBox rispostaBox = new VBox(5);
                        rispostaBox.getStyleClass().add("risposta-box");
                        Label rispostaLabel = new Label("Risposta del ristoratore:");
                        rispostaLabel.getStyleClass().add("risposta-header");
                        Label rispostaTestoLabel = new Label(item.getRisposta());
                        rispostaTestoLabel.setWrapText(true);
                        rispostaBox.getChildren().addAll(rispostaLabel, rispostaTestoLabel);
                        container.getChildren().add(rispostaBox);
                    }

                    setGraphic(container);
                }
            }
        });

        // Show/hide write review button based on user role
        scriviRecensioneButton.setVisible(SessioneUtente.isCliente());
    }

    /**
     * Configura il bottone posizione
     */
    private void setupPosizioneButton() {
        if (posizioneButton != null) {
            // Imposta lo stile del bottone (opzionale, dipende dal tuo CSS)
            posizioneButton.getStyleClass().add("maps-button");
            // Imposta il testo del bottone
            posizioneButton.setText("📍 Vedi su Maps");
        }
    }

    /**
     * Configura il bottone preferito
     */
    private void setupPreferitoButton() {
        if (preferitoButton != null) {
            preferitoButton.setVisible(SessioneUtente.isCliente());
        }
    }

    /**
     * Inizializza i componenti con valori di default
     */
    private void initializeDefaultValues() {
        if (nomeLabel != null) nomeLabel.setText("Caricamento...");
        if (indirizzoLabel != null) indirizzoLabel.setText("Caricamento...");
        if (localitaLabel != null) localitaLabel.setText("Caricamento...");
        if (cucinaLabel != null) cucinaLabel.setText("Caricamento...");
        if (telefonoLabel != null) telefonoLabel.setText("Caricamento...");
        if (prezzoLabel != null) prezzoLabel.setText("€");
        if (serviziTextArea != null) serviziTextArea.setText("Caricamento servizi...");
        if (descrizioneTextArea != null) descrizioneTextArea.setText("Caricamento descrizione...");
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

        // Debug per verificare i dati ricevuti
        if (ristorante != null) {
            System.out.println("=== DEBUG RISTORANTE ===");
            System.out.println("Nome: " + ristorante.getNome());
            System.out.println("Indirizzo: " + ristorante.getIndirizzo());
            System.out.println("Localita: " + ristorante.getLocalita());
            System.out.println("Cucina: " + ristorante.getCucina());
            System.out.println("Prezzo: " + ristorante.getPrezzo());
            System.out.println("Telefono: " + ristorante.getNumeroTelefono());
            System.out.println("Sito Web: " + ristorante.getSitoWeb());
            System.out.println("Premio: " + ristorante.getPremio());
            System.out.println("Stella Verde: " + ristorante.getStellaVerde());
            System.out.println("Servizi: " + ristorante.getServizi());
            System.out.println("Descrizione: " + ristorante.getDescrizione());
            System.out.println("========================");
        }

        // Aggiorna lo stato del bottone preferiti
        if (preferitoButton != null && SessioneUtente.getUsernameUtente() != null) {
            boolean isPreferito = preferenceService.isPreferito(
                SessioneUtente.getUsernameUtente(),
                ristorante.getNome()
            );
            preferitoButton.setText(isPreferito ? "❤️ Rimuovi dai preferiti" : "🤍 Aggiungi ai preferiti");
        }

        updateUI();
        loadRecensioni(); // Load reviews after setting the restaurant
    }

    /**
     * Aggiorna tutti i componenti dell'interfaccia con i dati del ristorante
     */
    private void updateUI() {
        if (ristorante == null) {
            System.out.println("ERRORE: Ristorante è null!");
            return;
        }

        Platform.runLater(() -> {
            try {
                // Informazioni base
                if (nomeLabel != null) {
                    nomeLabel.setText(ristorante.getNome() != null ? ristorante.getNome() : "Nome non disponibile");
                }

                if (indirizzoLabel != null) {
                    indirizzoLabel.setText(ristorante.getIndirizzo() != null ? ristorante.getIndirizzo() : "Indirizzo non disponibile");
                }

                if (localitaLabel != null) {
                    localitaLabel.setText(ristorante.getLocalita() != null ? ristorante.getLocalita() : "Località non disponibile");
                }

                if (cucinaLabel != null) {
                    String cucina = ristorante.getCucina() != null ? ristorante.getCucina() : "Non specificata";
                    cucinaLabel.setText("Cucina " + cucina);
                }

                // Telefono
                if (telefonoLabel != null) {
                    String telefono = ristorante.getNumeroTelefono();
                    if (telefono != null && !telefono.trim().isEmpty()) {
                        telefonoLabel.setText(telefono);
                    } else {
                        telefonoLabel.setText("Non disponibile");
                    }
                }

                // Sito web
                if (sitoWebLink != null) {
                    String sitoWeb = ristorante.getSitoWeb();
                    if (sitoWeb != null && !sitoWeb.trim().isEmpty()) {
                        sitoWebLink.setText("Visita il sito web");
                        sitoWebLink.setVisible(true);
                    } else {
                        sitoWebLink.setVisible(false);
                    }
                }

                // Bottone posizione - aggiorna la visibilità
                if (posizioneButton != null) {
                    boolean hasLocation = hasValidLocation();
                    posizioneButton.setVisible(hasLocation);
                    posizioneButton.setDisable(!hasLocation);
                }

                // Prezzo
                updatePrezzoDisplay();

                // Premio
                if (premioLabel != null && premioContainer != null) {
                    String premio = ristorante.getPremio();
                    if (premio != null && !premio.trim().isEmpty()) {
                        premioLabel.setText(premio);
                        premioContainer.setVisible(true);
                    } else {
                        premioContainer.setVisible(false);
                    }
                }

                // Stella Verde
                updateStellaVerdeDisplay();

                // Servizi - CORREZIONE IMPORTANTE
                if (serviziTextArea != null) {
                    String servizi = ristorante.getServizi();
                    System.out.println("Servizi dal ristorante: '" + servizi + "'"); // Debug
                    if (servizi != null && !servizi.trim().isEmpty()) {
                        serviziTextArea.setText(formatServizi(servizi));
                    } else {
                        serviziTextArea.setText("Nessun servizio specificato");
                    }
                }

                // Descrizione - CORREZIONE IMPORTANTE
                if (descrizioneTextArea != null) {
                    String descrizione = ristorante.getDescrizione();
                    System.out.println("Descrizione dal ristorante: '" + descrizione + "'"); // Debug
                    if (descrizione != null && !descrizione.trim().isEmpty()) {
                        descrizioneTextArea.setText(descrizione);
                    } else {
                        descrizioneTextArea.setText("Nessuna descrizione disponibile");
                    }
                }

                // Immagine placeholder
                setPlaceholderImage();

                System.out.println("UI aggiornata con successo!");

            } catch (Exception e) {
                System.err.println("Errore durante l'aggiornamento dell'UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Verifica se il ristorante ha informazioni di posizione valide
     * @return true se ha almeno indirizzo o località
     */
    private boolean hasValidLocation() {
        if (ristorante == null) return false;

        String indirizzo = ristorante.getIndirizzo();
        String localita = ristorante.getLocalita();

        return (indirizzo != null && !indirizzo.trim().isEmpty()) ||
                (localita != null && !localita.trim().isEmpty());
    }

    /**
     * Costruisce l'indirizzo completo per Google Maps
     * @return stringa con l'indirizzo formattato per la ricerca
     */
    private String buildFullAddress() {
        StringBuilder address = new StringBuilder();

        // Aggiungi il nome del ristorante se disponibile
        String nome = ristorante.getNome();
        if (nome != null && !nome.trim().isEmpty()) {
            address.append(nome.trim()).append(", ");
        }

        // Aggiungi l'indirizzo se disponibile
        String indirizzo = ristorante.getIndirizzo();
        if (indirizzo != null && !indirizzo.trim().isEmpty()) {
            address.append(indirizzo.trim()).append(", ");
        }

        // Aggiungi la località se disponibile
        String localita = ristorante.getLocalita();
        if (localita != null && !localita.trim().isEmpty()) {
            address.append(localita.trim());
        }

        // Rimuovi eventuali virgole finali
        String result = address.toString().replaceAll(",\\s*$", "");

        System.out.println("Indirizzo costruito per Maps: " + result);
        return result;
    }

    /**
     * Gestisce il click sul bottone posizione - apre Google Maps
     */
    @FXML
    private void handlePosizioneClick() {
        if (ristorante == null || !hasValidLocation()) {
            System.out.println("Nessuna posizione disponibile");
            showAlert("Attenzione", "Nessuna informazione di posizione disponibile per questo ristorante.");
            return;
        }

        try {
            // Costruisci l'indirizzo completo
            String fullAddress = buildFullAddress();

            if (fullAddress.trim().isEmpty()) {
                showAlert("Attenzione", "Impossibile determinare la posizione del ristorante.");
                return;
            }

            // Codifica l'indirizzo per l'URL
            String encodedAddress = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8);

            // Costruisci l'URL di Google Maps
            String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + encodedAddress;

            System.out.println("Tentativo di apertura Google Maps: " + mapsUrl);

            // Apri l'URL usando gli stessi metodi del sito web
            openExternalUrl(mapsUrl);

        } catch (Exception e) {
            System.err.println("Errore nell'apertura di Google Maps: " + e.getMessage());
            e.printStackTrace();
            showAlert("Errore", "Impossibile aprire Google Maps. Errore: " + e.getMessage());
        }
    }

    /**
     * Apre un URL esterno usando diversi metodi come fallback
     * @param url URL da aprire
     */
    private void openExternalUrl(String url) {
        // Metodo 1: Prova con HostServices (JavaFX)
        if (hostServices != null) {
            try {
                hostServices.showDocument(url);
                System.out.println("URL aperto con HostServices");
                return;
            } catch (Exception e) {
                System.err.println("Errore con HostServices: " + e.getMessage());
            }
        }

        // Metodo 2: Fallback con Desktop (AWT)
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    System.out.println("URL aperto con Desktop");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Errore con Desktop: " + e.getMessage());
            }
        }

        // Metodo 3: ProcessBuilder per una gestione più sicura dei comandi
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("cmd", "/c", "start", url);
            } else if (os.contains("mac")) {
                processBuilder = new ProcessBuilder("open", url);
            } else if (os.contains("nix") || os.contains("nux")) {
                processBuilder = new ProcessBuilder("xdg-open", url);
            } else {
                throw new UnsupportedOperationException("Sistema operativo non supportato: " + os);
            }

            processBuilder.start();
            System.out.println("URL aperto con ProcessBuilder");
        } catch (Exception e) {
            System.err.println("Errore nell'apertura dell'URL: " + e.getMessage());
            showAlert("Errore", "Impossibile aprire l'URL. URL: " + url);
        }
    }

    /**
     * Aggiorna la visualizzazione del prezzo con indicatori circolari
     */
    private void updatePrezzoDisplay() {
        if (prezzoLabel == null) return;

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
     * Supporta diversi formati: €€€, $$$, £££, ecc.
     * @param prezzo stringa del prezzo
     * @return numero da 1 a 4 rappresentante il livello di prezzo
     */
    private int calcolaLivelloPrezzo(String prezzo) {
        if (prezzo == null || prezzo.trim().isEmpty()) return 1;

        String prezzoTrimmed = prezzo.trim();

        // Metodo 1: Conta i simboli di valuta comuni
        long euroCount = prezzoTrimmed.chars().filter(ch -> ch == '€').count();
        long dollarCount = prezzoTrimmed.chars().filter(ch -> ch == '$').count();
        long poundCount = prezzoTrimmed.chars().filter(ch -> ch == '£').count();
        long yenCount = prezzoTrimmed.chars().filter(ch -> ch == '¥').count();

        // Trova il massimo tra i simboli di valuta
        long maxCurrencyCount = Math.max(Math.max(euroCount, dollarCount),
                Math.max(poundCount, yenCount));

        if (maxCurrencyCount > 0) {
            return Math.max(1, Math.min(4, (int) maxCurrencyCount));
        }

        // Metodo 2: Se non ci sono simboli di valuta, cerca pattern ripetitivi
        // Esempi: "Alto", "Medio-Alto", "Basso", ecc.
        String prezzoLower = prezzoTrimmed.toLowerCase();

        // Pattern per prezzi testuali italiani
        if (prezzoLower.contains("molto alto") || prezzoLower.contains("lusso") ||
                prezzoLower.contains("premium") || prezzoLower.contains("esclusivo")) {
            return 4;
        } else if (prezzoLower.contains("alto") || prezzoLower.contains("caro") ||
                prezzoLower.contains("costoso")) {
            return 3;
        } else if (prezzoLower.contains("medio") || prezzoLower.contains("moderato") ||
                prezzoLower.contains("ragionevole")) {
            return 2;
        } else if (prezzoLower.contains("basso") || prezzoLower.contains("economico") ||
                prezzoLower.contains("conveniente")) {
            return 1;
        }

        // Metodo 3: Pattern per prezzi testuali inglesi
        if (prezzoLower.contains("very expensive") || prezzoLower.contains("luxury") ||
                prezzoLower.contains("fine dining")) {
            return 4;
        } else if (prezzoLower.contains("expensive") || prezzoLower.contains("upscale") ||
                prezzoLower.contains("high-end")) {
            return 3;
        } else if (prezzoLower.contains("moderate") || prezzoLower.contains("mid-range") ||
                prezzoLower.contains("average")) {
            return 2;
        } else if (prezzoLower.contains("cheap") || prezzoLower.contains("budget") ||
                prezzoLower.contains("inexpensive")) {
            return 1;
        }

        // Metodo 4: Conta caratteri ripetitivi (per pattern come "****" o "####")
        if (prezzoTrimmed.length() > 0) {
            char firstChar = prezzoTrimmed.charAt(0);
            long charCount = prezzoTrimmed.chars().filter(ch -> ch == firstChar).count();

            // Se tutti i caratteri sono uguali e sono simboli non alfabetici
            if (charCount == prezzoTrimmed.length() && !Character.isLetter(firstChar)) {
                return Math.max(1, Math.min(4, (int) charCount));
            }
        }

        // Metodo 5: Analisi numerica per range di prezzi
        // Cerca numeri nella stringa per determinare il range
        String numbersOnly = prezzoTrimmed.replaceAll("[^0-9.]", "");
        if (!numbersOnly.isEmpty()) {
            try {
                double prezzoNumerico = Double.parseDouble(numbersOnly);

                // Range approssimativi per Euro (adatta in base alle tue esigenze)
                if (prezzoNumerico >= 80) return 4;      // Molto costoso
                else if (prezzoNumerico >= 50) return 3; // Costoso
                else if (prezzoNumerico >= 25) return 2; // Medio
                else return 1;                           // Economico

            } catch (NumberFormatException e) {
                // Se non riesce a parsare il numero, continua con il default
            }
        }

        // Default: livello 1 se non riesce a determinare il prezzo
        return 1;
    }

    /**
     * Aggiorna la visualizzazione della stella verde
     */
    private void updateStellaVerdeDisplay() {
        if (stellaVerdeLabel == null) return;

        String stellaVerde = ristorante.getStellaVerde();
        if (stellaVerde != null && !stellaVerde.trim().isEmpty() &&
                !"No".equalsIgnoreCase(stellaVerde.trim())) {
            stellaVerdeLabel.setText("Stella Verde");

            // Mostra il container della stella verde se disponibile
            if (stellaVerdeContainer != null) {
                stellaVerdeContainer.setVisible(true);
            }

            // Carica icona stella verde se disponibile
            if (stellaVerdeIcon != null) {
                try {
                    Image starImage = new Image(getClass().getResourceAsStream("/icons/green_star.png"));
                    stellaVerdeIcon.setImage(starImage);
                } catch (Exception e) {
                    // Se l'immagine non è disponibile, nascondi l'icona
                    stellaVerdeIcon.setVisible(false);
                }
            }
        } else {
            // Nascondi il container della stella verde
            if (stellaVerdeContainer != null) {
                stellaVerdeContainer.setVisible(false);
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
        String formatted = servizi.replaceAll("[,;]", "\n• ").trim();

        // Aggiungi un bullet point all'inizio se non presente
        if (!formatted.startsWith("•")) {
            formatted = "• " + formatted;
        }

        return formatted;
    }

    /**
     * Imposta un'immagine placeholder per il ristorante
     */
    private void setPlaceholderImage() {
        if (ristoranteImage == null) return;

        try {
            Image placeholder = new Image(getClass().getResourceAsStream("/images/restaurant_placeholder.jpg"));
            ristoranteImage.setImage(placeholder);
        } catch (Exception e) {
            // Se l'immagine placeholder non è disponibile, nasconde l'ImageView
            ristoranteImage.setVisible(false);
        }
    }

    /**
     * Gestisce il click sul link del sito web - VERSIONE MIGLIORATA
     * Utilizza sia HostServices che Desktop come fallback
     */
    @FXML
    private void handleSitoWebClick() {
        if (ristorante == null || ristorante.getSitoWeb() == null ||
                ristorante.getSitoWeb().trim().isEmpty()) {
            System.out.println("Nessun sito web disponibile");
            showAlert("Attenzione", "Nessun sito web disponibile per questo ristorante.");
            return;
        }

        String url = ristorante.getSitoWeb().trim();

        // Assicurati che l'URL abbia il protocollo
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        System.out.println("Tentativo di apertura URL: " + url);
        openExternalUrl(url);
    }

    /**
     * Gestisce il click sul numero di telefono
     */
    @FXML
    private void handleTelefonoClick() {
        if (ristorante != null && ristorante.getNumeroTelefono() != null) {
            String telefono = ristorante.getNumeroTelefono();
            System.out.println("Numero di telefono: " + telefono);

            // Copia il numero negli appunti
            try {
                java.awt.datatransfer.StringSelection stringSelection =
                        new java.awt.datatransfer.StringSelection(telefono);
                java.awt.datatransfer.Clipboard clipboard =
                        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                showAlert("Info", "Numero di telefono copiato negli appunti: " + telefono);
            } catch (Exception e) {
                System.err.println("Errore nella copia del telefono: " + e.getMessage());
                showAlert("Info", "Numero di telefono: " + telefono);
            }
        }
    }

    /**
     * Gestisce il click per mostrare tutte le recensioni
     */
    @FXML
    private void handleMostraRecensioni() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/theknife/recensioni.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("File recensioni.fxml non trovato nel classpath");
            }
            Parent root = loader.load();

            RecensioniController controller = loader.getController();
            controller.setRistoranteId(ristorante.getNome());

            Stage stage = new Stage();
            stage.setTitle("Recensioni - " + ristorante.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(mostraRecensioniButton.getScene().getWindow());
            stage.show();

            // Aggiorna le recensioni quando si chiude la finestra
            stage.setOnHiding(e -> loadRecensioni());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Errore", "Impossibile caricare le recensioni: " + e.getMessage());
        }
    }

    /**
     * Gestisce il click per scrivere una recensione
     */
    @FXML
    private void handleScriviRecensione() {
        if (!SessioneUtente.isUtenteLoggato()) {
            showAlert("Accesso richiesto", "Per scrivere una recensione devi effettuare l'accesso");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/theknife/recensioni.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("File recensioni.fxml non trovato nel classpath");
            }
            Parent root = loader.load();

            RecensioniController controller = loader.getController();
            controller.setRistoranteId(ristorante.getNome());

            Stage stage = new Stage();
            stage.setTitle("Scrivi Recensione - " + ristorante.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(scriviRecensioneButton.getScene().getWindow());
            stage.show();

            // Aggiorna le recensioni quando si chiude la finestra
            stage.setOnHiding(e -> loadRecensioni());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Errore", "Impossibile aprire il form per la recensione: " + e.getMessage());
        }
    }

    /**
     * Gestisce il click sul bottone preferito
     */
    @FXML
    private void handlePreferitoClick() {
        if (!SessioneUtente.isUtenteLoggato()) {
            showAlert("Accesso richiesto", "Per aggiungere ai preferiti devi effettuare l'accesso");
            return;
        }

        if (ristorante == null) return;

        String username = SessioneUtente.getUsernameUtente();
        String ristoranteId = ristorante.getNome();

        if (preferenceService.isPreferito(username, ristoranteId)) {
            preferenceService.rimuoviPreferito(username, ristoranteId);
            preferitoButton.setText("🤍 Aggiungi ai preferiti");
        } else {
            preferenceService.aggiungiPreferito(username, ristoranteId);
            preferitoButton.setText("❤️ Rimuovi dai preferiti");
        }
    }

    /**
     * Carica le recensioni recenti per il ristorante
     */
    private void loadRecensioni() {
        if (ristorante == null || recensioniRecentList == null) return;

        // Load only the 3 most recent reviews
        List<Recensione> listaRecensioni = recensioneService.getRecensioniRistorante(ristorante.getNome());
        ObservableList<Recensione> recensioni = FXCollections.observableArrayList(listaRecensioni);
        recensioni.sort((r1, r2) -> r2.getData().compareTo(r1.getData())); // Sort by date descending

        if (recensioni.size() > 3) {
            recensioni = FXCollections.observableArrayList(recensioni.subList(0, 3));
        }

        recensioniRecentList.setItems(recensioni);
    }

    /**
     * Restituisce il ristorante attualmente visualizzato
     * @return il ristorante corrente
     */
    public Ristorante getRistorante() {
        return ristorante;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}