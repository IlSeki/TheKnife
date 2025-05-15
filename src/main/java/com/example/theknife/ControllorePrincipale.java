package com.example.theknife;

import com.opencsv.CSVReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Arrays;

public class ControllorePrincipale implements Initializable {

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

    private ObservableList<Ristorante> listaRistoranti = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Stampa la working directory per verificare dove viene ricercato il file
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        // Collega le proprietà del modello alle colonne del TableView
        colonnaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colonnaIndirizzo.setCellValueFactory(new PropertyValueFactory<>("indirizzo"));
        colonnaLocalita.setCellValueFactory(new PropertyValueFactory<>("localita"));
        colonnaPrezzo.setCellValueFactory(new PropertyValueFactory<>("prezzo"));
        colonnaCucina.setCellValueFactory(new PropertyValueFactory<>("cucina"));

        // Carica i dati dal file CSV e stampa le prime 5 righe (per debug)
        caricaDatiCSV();

        // Imposta i dati nel TableView
        tabellaRistoranti.setItems(listaRistoranti);
    }

    private void caricaDatiCSV() {
        // Imposta il percorso corretto in base alla struttura del progetto:
        String percorsoCSV = "src/main/java/data/michelin_my_maps.csv";

        try (CSVReader reader = new CSVReader(new FileReader(percorsoCSV))) {
            // Salta l'intestazione del CSV
            String[] header = reader.readNext();
            System.out.println("Intestazione: " + Arrays.toString(header));

            String[] riga;
            int contatore = 0;
            while ((riga = reader.readNext()) != null) {
                // Stampa le prime 5 righe per verificare il contenuto
                if (contatore < 5) {
                    System.out.println("Riga " + (contatore + 1) + ": " + Arrays.toString(riga));
                } else {
                    // Se abbiamo già letto 5 righe, interrompiamo il ciclo.
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
