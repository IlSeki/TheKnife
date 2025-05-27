package com.example.theknife;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller per la gestione della ricerca dei ristoranti.
 * Permette di cercare ristoranti dal file CSV michelin_my_maps.csv
 * tramite un TextField che filtra dinamicamente i risultati.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 */
public class MenuRicerca implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Ristorante> resultList;

    // Lista principale di tutti i ristoranti
    private ObservableList<Ristorante> tuttiRistoranti;

    // Lista filtrata per la ricerca
    private FilteredList<Ristorante> ristorantiFiltrati;

    /**
     * Inizializza il controller caricando i dati dal CSV
     * e impostando i listener per la ricerca dinamica.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Carica i ristoranti dal file CSV
        caricaRistorantiDaCSV();

        // Configura la ListView con la lista filtrata
        ristorantiFiltrati = new FilteredList<>(tuttiRistoranti, ristorante -> true);
        resultList.setItems(ristorantiFiltrati);

        // Aggiunge il listener per la ricerca in tempo reale
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                filtraRistoranti(newValue);
            }
        });
    }

    /**
     * Carica i ristoranti dal file CSV michelin_my_maps.csv
     */
    private void caricaRistorantiDaCSV() {
        tuttiRistoranti = FXCollections.observableArrayList();

        try {
            // Carica il file CSV dal classpath
            InputStream inputStream = getClass().getResourceAsStream("/data/michelin_my_maps.csv");
            if (inputStream == null) {
                System.err.println("File michelin_my_maps.csv non trovato in /data/");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Salta l'header se presente
                if (isFirstLine) {
                    isFirstLine = false;
                    // Controlla se la prima riga contiene header
                    if (line.toLowerCase().contains("name") ||
                            line.toLowerCase().contains("nome") ||
                            line.toLowerCase().contains("ristorante")) {
                        continue;
                    }
                }

                // Parsing della riga CSV
                Ristorante ristorante = parseRigaCSV(line);
                if (ristorante != null) {
                    tuttiRistoranti.add(ristorante);
                }
            }

            reader.close();
            System.out.println("Caricati " + tuttiRistoranti.size() + " ristoranti dal CSV");

        } catch (IOException e) {
            System.err.println("Errore nel caricamento del file CSV: " + e.getMessage());
            e.printStackTrace();

            // Aggiungi alcuni ristoranti di esempio se il file non viene trovato
            aggiungiRistorantiEsempio();
        }
    }

    /**
     * Parsing di una riga del CSV per creare un oggetto Ristorante
     * Adatta questo metodo in base alla struttura del tuo CSV
     */
    private Ristorante parseRigaCSV(String line) {
        try {
            // Split della riga considerando le virgole (e gestendo le virgole dentro le virgolette)
            String[] parts = parseCSVLine(line);

            if (parts.length >= 2) {
                // Assumi che le prime colonne siano: nome, indirizzo, città, etc.
                // Adatta questi indici in base alla struttura del tuo CSV
                String nome = parts[0].trim().replaceAll("\"", "");
                String indirizzo = parts.length > 1 ? parts[1].trim().replaceAll("\"", "") : "";
                String citta = parts.length > 2 ? parts[2].trim().replaceAll("\"", "") : "";
                String tipo = parts.length > 3 ? parts[3].trim().replaceAll("\"", "") : "Ristorante";

                return new Ristorante(nome, indirizzo, citta, tipo);
            }
        } catch (Exception e) {
            System.err.println("Errore nel parsing della riga: " + line);
        }
        return null;
    }

    /**
     * Parsing più robusto per gestire CSV con virgolette
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        result.add(currentField.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Filtra i ristoranti in base al testo inserito nel campo di ricerca
     */
    private void filtraRistoranti(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            // Se il campo è vuoto, mostra tutti i ristoranti
            ristorantiFiltrati.setPredicate(ristorante -> true);
        } else {
            // Filtra per nome, città o tipo di cucina (case insensitive)
            String filtroLower = filtro.toLowerCase().trim();
            ristorantiFiltrati.setPredicate(ristorante ->
                    ristorante.getNome().toLowerCase().contains(filtroLower) ||
                            ristorante.getCitta().toLowerCase().contains(filtroLower) ||
                            ristorante.getTipo().toLowerCase().contains(filtroLower) ||
                            ristorante.getIndirizzo().toLowerCase().contains(filtroLower)
            );
        }
    }

    /**
     * Aggiunge alcuni ristoranti di esempio se il CSV non viene trovato
     */
    private void aggiungiRistorantiEsempio() {
        tuttiRistoranti.addAll(
                new Ristorante("Osteria Francescana", "Via Stella 22", "Modena", "Italiana"),
                new Ristorante("Le Bernardin", "155 West 51st Street", "New York", "Francese"),
                new Ristorante("Noma", "Refshalevej 96", "Copenhagen", "Nordica"),
                new Ristorante("El Celler de Can Roca", "Can Sunyer 48", "Girona", "Spagnola"),
                new Ristorante("Eleven Madison Park", "11 Madison Avenue", "New York", "Americana")
        );
    }

    /**
     * Classe che rappresenta un ristorante
     */
    public static class Ristorante {
        private String nome;
        private String indirizzo;
        private String citta;
        private String tipo;

        public Ristorante(String nome, String indirizzo, String citta, String tipo) {
            this.nome = nome != null ? nome : "";
            this.indirizzo = indirizzo != null ? indirizzo : "";
            this.citta = citta != null ? citta : "";
            this.tipo = tipo != null ? tipo : "";
        }

        // Getters
        public String getNome() { return nome; }
        public String getIndirizzo() { return indirizzo; }
        public String getCitta() { return citta; }
        public String getTipo() { return tipo; }

        // Setters
        public void setNome(String nome) { this.nome = nome; }
        public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
        public void setCitta(String citta) { this.citta = citta; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        @Override
        public String toString() {
            return nome + " - " + citta + " (" + tipo + ")";
        }
    }
}