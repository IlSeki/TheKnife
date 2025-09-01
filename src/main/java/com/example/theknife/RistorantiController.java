package com.example.theknife;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controller per la schermata principale di visualizzazione e ricerca dei ristoranti.
 * <p>
 * Questa classe gestisce l'interfaccia utente che mostra una lista di tutti i ristoranti
 * disponibili, permettendo all'utente di cercarli, filtrarli per fascia di prezzo e
 * visualizzarne i dettagli. Adatta anche la visibilità di alcuni elementi dell'interfaccia
 * in base al ruolo dell'utente loggato.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistorantiController implements Initializable {

    /**
     * La tabella che visualizza i dati dei ristoranti.
     */
    @FXML private TableView<Ristorante> tabellaRistoranti;
    /**
     * Colonna della tabella per il nome del ristorante.
     */
    @FXML private TableColumn<Ristorante, String> colonnaNome;
    /**
     * Colonna della tabella per l'indirizzo del ristorante.
     */
    @FXML private TableColumn<Ristorante, String> colonnaIndirizzo;
    /**
     * Colonna della tabella per la località del ristorante.
     */
    @FXML private TableColumn<Ristorante, String> colonnaLocalita;
    /**
     * Colonna della tabella per la fascia di prezzo del ristorante.
     */
    @FXML private TableColumn<Ristorante, String> colonnaPrezzo;
    /**
     * Colonna della tabella per il tipo di cucina del ristorante.
     */
    @FXML private TableColumn<Ristorante, String> colonnaCucina;
    /**
     * Campo di testo per la ricerca per nome del ristorante.
     */
    @FXML private TextField campoRicerca;
    /**
     * Campo di testo per la ricerca per località.
     */
    @FXML private TextField campoRicerca1;
    /**
     * Campo di testo per la ricerca per tipo di cucina.
     */
    @FXML private TextField campoRicerca2;
    /**
     * Pulsante con menu a tendina per selezionare la fascia di prezzo.
     */
    @FXML private MenuButton fasciaPrezzo;
    /**
     * Pulsante che reindirizza alla dashboard del ristoratore.
     */
    @FXML private Button dashboardButton;
    /**
     * Pulsante che reindirizza al profilo utente o alla schermata di registrazione.
     */
    @FXML private Button profiloButton;

    private final ObservableList<Ristorante> listaRistoranti = FXCollections.observableArrayList();
    private String fasciaPrezzoSelezionata = "";

    /**
     * Inizializza il controller dopo che il file FXML è stato caricato.
     * Configura le colonne della tabella, imposta i listener per gli eventi utente
     * e carica i dati iniziali dei ristoranti. Adatta anche la visibilità dei
     * pulsanti in base al ruolo dell'utente corrente.
     * Infine carica inizialmente i ristoranti vicini al domicilio dell'utente, se loggato.
     *
     * @param location  L'URL di localizzazione della risorsa FXML, o null se non noto.
     * @param resources Le risorse utilizzate per localizzare l'oggetto root, o null se non localizzato.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Collega le colonne della tabella alle proprietà dell'oggetto Ristorante
        colonnaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colonnaIndirizzo.setCellValueFactory(new PropertyValueFactory<>("indirizzo"));
        colonnaLocalita.setCellValueFactory(new PropertyValueFactory<>("localita"));
        colonnaPrezzo.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        colonnaCucina.setCellValueFactory(new PropertyValueFactory<>("cucina"));

        // Imposta la proprietà reorderable su false per ogni colonna
        colonnaNome.setReorderable(false);
        colonnaIndirizzo.setReorderable(false);
        colonnaLocalita.setReorderable(false);
        colonnaPrezzo.setReorderable(false);
        colonnaCucina.setReorderable(false);

        // Configura le colonne per larghezza uguale e non ridimensionabili dall'utente
        tabellaRistoranti.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Aggiunge un listener per gestire il doppio click sulla tabella
        tabellaRistoranti.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ristorante ristorante = tabellaRistoranti.getSelectionModel().getSelectedItem();
                if (ristorante != null) {
                    apriDettagliRistorante(ristorante);
                }
            }

        });

        // Configura il listener per il menu della fascia di prezzo
        for (MenuItem item : fasciaPrezzo.getItems()) {
            item.setOnAction(e -> {
                fasciaPrezzoSelezionata = item.getText();
                fasciaPrezzo.setText(item.getText());
                onCercaClick(e); // Applica il filtro immediatamente
            });
        }

        // Carica i dati iniziali
        refreshData();

        // Adatta l'interfaccia utente in base al ruolo dell'utente
        String ruoloUtente = SessioneUtente.getRuoloUtente();
        if(!"ristoratore".equals(ruoloUtente) && !"cliente".equals(ruoloUtente)){
            profiloButton.setText("Registrati");
        }
        dashboardButton.setVisible("ristoratore".equals(ruoloUtente));
        dashboardButton.setManaged("ristoratore".equals(ruoloUtente));

        if("ristoratore".equals(ruoloUtente) || "cliente".equals(ruoloUtente)){
            campoRicerca1.setText(SessioneUtente.getDomicilio());
        }
        this.onCercaClick(null);

    }

    /**
     * Carica i dati dei ristoranti da un file CSV.
     * <p>
     * Il metodo cerca il file `michelin_my_maps.csv` nella directory `data`. Se la directory
     * non esiste, la crea. Se il file non esiste, lo crea con un'intestazione predefinita.
     * Legge ogni riga del file, crea un oggetto {@link Ristorante} e lo aggiunge alla
     * lista di ristoranti. Eventuali errori di parsing in una riga non bloccano l'intero
     * processo, ma vengono stampati come avvisi sulla console.
     * </p>
     *
     * @throws RuntimeException se si verifica un errore di I/O o di validazione del CSV.
     */
    private void caricaDatiCSV() {
        String filePath = "data/michelin_my_maps.csv";
        File csvFile = new File(filePath);

        File parentDir = csvFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("DEBUG: Cartella 'data' creata.");
        }

        if (!csvFile.exists()) {
            System.err.println("File CSV non trovato. Creazione di un nuovo file.");
            try (FileWriter writer = new FileWriter(csvFile)) {
                writer.append("nome,indirizzo,localita,prezzo,cucina,longitudine,latitudine,numeroTelefono,url,sitoWeb,premio,stellaVerde,servizi,descrizione\n");
                System.out.println("DEBUG: Nuovo file CSV creato con header.");
            } catch (IOException e) {
                System.err.println("Errore durante la creazione del file CSV: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        try (FileReader reader = new FileReader(csvFile);
             CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext(); // Salta l'intestazione

            String[] riga;
            while ((riga = csvReader.readNext()) != null) {
                try {
                    String nome = riga[0];
                    String indirizzo = riga[1];
                    String localita = riga[2];
                    String prezzo = riga[3];
                    String cucina = riga[4];
                    double longitudine = Double.parseDouble(riga[5]);
                    double latitudine = Double.parseDouble(riga[6]);
                    String numeroTelefono = riga[7];
                    String url = riga[8];
                    String sitoWeb = riga[9];
                    String premio = riga[10];
                    String stellaVerde = riga[11];
                    String servizi = riga[12];
                    String descrizione = riga[13];

                    Ristorante ristorante = new Ristorante(
                            nome, indirizzo, localita, prezzo, cucina,
                            longitudine, latitudine, numeroTelefono,
                            url, sitoWeb, premio, stellaVerde,
                            servizi, descrizione
                    );
                    listaRistoranti.add(ristorante);
                } catch (Exception e) {
                    System.err.println("Errore nella riga: " + Arrays.toString(riga) + " - " + e.getMessage());
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Errore di I/O o di validazione nel caricamento del CSV.", e);
        }
    }

    /**
     * Apre la schermata dei dettagli del ristorante selezionato nella stessa finestra.
     * Mantiene un riferimento al "root" della schermata corrente per poterci tornare indietro.
     *
     * @param ristorante Il ristorante di cui visualizzare i dettagli.
     */
    private void apriDettagliRistorante(Ristorante ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ristorante-detail.fxml"));
            Parent root = loader.load();
            RistoranteDetailController controller = loader.getController();
            controller.setRistorante(ristorante);

            Parent rootToRestore = tabellaRistoranti.getScene().getRoot();
            controller.setRootToRestore(rootToRestore);
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                Scene scene = root.getScene();
                scene.setRoot(rootToRestore);
                this.refreshData();
            });

            Scene scene = tabellaRistoranti.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            mostraErrore("Errore nell'apertura della finestra dei dettagli del ristorante.", e);
        }
    }

    /**
     * Gestisce il click sul pulsante del profilo utente.
     * Reindirizza l'utente alla sua pagina profilo se è già loggato, altrimenti
     * lo porta alla schermata di registrazione.
     *
     * @param event L'evento di click del pulsante.
     */
    @FXML
    private void onProfiloClick(ActionEvent event) {
        try {
            String fxml = SessioneUtente.isUtenteLoggato() ? "user-profile.fxml" : "registrazione.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/data/stile.css")).toExternalForm());
            Window window = tabellaRistoranti.getScene().getWindow();
            Stage stage = (Stage) window;
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            mostraErrore("Errore durante l'apertura del profilo/registrazione", e);
        }
    }

    /**
     * Gestisce il click sul pulsante che porta alla dashboard del ristoratore.
     * Questo pulsante è visibile solo per gli utenti con ruolo "ristoratore".
     *
     * @param event L'evento di click del pulsante.
     */
    @FXML
    private void onDashboardClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ristoratore-dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/data/stile.css")).toExternalForm());
            Window window = tabellaRistoranti.getScene().getWindow();
            Stage stage = (Stage) window;
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            mostraErrore("Errore durante l'apertura della dashboard", e);
        }
    }

    /**
     * Gestisce il click sul pulsante di ricerca.
     * Filtra la lista dei ristoranti visualizzati nella tabella in base ai
     * criteri di ricerca inseriti nei campi di testo (nome, località, cucina)
     * e nel menu a tendina della fascia di prezzo.
     *
     * @param event L'evento di click del pulsante.
     */
    @FXML
    private void onCercaClick(ActionEvent event) {
        String ricercaR = campoRicerca.getText().toLowerCase().trim();
        String ricercaL = campoRicerca1.getText().toLowerCase().trim();
        String ricercaC = campoRicerca2.getText().toLowerCase().trim();
        int selezioneCount = fasciaPrezzoSelezionata == null ? 0 : fasciaPrezzoSelezionata.length();

        if (ricercaR.isEmpty() && ricercaL.isEmpty() && ricercaC.isEmpty() && selezioneCount == 0) {
            tabellaRistoranti.setItems(listaRistoranti);
            return;
        }

        ObservableList<Ristorante> risultati = FXCollections.observableArrayList(
                listaRistoranti.filtered(r -> {
                    boolean matchNome = ricercaR.isEmpty() || r.getNome().toLowerCase().contains(ricercaR);
                    boolean matchLocalita = ricercaL.isEmpty() || r.getLocalita().toLowerCase().startsWith(ricercaL);
                    boolean matchCucina = ricercaC.isEmpty() || r.getCucina().toLowerCase().contains(ricercaC);
                    int prezzoCount = r.getPrezzo() == null ? 0 : r.getPrezzo().length();
                    boolean matchPrezzo = (selezioneCount == 0) || (prezzoCount == selezioneCount);
                    return matchNome && matchLocalita && matchCucina && matchPrezzo;
                })
        );

        tabellaRistoranti.setItems(risultati);
    }

    /**
     * Aggiorna i dati della tabella dei ristoranti.
     * Svuota la lista corrente, ricarica i dati dal file CSV e ripopola la
     * {@code TableView} con i dati aggiornati.
     */
    public void refreshData() {
        listaRistoranti.clear();
        caricaDatiCSV();
        tabellaRistoranti.setItems(listaRistoranti);
    }

    /**
     * Mostra una finestra di dialogo di errore con un messaggio personalizzato.
     *
     * @param messaggio Descrizione sintetica dell'errore.
     * @param e         L'eccezione che ha causato l'errore, il cui messaggio viene
     * visualizzato come dettaglio.
     */
    private void mostraErrore(String messaggio, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(messaggio);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}