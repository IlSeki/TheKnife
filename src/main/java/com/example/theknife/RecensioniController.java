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

    // Mappa che tiene traccia del numero di recensioni per ogni punteggio di stelle (per il PieChart)
    private final Map<Integer, Integer> recensioniMap = new HashMap<>();

    // Lista master di tutte le recensioni per popolazione della TableView
    private ObservableList<Recensione> masterRecensioniList;

    @FXML
    public void initialize() {
        // Popola la ComboBox per permettere la selezione del numero di stelle.
        // Selezionando un valore la TableView mostrerà solo le recensioni aventi quel punteggio.
        comboBox.getItems().addAll(1, 2, 3, 4, 5);
        comboBox.setPromptText("Seleziona numero stelle");

        // SIMULAZIONE: dati per il PieChart - andranno sostituiti con dati reali
        recensioniMap.put(1, 2); // 2 recensioni con 1 stella
        recensioniMap.put(2, 5); // 5 recensioni con 2 stelle
        recensioniMap.put(3, 3); // 3 recensioni con 3 stelle
        recensioniMap.put(4, 4); // 4 recensioni con 4 stelle
        recensioniMap.put(5, 3); // 3 recensioni con 5 stelle

        // Aggiorna il PieChart con i dati
        aggiornaPieChart();

        // Crea alcuni esempi di recensioni per la TableView
        masterRecensioniList = FXCollections.observableArrayList(
                new Recensione(5, "Ottimo prodotto, consigliato!"),
                new Recensione(4, "Buono, ma con qualche piccolo difetto."),
                new Recensione(3, "Prodotto nella media, niente di eccezionale."),
                new Recensione(2, "Non ha soddisfatto le aspettative."),
                new Recensione(1, "Pessimo, molto deludente.")
        );

        // Avvolgiamo la lista master in una FilteredList per permettere il filtraggio
        FilteredList<Recensione> filteredList = new FilteredList<>(masterRecensioniList, recensione -> true);
        tableView.setItems(filteredList);

        // Configura le colonne della TableView
        colStelle.setCellValueFactory(new PropertyValueFactory<>("stelle"));
        colRecensione.setCellValueFactory(new PropertyValueFactory<>("recensione"));

        // Imposta un listener sulla ComboBox per aggiornare il filtro della TableView
        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Filtra le recensioni in base al numero di stelle selezionato
                filteredList.setPredicate(recensione -> recensione.getStelle() == newVal);
            } else {
                // Se non c'è selezione, non applica alcun filtro
                filteredList.setPredicate(recensione -> true);
            }
        });
    }

    /**
     * Aggiorna il PieChart in base alla distribuzione delle recensioni
     */
    private void aggiornaPieChart() {
        pieChart.getData().clear(); // Pulisce i dati precedenti
        recensioniMap.forEach((stelle, numeroRecensioni) -> {
            pieChart.getData().add(new PieChart.Data(stelle + " ⭐", numeroRecensioni));
        });
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

