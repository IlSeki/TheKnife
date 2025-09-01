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

/**
 * Controller per la visualizzazione dei dettagli di un ristorante.
 * <p>
 * Questa classe gestisce l'interfaccia utente che mostra tutte le informazioni di un ristorante,
 * incluse le recensioni, e permette di aggiungere il ristorante ai preferiti
 * o di lasciare una recensione. Si occupa di popolare i campi dell'interfaccia
 * con i dati del ristorante selezionato e di gestire le interazioni dell'utente.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistoranteDetailController implements Initializable {

    /** Etichetta per visualizzare il nome del ristorante. */
    @FXML private Label nomeLabel;
    /** Etichetta per visualizzare l'indirizzo completo del ristorante. */
    @FXML private Label indirizzoLabel;
    /** Etichetta per visualizzare la località del ristorante. */
    @FXML private Label localitaLabel;
    /** Etichetta per visualizzare la fascia di prezzo del ristorante. */
    @FXML private Label prezzoLabel;
    /** Etichetta per visualizzare il tipo di cucina offerta dal ristorante. */
    @FXML private Label cucinaLabel;
    /** Etichetta per visualizzare il numero di telefono del ristorante. */
    @FXML private Label telefonoLabel;
    /** Hyperlink per navigare al sito web ufficiale del ristorante. */
    @FXML private Hyperlink sitoWebLink;
    /** Pulsante per aprire la posizione del ristorante su Google Maps. */
    @FXML private Button posizioneButton;
    /** Etichetta per visualizzare l'eventuale premio Michelin del ristorante. */
    @FXML private Label premioLabel;
    /** ImageView per visualizzare l'icona della stella verde Michelin. */
    @FXML private ImageView stellaVerdeIcon;
    /** Etichetta per accompagnare l'icona della stella verde Michelin. */
    @FXML private Label stellaVerdeLabel;
    /** Area di testo per visualizzare un elenco dei servizi offerti dal ristorante. */
    @FXML private TextArea serviziTextArea;
    /** Area di testo per visualizzare una descrizione dettagliata del ristorante. */
    @FXML private TextArea descrizioneTextArea;
    /** Contenitore HBox per la fascia di prezzo. Utilizzato per la visibilità condizionale. */
    @FXML private HBox prezzoContainer;
    /** Contenitore HBox per il premio Michelin. Utilizzato per la visibilità condizionale. */
    @FXML private HBox premioContainer;
    /** Contenitore VBox per la stella verde. Utilizzato per la visibilità condizionale. */
    @FXML private VBox stellaVerdeContainer;
    /** Pulsante per aggiungere o rimuovere il ristorante dai preferiti dell'utente. */
    @FXML private Button preferitoButton;
    /** ListView per visualizzare le recensioni più recenti del ristorante. */
    @FXML private ListView<Recensione> recensioniRecentList;
    /** Pulsante per navigare alla schermata completa di tutte le recensioni. */
    @FXML private Button mostraRecensioniButton;

    /** L'oggetto Ristorante di cui vengono visualizzati i dettagli. */
    private Ristorante ristorante;
    /** Servizi host forniti dal framework JavaFX per l'apertura di link esterni. */
    private HostServices hostServices;
    /** Istanza singleton per la gestione delle operazioni sui preferiti. */
    private final GestionePreferiti gestionePreferiti = com.example.theknife.GestionePreferiti.getInstance();
    /** Istanza singleton per la gestione delle operazioni sulle recensioni. */
    private final GestioneRecensioni gestioneRecensioni = GestioneRecensioni.getInstance();
    /** Callback opzionale per tornare al menu principale. */
    private Runnable returnToMenuCallback;
    /** Callback per tornare alla schermata precedente. */
    private Runnable tornaAlMenuPrincipaleCallback;
    /** Il nodo radice della scena precedente da ripristinare per la navigazione all'indietro. */
    private Parent rootToRestore;

    /**
     * Imposta i servizi host per aprire link esterni.
     * @param hostServices L'istanza di {@link HostServices} dell'applicazione.
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Imposta la callback da eseguire per tornare al menu principale.
     * @param cb La callback {@link Runnable} da eseguire.
     */
    public void setReturnToMenuCallback(Runnable cb) {
        this.returnToMenuCallback = cb;
    }

    /**
     * Imposta il nodo radice della scena precedente per la navigazione all'indietro.
     * @param root Il {@link Parent} da ripristinare.
     */
    public void setRootToRestore(Parent root) {
        this.rootToRestore = root;
    }

    /**
     * Imposta la callback da eseguire per tornare al menu principale.
     * @param callback La callback {@link Runnable} da eseguire.
     */
    public void setTornaAlMenuPrincipaleCallback(Runnable callback) {
        this.tornaAlMenuPrincipaleCallback = callback;
    }

    /**
     * Metodo di inizializzazione del controller.
     * Viene chiamato automaticamente dopo che il file FXML è stato caricato.
     * Configura i componenti dell'interfaccia, come le TextAreas, i pulsanti e la ListView delle recensioni.
     * @param location L'URL di localizzazione della risorsa.
     * @param resources Le risorse utilizzate per la localizzazione.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inizializzazione dei componenti
        setupTextAreas();
        setupPosizioneButton();
        setupPreferitoButton();

        // Inizializza i componenti con valori di default
        initializeDefaultValues();

        // Setup recensioni list cell factory
        recensioniRecentList.setCellFactory(_ -> new ListCell<>() {
            /**
             * Aggiorna la cella con i dati di una recensione.
             * @param item L'oggetto {@link Recensione} da visualizzare.
             * @param empty Flag che indica se la cella è vuota.
             */
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
    }

    /**
     * Configura l'aspetto del pulsante per la posizione su Google Maps.
     */
    private void setupPosizioneButton() {
        if (posizioneButton != null) {
            posizioneButton.getStyleClass().add("maps-button");
            posizioneButton.setText("📍 Vedi su Maps");
        }
    }

    /**
     * Configura la visibilità del pulsante per i preferiti in base al ruolo dell'utente.
     * Il pulsante è visibile solo se l'utente è un cliente.
     */
    private void setupPreferitoButton() {
        if (preferitoButton != null) {
            preferitoButton.setVisible(SessioneUtente.isCliente());
        }
    }

    /**
     * Inizializza i componenti dell'interfaccia con valori predefiniti
     * per visualizzare un'indicazione di caricamento all'utente.
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
     * Configura le aree di testo per una migliore visualizzazione dei dati,
     * impostando il wrapping del testo e rendendole non modificabili.
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
     * Imposta il ristorante da visualizzare e avvia l'aggiornamento dell'interfaccia.
     * Questo è il metodo principale utilizzato per passare i dati al controller.
     * @param ristorante L'oggetto {@link Ristorante} da visualizzare.
     */
    public void setRistorante(Ristorante ristorante) {
        this.ristorante = ristorante;

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

        if (preferitoButton != null && SessioneUtente.getUsernameUtente() != null) {
            boolean isPreferito = gestionePreferiti.isPreferito(
                    SessioneUtente.getUsernameUtente(),
                    ristorante.getNome()
            );
            preferitoButton.setText(isPreferito ? "❤️ Rimuovi dai preferiti" : "🤍 Aggiungi ai preferiti");
        }

        updateUI();
        loadRecensioni();
    }

    /**
     * Aggiorna tutti i componenti dell'interfaccia utente con i dati del ristorante.
     * L'aggiornamento viene eseguito sul thread della UI per evitare problemi di concorrenza.
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

                // Prezzo
                if (prezzoLabel != null) {
                    String prezzo = ristorante.getPrezzo();
                    if (prezzo != null && !prezzo.trim().isEmpty()) {
                        prezzoLabel.setText(prezzo);
                    } else {
                        prezzoLabel.setText("Non disponibile");
                    }
                }

                // Sito web
                if (sitoWebLink != null) {
                    String sitoWeb = ristorante.getSitoWeb();
                    if (sitoWeb != null && !sitoWeb.trim().isEmpty()) {
                        sitoWebLink.setText("Visita il sito web nella Guida Michelin");
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

                // Servizi
                if (serviziTextArea != null) {
                    String servizi = ristorante.getServizi();
                    System.out.println("Servizi dal ristorante: '" + servizi + "'"); // Debug
                    if (servizi != null && !servizi.trim().isEmpty()) {
                        serviziTextArea.setText(formatServizi(servizi));
                    } else {
                        serviziTextArea.setText("Nessun servizio specificato");
                    }
                }

                if (descrizioneTextArea != null) {
                    String descrizione = ristorante.getDescrizione();
                    System.out.println("Descrizione dal ristorante: '" + descrizione + "'"); // Debug
                    if (descrizione != null && !descrizione.trim().isEmpty()) {
                        descrizioneTextArea.setText(descrizione);
                    } else {
                        descrizioneTextArea.setText("Nessuna descrizione disponibile");
                    }
                }

                System.out.println("UI aggiornata con successo!");

            } catch (Exception e) {
                System.err.println("Errore durante l'aggiornamento dell'UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Verifica se il ristorante ha informazioni di posizione valide.
     * @return {@code true} se il ristorante ha almeno un indirizzo o una località valida, {@code false} altrimenti.
     */
    private boolean hasValidLocation() {
        if (ristorante == null) return false;

        String indirizzo = ristorante.getIndirizzo();
        String localita = ristorante.getLocalita();

        return (indirizzo != null && !indirizzo.trim().isEmpty()) ||
                (localita != null && !localita.trim().isEmpty());
    }

    /**
     * Costruisce una stringa con l'indirizzo completo del ristorante, formattata per essere usata nelle ricerche.
     * @return Una {@link String} con l'indirizzo formattato per la ricerca, ad es. "Nome Ristorante, Via di Prova 123, Città".
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
     * Gestisce l'evento di click sul pulsante "Posizione".
     * Costruisce un URL per Google Maps e tenta di aprirlo nel browser predefinito.
     * Se la posizione non è disponibile o l'apertura fallisce, mostra un avviso all'utente.
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
     * Apre un URL esterno utilizzando diversi metodi come fallback per garantire la compatibilità.
     * Prova prima con {@link HostServices}, poi con {@link Desktop} (se supportato)
     * e infine con {@link ProcessBuilder} per l'apertura del comando specifico del sistema operativo.
     * @param url La {@link String} dell'URL da aprire.
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
     * Aggiorna la visibilità dell'icona e dell'etichetta relative alla stella verde Michelin.
     * Vengono mostrate solo se il ristorante ha la stella verde, altrimenti il container viene nascosto.
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
     * Formatta una stringa di servizi in un formato più leggibile,
     * aggiungendo un bullet point e un a capo per ogni servizio separato da virgola o punto e virgola.
     * @param servizi La {@link String} grezza dei servizi.
     * @return Una {@link String} formattata con bullet points, ad es. "• Servizio 1\n• Servizio 2".
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
     * Gestisce l'evento di click sull'hyperlink del sito web.
     * Aggiunge un protocollo HTTPS se mancante e tenta di aprire l'URL esterno.
     */
    @FXML
    private void handleSitoWebClick1() {
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
     * Gestisce l'evento di click sull'hyperlink del sito web.
     * Aggiunge un protocollo HTTPS se mancante e tenta di aprire l'URL esterno.
     */

    @FXML
    private void handleSitoWebClick() {
        if (ristorante == null || ristorante.getUrl() == null ||
                ristorante.getUrl().trim().isEmpty()) {
            System.out.println("Nessun sito web disponibile");
            showAlert("Attenzione", "Nessun sito web disponibile per questo ristorante.");
            return;
        }

        String url = ristorante.getUrl().trim();

        // Assicurati che l'URL abbia il protocollo
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        System.out.println("Tentativo di apertura URL: " + url);
        openExternalUrl(url);
    }

    /**
     * Gestisce l'evento di click sul numero di telefono.
     * Copia il numero di telefono del ristorante negli appunti di sistema e notifica l'utente con un {@link Alert}.
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
     * Gestisce l'evento di click sul pulsante per mostrare tutte le recensioni.
     * Carica una nuova scena ({@code recensioni.fxml}) e passa i dati del ristorante al nuovo controller.
     * Imposta una callback per tornare a questa scena dopo l'interazione.
     */
    @FXML
    private void handleMostraRecensioni() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/theknife/recensioni.fxml"));
            Parent recensioniRoot = loader.load();
            RecensioniController controller = loader.getController();
            controller.setRistoranteId(ristorante.getNome());
            // Passa il root di ritorno
            Parent rootToRestore = nomeLabel.getScene().getRoot();
            controller.setRootToRestore(rootToRestore);
            // Callback: aggiorna le recensioni recenti quando si torna indietro
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                Scene scene = recensioniRoot.getScene();
                scene.setRoot(rootToRestore);
                this.refreshRecensioni();
            });
            // Scene switch (finestra singola)
            Scene scene = nomeLabel.getScene();
            scene.setRoot(recensioniRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Errore", "Impossibile caricare le recensioni: " + e.getMessage());
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Preferito".
     * Aggiunge o rimuove il ristorante dai preferiti dell'utente loggato, aggiornando
     * il testo del pulsante di conseguenza.
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

        if (gestionePreferiti.isPreferito(username, ristoranteId)) {
            gestionePreferiti.rimuoviPreferito(username, ristoranteId);
            preferitoButton.setText("🤍 Aggiungi ai preferiti");
        } else {
            gestionePreferiti.aggiungiPreferito(username, ristoranteId);
            preferitoButton.setText("❤️ Rimuovi dai preferiti");
        }
    }

    /**
     * Carica le recensioni più recenti per il ristorante.
     * Recupera tutte le recensioni, le ordina per data (dalla più recente) e visualizza solo le prime tre nella {@link ListView}.
     */
    private void loadRecensioni() {
        if (ristorante == null || recensioniRecentList == null) return;

        // Load only the 3 most recent reviews
        List<Recensione> listaRecensioni = gestioneRecensioni.getRecensioniRistorante(ristorante.getNome());
        ObservableList<Recensione> recensioni = FXCollections.observableArrayList(listaRecensioni);
        recensioni.sort((r1, r2) -> r2.getData().compareTo(r1.getData())); // Sort by date descending

        if (recensioni.size() > 3) {
            recensioni = FXCollections.observableArrayList(recensioni.subList(0, 3));
        }

        recensioniRecentList.setItems(recensioni);
    }

    /**
     * Restituisce l'oggetto ristorante attualmente visualizzato dal controller.
     * @return L'oggetto {@link Ristorante} corrente.
     */
    public Ristorante getRistorante() {
        return ristorante;
    }

    /**
     * Aggiorna la lista delle recensioni recenti.
     * Questo metodo è utile per ricaricare le recensioni quando si torna da un'altra schermata (es. aggiunta recensione).
     */
    public void refreshRecensioni() {
        loadRecensioni();
    }

    /**
     * Gestisce l'evento di click sul pulsante "Torna al menu principale".
     * Esegue la callback di ritorno se impostata, altrimenti tenta di ripristinare il nodo radice
     * della scena precedente o di caricare la schermata della lista dei ristoranti.
     */
    @FXML
    private void handleTornaAlMenuPrincipale() {
        System.out.println("DEBUG: Bottone premuto");
        if (tornaAlMenuPrincipaleCallback != null) {
            System.out.println("DEBUG: Eseguo callback");
            tornaAlMenuPrincipaleCallback.run();
        } else {
            System.out.println("DEBUG: Nessuna callback disponibile");
        }
        if (tornaAlMenuPrincipaleCallback != null) {
            tornaAlMenuPrincipaleCallback.run();
        } else if (rootToRestore != null) {
            Scene scene = nomeLabel.getScene();
            scene.setRoot(rootToRestore);
        } else {
            // Fallback: torna alla lista ristoranti (menu principale moderno)
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("lista.fxml"));
                Parent root = loader.load();
                Scene scene = nomeLabel.getScene();
                scene.setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mostra una finestra di dialogo di tipo {@link Alert} all'utente.
     * @param title Il titolo della finestra di dialogo.
     * @param message Il messaggio da visualizzare nel corpo della finestra.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
