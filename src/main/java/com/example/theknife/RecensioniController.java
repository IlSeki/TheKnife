package com.example.theknife;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
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

    // Mappa che tiene traccia del numero di recensioni per ogni punteggio di stelle
    private final Map<Integer, Integer> recensioniMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Popola la ComboBox per permettere la selezione del numero di stelle
        comboBox.getItems().addAll(1, 2, 3, 4, 5);
        comboBox.setPromptText("Seleziona numero stelle");
        //comboBox.getSelectionModel().selectFirst(); // Default: prima opzione

        //SIMULAZIONE, QUESTO POI ANDRA' CAMBIATO
        // Simuliamo un set di recensioni (può essere sostituito con dati reali)
        recensioniMap.put(8, 2); // 2 recensioni con 1 stella
        recensioniMap.put(2, 5); // 5 recensioni con 2 stelle
        recensioniMap.put(3, 3); // 3 recensioni con 3 stelle
        recensioniMap.put(4, 4); // 4 recensioni con 4 stelle
        recensioniMap.put(5, 3); // 3 recensioni con 5 stelle

        // Popola il PieChart basandosi sulla distribuzione delle recensioni
        aggiornaPieChart();
    }

    /**
     * Metodo per aggiornare il PieChart in base ai dati delle recensioni
     */
    private void aggiornaPieChart() {
        pieChart.getData().clear(); // Pulisce i dati precedenti
        recensioniMap.forEach((stelle, numeroRecensioni) -> {
            pieChart.getData().add(new PieChart.Data(stelle + " ⭐", numeroRecensioni));
        });
    }
}

