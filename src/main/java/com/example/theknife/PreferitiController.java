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
 * Gestisce l'interfaccia che mostra la lista dei ristoranti salvati come preferiti
 * e permette di visualizzarne i dettagli o rimuoverli dai preferiti.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class PreferitiController implements Initializable {
    @FXML private ListView<Ristorante> preferitiListView;
    private final PreferenceService preferenceService = PreferenceService.getInstance();
    private final RistoranteService ristoranteService = RistoranteService.getInstance();
    private HostServices hostServices;

    /**
     * Inizializza il controller configurando la ListView dei preferiti.
     * Imposta il gestore per il doppio click che apre i dettagli del ristorante
     * e configura il factory delle celle per mostrare le informazioni dei ristoranti
     * con il pulsante di rimozione.
     *
     * @param location  L'URL di localizzazione della risorsa FXML (non utilizzato)
     * @param resources Le risorse aggiuntive per l'inizializzazione (non utilizzate)
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
     * @param hostServices I servizi host di JavaFX per aprire link esterni
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Carica la lista dei ristoranti preferiti dell'utente corrente.
     * Recupera l'username dell'utente dalla sessione, ottiene la lista dei preferiti
     * dal PreferenceService e carica i dettagli dei ristoranti tramite il RistoranteService.
     */
    private void caricaPreferiti() {
        String username = SessioneUtente.getUsernameUtente();
        if (username == null) return;

        Set<String> preferiti = preferenceService.getPreferiti(username);
        List<Ristorante> ristoranti = ristoranteService.getRistorantiByNomi(preferiti);
        
        ObservableList<Ristorante> items = FXCollections.observableArrayList(ristoranti);
        preferitiListView.setItems(items);
    }

    /**
     * Apre una nuova finestra con i dettagli del ristorante selezionato.
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
     * Rimuove un ristorante dalla lista dei preferiti dell'utente.
     * Aggiorna sia il servizio delle preferenze che la visualizzazione.
     *
     * @param ristorante Il ristorante da rimuovere dai preferiti
     */
    private void rimuoviPreferito(Ristorante ristorante) {
        String username = SessioneUtente.getUsernameUtente();
        if (username == null) return;

        preferenceService.rimuoviPreferito(username, ristorante.getNome());
        caricaPreferiti();
    }

    /**
     * Aggiorna dinamicamente la lista dei ristoranti preferiti.
     * Recupera la lista aggiornata dei preferiti dal PreferenceService
     * e aggiorna la ListView per riflettere le modifiche.
     */
    public void refreshData() {
        String username = SessioneUtente.getUsernameUtente();
        if (username == null) return;
        Set<String> preferiti = preferenceService.getPreferiti(username);
        List<Ristorante> ristoranti = ristoranteService.getRistorantiByNomi(preferiti);
        ObservableList<Ristorante> items = FXCollections.observableArrayList(ristoranti);
        preferitiListView.setItems(items);
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}