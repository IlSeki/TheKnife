package com.example.theknife;

import java.io.*;
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
 * <p>
 * Implementa il pattern Singleton per garantire un'unica istanza in tutta l'applicazione.
 * Permette di:
 * <ul>
 *     <li>Caricare e salvare recensioni da/verso CSV</li>
 *     <li>Aggiungere, modificare o eliminare recensioni</li>
 *     <li>Gestire le risposte dei ristoratori</li>
 *     <li>Calcolare la media delle stelle per un ristorante</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi
 * @author Flavio Marin
 * @author Matilde Lecchi
 * @author Davide Caccia
 * @version 1.0
 * @since 2025-05-20
 */
public class GestioneRecensioni {
    private static GestioneRecensioni instance;
    private final Map<String, List<Recensione>> recensioniMap = new HashMap<>();
    private final GestionePossessoRistorante ownershipService = GestionePossessoRistorante.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObservableList<Recensione> allRecensioni;

    /**
     * Costruttore privato per il pattern Singleton.
     * Inizializza la mappa e la lista osservabile e carica le recensioni dal CSV.
     */
    private GestioneRecensioni() {
        allRecensioni = FXCollections.observableArrayList();
        caricaRecensioni();
    }

    /**
     * Restituisce l'istanza singleton del servizio.
     *
     * @return L'unica istanza di GestioneRecensioni
     */
    public static GestioneRecensioni getInstance() {
        if (instance == null) {
            instance = new GestioneRecensioni();
        }
        return instance;
    }

    /**
     * Carica tutte le recensioni dal file CSV.
     * <p>
     * Supporta caratteri speciali UTF-8 e campi di testo con virgole.
     * Aggiorna la mappa e la lista osservabile.
     * </p>
     */

    private void caricaRecensioni() {
        recensioniMap.clear();
        allRecensioni.clear();

        // Definisci il percorso del file esterno
        String filePath = "data/recensioni.csv";
        File csvFile = new File(filePath);

        // Controlla e crea la cartella 'data' se non esiste.
        File parentDir = csvFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("DEBUG: Cartella 'data' creata.");
        }

        // Se il file non esiste, lo crea con l'header
        if (!csvFile.exists()) {
            System.err.println("File recensioni.csv non trovato. Creazione di un nuovo file.");
            try (FileWriter writer = new FileWriter(csvFile, StandardCharsets.UTF_8)) {
                writer.append("username,ristorante,stelle,testo,data,risposta\n");
                System.out.println("DEBUG: Nuovo file recensioni.csv creato con header.");
            } catch (IOException e) {
                System.err.println("Errore durante la creazione del file recensioni.csv: " + e.getMessage());
            }
            return; // Restituisce una lista vuota dopo aver creato il file
        }

        // Se il file esiste, procedi con la lettura.
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Salta l'header
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
        } catch (IOException e) {
            System.err.println("Errore nel caricamento delle recensioni: " + e.getMessage());
        }
    }

    /**
     * Salva tutte le recensioni su file CSV.
     * <p>
     * I campi di testo che contengono virgole vengono racchiusi tra virgolette.
     * </p>
     */
    private void salvaRecensioni() {
        // Usa un percorso esterno invece del percorso di sviluppo
        String filePath = "data/recensioni.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("username,ristoranteId,stelle,testo,data,risposta");

            // La logica di scrittura dei dati rimane invariata
            recensioniMap.values().stream()
                    .flatMap(List::stream)
                    .forEach(r -> writer.println(String.format("%s,%s,%d,\"%s\",%s,\"%s\"",
                            r.getUsername(),
                            r.getRistoranteId(),
                            r.getStelle(),
                            r.getTesto(),
                            r.getData(),
                            r.getRisposta())));

            System.out.println("Recensioni salvate con successo nel file: " + filePath);

        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle recensioni: " + e.getMessage());
        }
    }

    /**
     * Aggiunge una nuova recensione e aggiorna la data corrente.
     * Aggiorna sia la mappa che la lista osservabile e salva su CSV.
     *
     * @param recensione La recensione da aggiungere
     */
    public void aggiungiRecensione(Recensione recensione) {
        recensione.setData(LocalDateTime.now().format(formatter));
        recensioniMap.computeIfAbsent(recensione.getRistoranteId(), _ -> new ArrayList<>()).add(recensione);
        allRecensioni.add(recensione);
        salvaRecensioni();
    }

    /**
     * Modifica una recensione esistente.
     *
     * @param username l'utente autore della recensione
     * @param ristoranteId l'ID del ristorante recensito
     * @param nuovoTesto il nuovo testo della recensione
     * @param nuoveStelle il nuovo numero di stelle
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
     * Elimina una recensione di un utente per un ristorante.
     *
     * @param username l'utente autore della recensione
     * @param ristoranteId l'ID del ristorante
     */
    public void eliminaRecensione(String username, String ristoranteId) {
        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni != null) {
            recensioni.removeIf(r -> r.getUsername().equals(username) && r.getRistoranteId().equals(ristoranteId));
            salvaRecensioni();
        }
    }

    /**
     * Aggiunge una risposta a una recensione.
     * <p>
     * Solo il proprietario del ristorante può rispondere.
     * </p>
     *
     * @param username l'autore della recensione a cui rispondere
     * @param ristoranteId l'ID del ristorante
     * @param risposta testo della risposta
     * @throws IllegalStateException se l'utente corrente non è proprietario
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
     * Restituisce tutte le recensioni di un ristorante.
     * <p>
     * Carica sempre le recensioni aggiornate dal CSV.
     * </p>
     *
     * @param nomeRistorante l'ID del ristorante
     * @return lista delle recensioni
     */
    public List<Recensione> getRecensioniRistorante(String nomeRistorante) {
        caricaRecensioni();
        return recensioniMap.getOrDefault(nomeRistorante, new ArrayList<>());
    }

    /**
     * Restituisce tutte le recensioni fatte da un utente.
     * <p>
     * Carica sempre le recensioni aggiornate dal CSV.
     * </p>
     *
     * @param username l'utente autore delle recensioni
     * @return lista delle recensioni dell'utente
     */
    public List<Recensione> getRecensioniUtente(String username) {
        caricaRecensioni();
        return recensioniMap.values().stream()
            .flatMap(List::stream)
            .filter(r -> r.getUsername().equals(username))
            .collect(Collectors.toList());
    }

    /**
     * Calcola la media delle stelle per un ristorante.
     *
     * @param ristoranteId l'ID del ristorante
     * @return media delle stelle (0.0 se non ci sono recensioni)
     */
    public double getMediaStelleRistorante(String ristoranteId) {
        caricaRecensioni();
        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni == null || recensioni.isEmpty()) {
            return 0.0;
        }
        return recensioni.stream()
            .mapToInt(Recensione::getStelle)
            .average()
            .orElse(0.0);
    }

    /**
     * Controlla se un utente è l'autore di una recensione.
     *
     * @param username l'username da verificare
     * @param recensione la recensione da controllare
     * @return true se l'utente è l'autore, false altrimenti
     */
    public boolean isRecensioneOwner(String username, Recensione recensione) {
        return recensione != null && recensione.getUsername().equals(username);
    }
}