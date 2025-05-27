package com.example.theknife;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * [Samuele Secchi, 761031, Sede CO]
 * [Flavio Marin, 759910, Sede CO]
 * [Matilde Lecchi, 759875, Sede CO]
 * [Davide Caccia, 760742, Sede CO]
 * @version 1.0
 */
public class RecensioniController {

    @FXML
    private PieChart pieChart;

    @FXML
    private ComboBox<Integer> comboBox;

    @FXML
    private TableView<Recensione> tableView;

    @FXML
    private TableColumn<Recensione, Integer> colStelle;

    @FXML
    private TableColumn<Recensione, String> colRecensione;

    // Mappa per aggiornare il PieChart
    private final Map<Integer, Integer> recensioniMap = new HashMap<>();

    // Lista master per la TableView
    private ObservableList<Recensione> masterRecensioniList;

    // Percorso del file CSV
    private static final String CSV_FILE_PATH = "src/main/resources/data/recensioni.csv";

    @FXML
    public void initialize() {

        // Configura la ComboBox per il filtro per stelle
        comboBox.getItems().addAll(1, 2, 3, 4, 5);
        comboBox.setPromptText("Seleziona numero stelle");

        // Inizializza la mappa dei conteggi
        recensioniMap.clear();

        // Inizializza la lista master e carica le recensioni dal CSV
        masterRecensioniList = FXCollections.observableArrayList();
        caricaRecensioniDaCSV(CSV_FILE_PATH);

        // Avvolgi la lista master in una FilteredList per il filtraggio della TableView
        FilteredList<Recensione> filteredList = new FilteredList<>(masterRecensioniList, recensione -> true);
        tableView.setItems(filteredList);

        // Configura le colonne della TableView
        colStelle.setCellValueFactory(new PropertyValueFactory<>("stelle"));
        colRecensione.setCellValueFactory(new PropertyValueFactory<>("recensione"));

        // Listener sulla ComboBox per filtrare le recensioni per numero di stelle
        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filteredList.setPredicate(recensione -> recensione.getStelle() == newVal);
            } else {
                filteredList.setPredicate(recensione -> true);
            }
        });
    }

    /**
     * Legge le recensioni da un file CSV e aggiorna sia la lista master che la mappa per il PieChart.
     *
     * @param filePath il percorso del file CSV
     */
    private void caricaRecensioniDaCSV(String filePath) {
        File file = new File(filePath);
        // Se il file non esiste, crealo con l'header
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("stelle,recensione");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;  // Non ci sono recensioni da caricare
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                // Salta l'intestazione
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                // Supponiamo un formato semplice: valore separati da virgola (limit=2 per gestire eventuali virgole nel testo)
                String[] tokens = line.split(",", 2);
                if (tokens.length == 2) {
                    int stelle = Integer.parseInt(tokens[0].trim());
                    String recensione = tokens[1].trim();
                    // Rimuove eventuali virgolette iniziali e finali
                    if (recensione.startsWith("\"") && recensione.endsWith("\"")) {
                        recensione = recensione.substring(1, recensione.length() - 1);
                    }
                    Recensione r = new Recensione(stelle, recensione);
                    masterRecensioniList.add(r);

                    // Aggiorna il conteggio nella mappa per il PieChart
                    recensioniMap.put(stelle, recensioniMap.getOrDefault(stelle, 0) + 1);
                }
            }
            // Aggiorna il PieChart con i dati letti
            aggiornaPieChart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aggiorna il PieChart in base alla distribuzione delle recensioni.
     */
    private void aggiornaPieChart() {
        pieChart.getData().clear();
        recensioniMap.forEach((stelle, numeroRecensioni) -> {
            pieChart.getData().add(new PieChart.Data(stelle + " ⭐", numeroRecensioni));
        });
    }

    /**
     * Aggiunge una recensione sia alla lista della TableView che al file CSV.
     *
     * @param stelle     punteggio (1-5)
     * @param recensione testo recensione
     */
    public void aggiungiRecensione(int stelle, String recensione) {
        // Crea l'oggetto Recensione e aggiornalo nella lista
        Recensione r = new Recensione(stelle, recensione);
        masterRecensioniList.add(r);

        // Aggiorna la mappa (e conseguentemente il PieChart)
        recensioniMap.put(stelle, recensioniMap.getOrDefault(stelle, 0) + 1);
        aggiornaPieChart();

        // Append all'archivio CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
            String record = stelle + ",\"" + recensione.replace("\"", "\"\"") + "\"";
            writer.newLine();
            writer.write(record);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Classe interna che rappresenta una recensione.
     */
    public static class Recensione {
        private final SimpleIntegerProperty stelle;
        private final SimpleStringProperty recensione;

        public Recensione(int stelle, String recensione) {
            this.stelle = new SimpleIntegerProperty(stelle);
            this.recensione = new SimpleStringProperty(recensione);
        }

        public int getStelle() {
            return stelle.get();
        }
        public void setStelle(int stelle) {
            this.stelle.set(stelle);
        }
        public SimpleIntegerProperty stelleProperty() {
            return stelle;
        }
        public String getRecensione() {
            return recensione.get();
        }
        public void setRecensione(String recensione) {
            this.recensione.set(recensione);
        }
        public SimpleStringProperty recensioneProperty() {
            return recensione;
        }
    }
}
