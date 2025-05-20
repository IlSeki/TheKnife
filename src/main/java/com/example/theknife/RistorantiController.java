package com.example.theknife;

import com.opencsv.CSVReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * La classe {@code RistorantiController} funge da controller principale per l'interfaccia utente
 * dell'applicazione "The Knife". In qualità di controller JavaFX, essa implementa l'interfaccia
 * {@link Initializable} per garantire l'inizializzazione corretta dei componenti grafici definiti in FXML
 * e per stabilire il legame tra il modello dei dati (classe {@link Ristorante}) e la vista.
 *
 * <p>
 * Le funzionalità principali della classe includono:
 * <ul>
 *   <li>Collegamento delle proprietà del modello ai componenti grafici, in particolare alle colonne della
 *       {@code TableView} che visualizza i dati relativi ai ristoranti.</li>
 *   <li>Caricamento dei dati da un file CSV presente tra le risorse del progetto, mediante il metodo
 *       {@link #caricaDatiCSV()}.</li>
 *   <li>Impostazione dei dati caricati nella {@code TableView} per una visualizzazione dinamica e interattiva.</li>
 *   <li>Debug durante il caricamento dei dati, mediante la stampa in console della working directory e delle
 *       prime cinque righe lette dal CSV.</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @version 1.0
 * @see Ristorante
 * @since 2025-05-13
 */
public class RistorantiController implements Initializable {

    @FXML
    private TableView<Ristorante> tabellaRistoranti;
    @FXML
    private TableColumn<Ristorante, String> colonnaNome;
    @FXML
    private TableColumn<Ristorante, String> colonnaIndirizzo;
    @FXML
    private TableColumn<Ristorante, String> colonnaLocalita;
    @FXML
    private TableColumn<Ristorante, String> colonnaPrezzo;
    @FXML
    private TableColumn<Ristorante, String> colonnaCucina;

    private final ObservableList<Ristorante> listaRistoranti = FXCollections.observableArrayList();

    /**
     * Inizializza il controller, configurando i componenti dell'interfaccia utente e caricando i dati
     * dei ristoranti dal file CSV. Durante l'inizializzazione vengono:
     * <ul>
     *   <li>Stampata la working directory per scopi di debug.</li>
     *   <li>Associate le proprietà del modello alle corrispondenti colonne della {@code TableView}.</li>
     *   <li>Richiamato il metodo {@link #caricaDatiCSV()} per il caricamento dei dati dai file di risorsa.</li>
     *   <li>Impostati i dati caricati nella {@code TableView} per la visualizzazione.</li>
     * </ul>
     *
     * @param location  L'URL di localizzazione della risorsa FXML (non utilizzato).
     * @param resources Le risorse aggiuntive per l'inizializzazione (non utilizzate).
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Stampa la working directory per verificare il contesto d'esecuzione (utile per debug)
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        // Collega le proprietà del modello alle colonne del TableView
        colonnaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colonnaIndirizzo.setCellValueFactory(new PropertyValueFactory<>("indirizzo"));
        colonnaLocalita.setCellValueFactory(new PropertyValueFactory<>("localita"));
        colonnaPrezzo.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        colonnaCucina.setCellValueFactory(new PropertyValueFactory<>("cucina"));

        // Carica i dati dal file CSV e visualizza le prime 5 righe per debug
        caricaDatiCSV();

        // Imposta i dati nella TableView
        tabellaRistoranti.setItems(listaRistoranti);
    }

    /**
     * Carica i dati dei ristoranti da un file CSV presente nelle risorse del programma e li
     * aggiunge alla lista interna dei ristoranti. <br>
     * Il metodo utilizza {@code getResourceAsStream("/data/michelin_my_maps.csv")} per accedere al file CSV,
     * legge la riga di intestazione per confermare il formato e processa le righe successive. Per scopi di debug,
     * vengono stampate in output le prime cinque righe lette. <br>
     * Ad ogni riga vengono estratti i campi necessari (ad es. nome, indirizzo, coordinate, ecc.) e, tramite
     * conversioni specifiche (per esempio, {@code Double.parseDouble} per longitudine e latitudine), viene creato
     * un oggetto {@link Ristorante} che viene aggiunto alla lista dei ristoranti. <br>
     * In caso di errori nella lettura o nella conversione dei dati, il metodo gestisce le eccezioni internamente,
     * stampando il relativo messaggio d’errore e proseguendo con il caricamento.
     *
     * @author Samuele Secchi, 761031, Sede CO
     * @param Nessuno - Questo metodo non richiede parametri.
     * @return Nessuno - Il metodo non restituisce alcun valore.
     * @throws Nessuno - Tutte le eccezioni vengono gestite internamente e non vengono propagate.
     */
    private void caricaDatiCSV() {
        // Usa getResourceAsStream per accedere al CSV incluso nelle risorse
        try (InputStream is = getClass().getResourceAsStream("/data/michelin_my_maps.csv")) {
            if (is == null) {
                System.err.println("Risorsa non trovata: /data/michelin_my_maps.csv");
                return;
            }
            try (InputStreamReader isr = new InputStreamReader(is);
                 CSVReader reader = new CSVReader(isr)) {

                // Salta l'intestazione del CSV
                String[] header = reader.readNext();
                System.out.println("Intestazione: " + Arrays.toString(header));

                String[] riga;
                int contatore = 0;
                while ((riga = reader.readNext()) != null) {
                    // Stampa le prime 5 righe per debug
                    if (contatore < 5) {
                        System.out.println("Riga " + (contatore + 1) + ": " + Arrays.toString(riga));
                    } else {
                        break;
                    }
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
                        System.out.println("Errore nella riga: " + e.getMessage());
                    }
                    contatore++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
