package com.example.theknife;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Servizio per la gestione delle preferenze degli utenti.
 * Implementa il pattern Singleton e gestisce i ristoranti preferiti
 * degli utenti, mantenendo la persistenza su file CSV.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class GestionePreferiti {
    private static final String CSV_FILE = "data/preferiti.csv";
    private static final String CSV_HEADER = "username,ristoranteId";

    private static GestionePreferiti instance;
    private final Map<String, Set<String>> preferitiPerUtente = new HashMap<>();

    private GestionePreferiti() {
        caricaPreferiti();
    }

    public static GestionePreferiti getInstance() {
        if (instance == null) {
            instance = new GestionePreferiti();
        }
        return instance;
    }

    /**
     * Carica i preferiti dal file CSV.
     */
    private void caricaPreferiti() {
        preferitiPerUtente.clear();
        File file = new File(CSV_FILE);

        if (!file.exists()) {
            createPreferitiFile(file);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                processPreferitiLine(line);
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file preferiti: " + e.getMessage());
        }
    }

    /**
     * Crea il file dei preferiti con header se non esiste.
     */
    private void createPreferitiFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(CSV_HEADER + "\n");
            }
        } catch (IOException e) {
            System.err.println("Errore nella creazione del file preferiti: " + e.getMessage());
        }
    }

    /**
     * Processa una singola riga del file preferiti.
     */
    private void processPreferitiLine(String line) {
        String[] values = line.split(",");
        if (values.length >= 2) {
            String username = values[0].trim();
            String ristoranteId = values[1].trim();
            preferitiPerUtente.computeIfAbsent(username, k -> new HashSet<>()).add(ristoranteId);
        }
    }

    /**
     * Salva i preferiti correnti su file CSV.
     */
    private void salvaPreferiti() {
        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            writer.write(CSV_HEADER + "\n");
            preferitiPerUtente.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream()
                            .map(ristoranteId -> entry.getKey() + "," + ristoranteId))
                    .forEach(line -> {
                        try {
                            writer.write(line + "\n");
                        } catch (IOException e) {
                            System.err.println("Errore nella scrittura: " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dei preferiti: " + e.getMessage());
        }
    }

    /**
     * Aggiunge un ristorante ai preferiti di un utente.
     */
    public void aggiungiPreferito(String username, String ristoranteId) {
        preferitiPerUtente.computeIfAbsent(username, k -> new HashSet<>()).add(ristoranteId);
        salvaPreferiti();
    }

    /**
     * Rimuove un ristorante dai preferiti di un utente.
     */
    public void rimuoviPreferito(String username, String ristoranteId) {
        Set<String> preferiti = preferitiPerUtente.get(username);
        if (preferiti != null) {
            preferiti.remove(ristoranteId);
            salvaPreferiti();
        }
    }

    /**
     * Recupera tutti i ristoranti preferiti di un utente.
     */
    public Set<String> getPreferiti(String username) {
        caricaPreferiti(); // Ricarica per avere dati aggiornati
        return preferitiPerUtente.getOrDefault(username, new HashSet<>());
    }

    /**
     * Verifica se un ristorante è tra i preferiti di un utente.
     */
    public boolean isPreferito(String username, String ristoranteId) {
        caricaPreferiti();
        Set<String> preferiti = preferitiPerUtente.get(username);
        return preferiti != null && preferiti.contains(ristoranteId);
    }
}