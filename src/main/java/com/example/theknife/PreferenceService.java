package com.example.theknife;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Servizio per la gestione delle preferenze degli utenti.
 * Implementa il pattern Singleton e gestisce i ristoranti preferiti
 * degli utenti, mantenendo la persistenza su file CSV.
 *
 * <p>
 * Fornisce metodi per aggiungere, rimuovere, recuperare e verificare i ristoranti preferiti
 * di ciascun utente. Garantisce la sincronizzazione dei dati con il file CSV.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class PreferenceService {
    private static final String CSV_FILE = "src/main/resources/data/preferiti.csv";
    private static final String CSV_HEADER = "username,ristoranteId";
    private static PreferenceService instance;
    private final Map<String, Set<String>> preferitiPerUtente;

    /**
     * Costruttore privato per il pattern Singleton.
     * Inizializza la mappa dei preferiti e carica i dati dal CSV.
     */
    private PreferenceService() {
        preferitiPerUtente = new HashMap<>();
        caricaPreferiti();
    }

    /**
     * Restituisce l'istanza singola del servizio.
     *
     * @return istanza di {@link PreferenceService}
     */
    public static PreferenceService getInstance() {
        if (instance == null) {
            instance = new PreferenceService();
        }
        return instance;
    }

    /**
     * Carica i preferiti dal file CSV.
     * Se il file non esiste, viene creato con l'intestazione.
     */
    private void caricaPreferiti() {
        preferitiPerUtente.clear();
        File file = new File(CSV_FILE);
        
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write(CSV_HEADER + "\n");
                }
            } catch (IOException e) {
                System.err.println("Errore nella creazione del file preferiti: " + e.getMessage());
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 2) {
                    String username = values[0];
                    String ristoranteId = values[1];
                    preferitiPerUtente
                        .computeIfAbsent(username, k -> new HashSet<>())
                        .add(ristoranteId);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file preferiti: " + e.getMessage());
        }
    }

    /**
     * Salva i preferiti correnti su file CSV.
     */
    private void salvaPreferiti() {
        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            writer.write(CSV_HEADER + "\n");
            for (Map.Entry<String, Set<String>> entry : preferitiPerUtente.entrySet()) {
                String username = entry.getKey();
                for (String ristoranteId : entry.getValue()) {
                    writer.write(username + "," + ristoranteId + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dei preferiti: " + e.getMessage());
        }
    }

    /**
     * Aggiunge un ristorante ai preferiti di un utente e aggiorna il CSV.
     *
     * @param username    nome dell'utente
     * @param ristoranteId ID del ristorante da aggiungere
     */
    public void aggiungiPreferito(String username, String ristoranteId) {
        preferitiPerUtente
            .computeIfAbsent(username, k -> new HashSet<>())
            .add(ristoranteId);
        salvaPreferiti();
    }

    /**
     * Rimuove un ristorante dai preferiti di un utente e aggiorna il CSV.
     *
     * @param username    nome dell'utente
     * @param ristoranteId ID del ristorante da rimuovere
     */
    public void rimuoviPreferito(String username, String ristoranteId) {
        if (preferitiPerUtente.containsKey(username)) {
            preferitiPerUtente.get(username).remove(ristoranteId);
            salvaPreferiti();
        }
    }

    /**
     * Recupera tutti i ristoranti preferiti di un utente.
     * I dati vengono aggiornati dal CSV prima del ritorno.
     *
     * @param username nome dell'utente
     * @return insieme degli ID dei ristoranti preferiti
     */
    public Set<String> getPreferiti(String username) {
        caricaPreferiti();
        return preferitiPerUtente.getOrDefault(username, new HashSet<>());
    }

    /**
     * Verifica se un ristorante è tra i preferiti di un utente.
     * I dati vengono aggiornati dal CSV prima della verifica.
     *
     * @param username    nome dell'utente
     * @param ristoranteId ID del ristorante da verificare
     * @return true se il ristorante è tra i preferiti, false altrimenti
     */
    public boolean isPreferito(String username, String ristoranteId) {
        caricaPreferiti();
        return preferitiPerUtente.containsKey(username) &&
               preferitiPerUtente.get(username).contains(ristoranteId);
    }
}