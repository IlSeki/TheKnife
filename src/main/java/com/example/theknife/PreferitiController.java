package com.example.theknife;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

/**
 * Controller per la gestione dei ristoranti preferiti dell'utente.
 * <p>
 * Questa classe gestisce l'interfaccia che mostra la lista dei ristoranti
 * salvati come preferiti dall'utente. Permette di:
 * <ul>
 *     <li>Visualizzare la lista dei preferiti</li>
 *     <li>Aprire i dettagli di un ristorante con doppio click</li>
 *     <li>Rimuovere ristoranti dalla lista dei preferiti</li>
 *     <li>Aggiornare dinamicamente la lista in caso di modifiche</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi, 761031
 * @author Flavio Marin, 759910
 * @author Matilde Lecchi, 759875
 * @author Davide Caccia, 760742
 * @version 1.0
 * @since 2025-05-20
 */
public class PreferitiController implements Initializable {
    @FXML private ListView<Ristorante> preferitiListView;
    private final GestionePreferiti gestionePreferiti = com.example.theknife.GestionePreferiti.getInstance();
    private final GestioneRistorante gestioneRistorante = GestioneRistorante.getInstance();
    private HostServices hostServices;

    /**
     * Inizializza il controller configurando la ListView dei preferiti.
     * Imposta il gestore per il doppio click per aprire i dettagli
     * del ristorante e configura il cell factory con pulsante di rimozione.
     *
     * @param location  URL della risorsa FXML (non utilizzato)
     * @param resources Risorse per l'inizializzazione (non utilizzate)
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configura il doppio click sulla ListView
        preferitiListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Ristorante ristorante = preferitiListView.getSelectionModel().getSelectedItem();
                if (ristorante != null) {
                    openRestaurantInfo(ristorante);
                }
            }
        });

        // Configura la ListView
        preferitiListView.setCellFactory(__ -> new ListCell<>() {
            @Override
            protected void updateItem(Ristorante item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Crea una label con le informazioni del ristorante
                    String info = String.format("%s - %s (%s)", 
                        item.getNome(), 
                        item.getLocalita(), 
                        item.getCucina()
                    );
                    
                    // Crea il pulsante per rimuovere dai preferiti
                    Button removeButton = new Button("❌");
                    removeButton.setOnAction(e -> rimuoviPreferito(item));

                    // Inserisci label e pulsante di rimozione in un contenitore orizzontale
                    HBox container = new HBox(10);
                    Label infoLabel = new Label(info);
                    infoLabel.setStyle("-fx-padding: 5px 0;"); // Aggiunge padding verticale per allineare con il pulsante
                    container.getChildren().addAll(infoLabel, removeButton);
                    setGraphic(container);
                }
            }
        });

        // Carica i preferiti dell'utente corrente
        caricaPreferiti();
    }

    /**
     * Imposta i servizi host necessari per aprire link esterni.
     *
     * @param hostServices I servizi host di JavaFX
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Carica la lista dei ristoranti preferiti dell'utente corrente.
     * Recupera l'username dalla sessione, ottiene i preferiti dal GestionePreferiti
     * e i dettagli dei ristoranti dal GestioneRistorante.
     */
    private void caricaPreferiti() {
        String username = SessioneUtente.getUsernameUtente();
        if (username == null) return;

        Set<String> preferiti = gestionePreferiti.getPreferiti(username);
        List<Ristorante> ristoranti = gestioneRistorante.getRistorantiByNomi(preferiti);
        
        ObservableList<Ristorante> items = FXCollections.observableArrayList(ristoranti);
        preferitiListView.setItems(items);
    }

    /**
     * Apre una nuova finestra o cambia la scena per mostrare i dettagli
     * del ristorante selezionato.
     *
     * @param ristorante Il ristorante di cui visualizzare i dettagli
     */
    private void openRestaurantInfo(Ristorante ristorante) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ristorante-detail.fxml"));
            Parent root = loader.load();
            RistoranteDetailController controller = loader.getController();
            if (hostServices != null) {
                controller.setHostServices(hostServices);
            }
            controller.setRistorante(ristorante);
            // Salva il root originale e passalo al dettaglio
            Parent rootToRestore = preferitiListView.getScene().getRoot();
            controller.setRootToRestore(rootToRestore);
            controller.setTornaAlMenuPrincipaleCallback(() -> {
                Scene scene = root.getScene();
                scene.setRoot(rootToRestore);
                this.refreshData();
            });
            // Scene switch (finestra singola)
            Scene scene = preferitiListView.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della pagina dettagli: " + e.getMessage());
            e.printStackTrace();
            mostraErrore("Errore", "Impossibile aprire i dettagli del ristorante");
        }
    }

    /**
     * Rimuove un ristorante dai preferiti dell'utente corrente.
     * Aggiorna sia il servizio delle preferenze che la visualizzazione.
     *
     * @param ristorante Il ristorante da rimuovere dai preferiti
     */
    private void rimuoviPreferito(Ristorante ristorante) {
        String username = SessioneUtente.getUsernameUtente();
        if (username == null) return;

        gestionePreferiti.rimuoviPreferito(username, ristorante.getNome());
        caricaPreferiti();
    }

    /**
     * Aggiorna dinamicamente la lista dei ristoranti preferiti.
     * Recupera la lista aggiornata dei preferiti dal GestionePreferiti
     * e aggiorna la ListView.
     */
    public void refreshData() {
        String username = SessioneUtente.getUsernameUtente();
        if (username == null) return;
        Set<String> preferiti = gestionePreferiti.getPreferiti(username);
        List<Ristorante> ristoranti = gestioneRistorante.getRistorantiByNomi(preferiti);
        ObservableList<Ristorante> items = FXCollections.observableArrayList(ristoranti);
        preferitiListView.setItems(items);
    }

    /**
     * Mostra un messaggio di errore all'utente tramite un Alert JavaFX.
     *
     * @param titolo   Il titolo della finestra di alert
     * @param messaggio Il contenuto del messaggio di errore
     */
    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}