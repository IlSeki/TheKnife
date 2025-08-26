package com.example.theknife;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.opencsv.CSVReader;

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
 * Controller principale per la visualizzazione e ricerca dei ristoranti.
 * Gestisce la schermata principale che mostra tutti i ristoranti disponibili
 * e permette agli utenti di cercare e visualizzare i dettagli dei ristoranti.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistorantiController implements Initializable {

    @FXML private TableView<Ristorante> tabellaRistoranti;
    @FXML private TableColumn<Ristorante, String> colonnaNome;
    @FXML private TableColumn<Ristorante, String> colonnaIndirizzo;
    @FXML private TableColumn<Ristorante, String> colonnaLocalita;
    @FXML private TableColumn<Ristorante, String> colonnaPrezzo;
    @FXML private TableColumn<Ristorante, String> colonnaCucina;
    @FXML private TextField campoRicerca;
    @FXML private TextField campoRicerca1;
    @FXML private TextField campoRicerca2;
    @FXML private MenuButton fasciaPrezzo;
    @FXML private Button dashboardButton;
    @FXML private Button profiloButton;

    private final ObservableList<Ristorante> listaRistoranti = FXCollections.observableArrayList();
    private String fasciaPrezzoSelezionata = "";

    /**
     * Inizializza il controller configurando la tabella dei ristoranti
     * e caricando i dati iniziali.
     *
     * @param location  L'URL di localizzazione della risorsa FXML (non utilizzato)
     * @param resources Le risorse aggiuntive per l'inizializzazione (non utilizzate)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colonnaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colonnaIndirizzo.setCellValueFactory(new PropertyValueFactory<>("indirizzo"));
        colonnaLocalita.setCellValueFactory(new PropertyValueFactory<>("localita"));
        colonnaPrezzo.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        colonnaCucina.setCellValueFactory(new PropertyValueFactory<>("cucina"));


        tabellaRistoranti.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ristorante ristorante = tabellaRistoranti.getSelectionModel().getSelectedItem();
                if (ristorante != null) {
                    apriDettagliRistorante(ristorante);
                }
            }
        });

        for (MenuItem item : fasciaPrezzo.getItems()) {
            item.setOnAction(e -> {
                fasciaPrezzoSelezionata = item.getText();   // salva la fascia scelta
                fasciaPrezzo.setText(item.getText());       // cambia il testo del MenuButton
            });
        }

        refreshData();
        String ruoloUtente = SessioneUtente.getRuoloUtente();
        dashboardButton.setVisible("ristoratore".equals(ruoloUtente));
        dashboardButton.setManaged("ristoratore".equals(ruoloUtente));
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
                        System.err.println("Errore nella riga " + (contatore + 1) + ": " + e.getMessage());
                    }
                    contatore++;
                }
                System.out.println("Totale ristoranti caricati: " + listaRistoranti.size());
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento del CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Apre la schermata dei dettagli del ristorante nella stessa finestra.
     * @param ristorante Il ristorante di cui visualizzare i dettagli
     */
    private void apriDettagliRistorante(Ristorante ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ristorante-detail.fxml"));
            Parent root = loader.load();
            RistoranteDetailController controller = loader.getController();
            controller.setRistorante(ristorante);
            // Salva il root originale e passalo al dettaglio
            Parent rootToRestore = tabellaRistoranti.getScene().getRoot();
            controller.setRootToRestore(rootToRestore);
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                Scene scene = root.getScene();
                scene.setRoot(rootToRestore);
                this.refreshData();
            });
            // Scene switch (finestra singola)
            Scene scene = tabellaRistoranti.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestisce il click sul pulsante del profilo utente.
     * Reindirizza l'utente alla sua pagina profilo.
     *
     * @param event L'evento di click
     */
    @FXML
    private void onProfiloClick(ActionEvent event) {
        try {
            String fxml = SessioneUtente.isUtenteLoggato() ? "user-profile.fxml" : "registrazione.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());
            Window window = tabellaRistoranti.getScene().getWindow();
            Stage stage = (Stage) window;
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            mostraErrore("Errore durante l'apertura del profilo/registrazione", e);
        }
    }

    /**
     * Gestisce il click sul pulsante della dashboard ristoratore.
     * Disponibile solo per gli utenti con ruolo ristoratore.
     *
     * @param event L'evento di click
     */
    @FXML
    private void onDashboardClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ristoratore-dashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/data/stile.css").toExternalForm());

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
     * Filtra i ristoranti in base al testo inserito nel campo di ricerca.
     *
     * @param event L'evento di click
     */
    @FXML
    private void onCercaClick(ActionEvent event) {
        String ricercaR = campoRicerca.getText().toLowerCase().trim();
        String ricercaL = campoRicerca1.getText().toLowerCase().trim();
        String ricercaC = campoRicerca2.getText().toLowerCase().trim();

        int selezioneCount = fasciaPrezzoSelezionata == null ? 0
                : fasciaPrezzoSelezionata.codePointCount(0, fasciaPrezzoSelezionata.length());

        // se tutti vuoti → mostra tutta la lista
        if (ricercaR.isEmpty() && ricercaL.isEmpty() && ricercaC.isEmpty() && selezioneCount == 0) {
            tabellaRistoranti.setItems(listaRistoranti);
            return;
        }

        ObservableList<Ristorante> risultati = FXCollections.observableArrayList(
                listaRistoranti.filtered(r -> {
                    boolean matchNome = ricercaR.isEmpty() || r.getNome().toLowerCase().contains(ricercaR);
                    boolean matchLocalita = ricercaL.isEmpty() || r.getLocalita().toLowerCase().startsWith(ricercaL);
                    boolean matchCucina = ricercaC.isEmpty() || r.getCucina().toLowerCase().contains(ricercaC);

                    int prezzoCount = r.getPrezzo().codePointCount(0, r.getPrezzo().length());
                    boolean matchPrezzo = (selezioneCount == 0) || (prezzoCount == selezioneCount);

                    return matchNome && matchLocalita && matchCucina && matchPrezzo;
                })
        );

        tabellaRistoranti.setItems(risultati);
    }


    /**
     * Aggiorna i dati della tabella dei ristoranti.
     * <p>
     * L'operazione prevede:
     * <ul>
     *   <li>Svuotare la lista corrente dei ristoranti</li>
     *   <li>Ricaricare i dati da sorgente CSV</li>
     *   <li>Ripopolare la {@code TableView} con i nuovi dati</li>
     * </ul>
     * </p>
     */
    public void refreshData() {
        listaRistoranti.clear();
        caricaDatiCSV();
        tabellaRistoranti.setItems(listaRistoranti);
    }
    /**
     * Mostra un messaggio di errore in una finestra di dialogo.
     * <p>
     * Utile per segnalare problemi durante l’esecuzione di operazioni
     * (ad esempio il caricamento dei dati).
     * </p>
     *
     * @param messaggio descrizione sintetica dell’errore da visualizzare come intestazione
     * @param e         eccezione che ha causato l’errore, da cui viene mostrato il {@code getMessage()}
     */
    private void mostraErrore(String messaggio, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(messaggio);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
