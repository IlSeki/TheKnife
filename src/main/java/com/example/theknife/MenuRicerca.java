package com.example.theknife;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
 * tramite un TextField che filtra dinamicamente i risultati e un MenuButton per ordinare.
 *
 * Inoltre, per ogni ristorante viene aggiunto un pulsante [info] che, una volta cliccato, ti porterà alla pagina
 * con le informazioni sul ristorante (pagina già esistente, che potrai collegare).
 *
 * @author Samuele
 * @version 1.0
 */
public class MenuRicerca implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Ristorante> resultList;

    @FXML
    private MenuButton filterMenu;  // Assicurati che l'FXML contenga questo componente

    // Lista principale di tutti i ristoranti
    private ObservableList<Ristorante> tuttiRistoranti;

    // Lista filtrata per la ricerca
    private FilteredList<Ristorante> ristorantiFiltrati;

    // Lista ordinata che incapsula la lista filtrata
    private SortedList<Ristorante> sortedData;

    /**
     * Inizializza il controller caricando i dati dal CSV, impostando il filtro di ricerca ed
     * configurando gli eventi per il MenuButton che gestisce l'ordinamento.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Carica i ristoranti dal file CSV
        caricaRistorantiDaCSV();

        // Crea la FilteredList utilizzando la lista principale
        ristorantiFiltrati = new FilteredList<>(tuttiRistoranti, ristorante -> true);

        // Crea la SortedList incapsulando la FilteredList
        sortedData = new SortedList<>(ristorantiFiltrati);

        // Imposta la ListView per mostrare la SortedList
        resultList.setItems(sortedData);

        // Imposta un CellFactory personalizzato per aggiungere il pulsante [info] in ogni cella
        resultList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Ristorante item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Crea una label con le informazioni del ristorante (usa il toString() o personalizza come preferisci)
                    Label infoLabel = new Label(item.toString());

                    // Crea il pulsante [info] e definisci l'azione da eseguire al click
                    Button infoButton = new Button("[info]");
                    infoButton.setOnAction(e -> openRestaurantInfo(item));

                    // Inserisci label e pulsante in un contenitore orizzontale
                    HBox container = new HBox(infoLabel, infoButton);
                    container.setSpacing(10);

                    setGraphic(container);
                }
            }
        });

        // Aggiunge il listener per la ricerca in tempo reale: modifica il predicato della FilteredList
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                filtraRistoranti(newValue);
            }
        });

        // Imposta gli handler per i MenuItem del MenuButton per l'ordinamento
        for (MenuItem item : filterMenu.getItems()) {
            item.setOnAction(event -> {
                String filterText = item.getText();
                switch (filterText) {
                    case "Per Nome":
                        sortedData.setComparator((r1, r2) -> r1.getNome().compareToIgnoreCase(r2.getNome()));
                        break;
                    case "Per Città":
                        sortedData.setComparator((r1, r2) -> r1.getCitta().compareToIgnoreCase(r2.getCitta()));
                        break;
                    case "Per Tipo Cucina":
                        sortedData.setComparator((r1, r2) -> r1.getTipo().compareToIgnoreCase(r2.getTipo()));
                        break;
                    default:
                        sortedData.setComparator(null);
                        break;
                }
                // Aggiorna il testo del MenuButton per indicare il criterio di ordinamento selezionato
                filterMenu.setText(filterText);
            });
        }
    }

    /**
     * Metodo di supporto per aprire la pagina di informazioni sul ristorante.
     * Qui puoi implementare la logica per il passaggio alla pagina info, ad esempio caricando l'FXML corrispondente.
     *
     * @param ristorante il ristorante di cui visualizzare le informazioni.
     */
    private void openRestaurantInfo(Ristorante ristorante) {
        // Qui inserisci il codice per aprire la pagina delle informazioni.
        // Ad esempio, potresti usare FXMLLoader per caricare la scena e passarle i dati del ristorante.
        System.out.println("Apertura info per: " + ristorante.getNome());
        // Esempio di stub:
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("restaurantInfo.fxml"));
        // Parent root = loader.load();
        // RestaurantInfoController controller = loader.getController();
        // controller.setRistorante(ristorante);
        // Stage stage = new Stage();
        // stage.setScene(new Scene(root));
        // stage.show();
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
            aggiungiRistorantiEsempio();
        }
    }

    /**
     * Parsing di una riga del CSV per creare un oggetto Ristorante.
     * Adatta questo metodo in base alla struttura del tuo CSV.
     */
    private Ristorante parseRigaCSV(String line) {
        try {
            // Split della riga considerando le virgole (e gestendo le virgole dentro le virgolette)
            String[] parts = parseCSVLine(line);

            if (parts.length >= 2) {
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
     * Parsing più robusto per gestire CSV con virgolette.
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
     * Filtra i ristoranti in base al testo inserito nel campo di ricerca.
     */
    private void filtraRistoranti(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            ristorantiFiltrati.setPredicate(ristorante -> true);
        } else {
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
     * Aggiunge alcuni ristoranti di esempio se il CSV non viene trovato.
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
     * Classe interna che rappresenta un ristorante.
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
