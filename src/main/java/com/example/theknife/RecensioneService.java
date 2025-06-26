package com.example.theknife;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servizio per la gestione delle recensioni dei ristoranti.
 * Implementa il pattern Singleton per garantire un'unica istanza del servizio
 * in tutta l'applicazione.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RecensioneService {
    private static RecensioneService instance;
    private final Map<String, List<Recensione>> recensioniMap = new HashMap<>();
    private final RistoranteOwnershipService ownershipService = RistoranteOwnershipService.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObservableList<Recensione> allRecensioni;

    /**
     * Costruttore privato per implementare il pattern Singleton.
     * Inizializza le strutture dati e carica le recensioni dal file CSV.
     */
    private RecensioneService() {
        allRecensioni = FXCollections.observableArrayList();
        caricaRecensioni();
    }

    /**
     * Restituisce l'istanza singleton del servizio.
     *
     * @return L'unica istanza di RecensioneService
     */
    public static RecensioneService getInstance() {
        if (instance == null) {
            instance = new RecensioneService();
        }
        return instance;
    }

    /**
     * Carica tutte le recensioni dal file CSV in memoria.
     * Legge il file utilizzando UTF-8 per supportare caratteri speciali
     * e gestisce correttamente le virgole nei campi di testo usando una regex.
     * Per ogni recensione caricata:
     * <ul>
     *   <li>Crea un nuovo oggetto Recensione</li>
     *   <li>Imposta la data originale</li>
     *   <li>Aggiunge eventuali risposte dei ristoratori</li>
     *   <li>Memorizza la recensione nella mappa e nella lista osservabile</li>
     * </ul>
     */
    private void caricaRecensioni() {
        try (InputStream is = getClass().getResourceAsStream("/data/recensioni.csv")) {
            if (is == null) {
                System.err.println("File recensioni.csv non trovato");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line = reader.readLine(); // Skip header
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (parts.length >= 5) {
                        String username = parts[0].trim();
                        String ristoranteId = parts[1].trim();
                        int stelle = Integer.parseInt(parts[2].trim());
                        String testo = parts[3].replace("\"", "").trim();
                        String data = parts[4].trim();
                        String risposta = parts.length > 5 ? parts[5].replace("\"", "").trim() : "";

                        Recensione recensione = new Recensione(stelle, testo, ristoranteId, username);
                        recensione.setData(data);
                        if (!risposta.isEmpty()) {
                            recensione.setRisposta(risposta);
                        }

                        recensioniMap.computeIfAbsent(ristoranteId, _ -> new ArrayList<>()).add(recensione);
                        allRecensioni.add(recensione);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel caricamento delle recensioni: " + e.getMessage());
        }
    }

    /**
     * Salva tutte le recensioni su file CSV.
     * Scrive l'header del CSV e poi tutte le recensioni nel formato:
     * username,ristoranteId,stelle,testo,data,risposta
     * Gestisce correttamente le virgole nei campi di testo usando le virgolette.
     */
    private void salvaRecensioni() {
        String filePath = "src/main/resources/data/recensioni.csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("username,ristoranteId,stelle,testo,data,risposta");
            recensioniMap.values().stream()
                .flatMap(List::stream)
                .forEach(r -> writer.println(String.format("%s,%s,%d,\"%s\",%s,\"%s\"",
                    r.getUsername(),
                    r.getRistoranteId(),
                    r.getStelle(),
                    r.getTesto(),
                    r.getData(),
                    r.getRisposta())));
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle recensioni: " + e.getMessage());
        }
    }

    /**
     * Aggiunge una nuova recensione.
     * Imposta la data corrente e aggiorna sia la mappa che la lista osservabile.
     *
     * @param recensione La nuova recensione da aggiungere
     */
    public void aggiungiRecensione(Recensione recensione) {
        recensione.setData(LocalDateTime.now().format(formatter));
        recensioniMap.computeIfAbsent(recensione.getRistoranteId(), _ -> new ArrayList<>()).add(recensione);
        allRecensioni.add(recensione);
        salvaRecensioni();
    }

    /**
     * Modifica una recensione esistente
     */
    public void modificaRecensione(String username, String ristoranteId, String nuovoTesto, int nuoveStelle) {
        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni != null) {
            for (Recensione r : recensioni) {
                if (r.getUsername().equals(username) && r.getRistoranteId().equals(ristoranteId)) {
                    r.setTesto(nuovoTesto);
                    r.setStelle(nuoveStelle);
                    r.setData(LocalDateTime.now().format(formatter));
                    break;
                }
            }
            salvaRecensioni();
        }
    }

    /**
     * Elimina una recensione
     */
    public void eliminaRecensione(String username, String ristoranteId) {
        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni != null) {
            recensioni.removeIf(r -> r.getUsername().equals(username) && r.getRistoranteId().equals(ristoranteId));
            salvaRecensioni();
        }
    }

    /**
     * Aggiunge una risposta a una recensione
     */
    public void aggiungiRisposta(String username, String ristoranteId, String risposta) {
        String currentUser = SessioneUtente.getUsernameUtente();
        if (!ownershipService.isOwner(currentUser, ristoranteId)) {
            throw new IllegalStateException("Non sei autorizzato a rispondere a questa recensione");
        }

        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni != null) {
            for (Recensione r : recensioni) {
                if (r.getUsername().equals(username) && r.getRistoranteId().equals(ristoranteId)) {
                    r.setRisposta(risposta);
                    break;
                }
            }
            salvaRecensioni();
        }
    }

    /**
     * Salva la risposta a una recensione.
     * Aggiorna il file CSV con la nuova risposta.
     *
     * @param recensione La recensione con la risposta aggiornata
     */
    public void salvaRispostaRecensione(Recensione recensione) {
        // La risposta è già stata aggiornata nell'oggetto Recensione
        // Dobbiamo solo salvare il file CSV
        salvaRecensioni();
    }

    /**
     * Recupera tutte le recensioni per un dato ristorante.
     *
     * @param nomeRistorante Il nome del ristorante
     * @return Lista delle recensioni del ristorante
     */
    public List<Recensione> getRecensioniRistorante(String nomeRistorante) {
        return recensioniMap.getOrDefault(nomeRistorante, new ArrayList<>());
    }

    /**
     * Recupera tutte le recensioni fatte da un utente.
     *
     * @param username L'username dell'utente
     * @return Lista delle recensioni fatte dall'utente
     */
    public List<Recensione> getRecensioniUtente(String username) {
        return recensioniMap.values().stream()
            .flatMap(List::stream)
            .filter(r -> r.getUsername().equals(username))
            .collect(Collectors.toList());
    }

    /**
     * Calcola la media delle stelle per un ristorante
     */
    public double getMediaStelleRistorante(String ristoranteId) {
        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni == null || recensioni.isEmpty()) {
            return 0.0;
        }
        return recensioni.stream()
            .mapToInt(Recensione::getStelle)
            .average()
            .orElse(0.0);
    }

    public boolean isRecensioneOwner(String username, Recensione recensione) {
        return recensione != null && recensione.getUsername().equals(username);
    }
}