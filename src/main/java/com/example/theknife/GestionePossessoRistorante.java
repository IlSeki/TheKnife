package com.example.theknife;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servizio per la gestione delle proprietà dei ristoranti.
 * Implementa il pattern Singleton e gestisce le associazioni tra ristoratori
 * e i loro ristoranti, mantenendo la persistenza su file CSV.
 *
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */

public class GestionePossessoRistorante {
    private static final String OWNERSHIP_FILE_PATH = "data/proprietari_ristoranti.csv";
    private static final String CSV_HEADER = "username,ristorante";

    private static GestionePossessoRistorante instance;
    private final Map<String, List<String>> ownershipMap = new HashMap<>();
    private boolean isInitialized = false;

    private GestionePossessoRistorante() {}

    public static GestionePossessoRistorante getInstance() {
        if (instance == null) {
            instance = new GestionePossessoRistorante();
        }
        return instance;
    }

    public void initialize() {
        if (!isInitialized) {
            loadOwnershipData();
            isInitialized = true;
        }
    }

    /**
     * Carica i dati di proprietà dal file CSV.
     * <p>
     * Se il file non esiste, viene creato con l'header predefinito.
     * I dati vengono memorizzati nella mappa {@code ownershipMap},
     * associando ciascun utente alla lista di ristoranti posseduti.
     * </p>
     */
    private void loadOwnershipData() {
        File file = new File(OWNERSHIP_FILE_PATH);

        if (!file.exists()) {
            createOwnershipFile(file);
            return;
        }

        ownershipMap.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                processOwnershipLine(line);
            }
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dei dati di proprietà: " + e.getMessage());
        }
    }

    /**
     * Crea il file di proprietà con header se non esiste.
     *@param file file CSV da creare
     */
    private void createOwnershipFile(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER + "\n");
        } catch (IOException e) {
            System.err.println("Errore nella creazione del file: " + e.getMessage());
        }
    }

    /**
     * Processa una singola riga del file di proprietà.
     * <p>
     * La riga deve contenere almeno due campi: username e ID del ristorante.
     * Se il ristorante non esiste nel database, l'associazione viene ignorata.
     * </p>
     *
     * @param line riga del file CSV da processare
     */
    private void processOwnershipLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 2) {
            String username = parts[0].trim();
            String ristoranteId = parts[1].trim();

            if (!username.isEmpty() && !ristoranteId.isEmpty()) {
                // Verifica che il ristorante esista prima di aggiungerlo
                if (GestioneRistorante.getInstance().getRistorante(ristoranteId) != null) {
                    ownershipMap.computeIfAbsent(username, k -> new ArrayList<>()).add(ristoranteId);
                } else {
                    System.err.println("Ristorante non trovato nel database: " + ristoranteId);
                }
            }
        }
    }
    /**
     * Restituisce la lista degli ID dei ristoranti posseduti da un utente.
     *
     * @param username username del ristoratore
     * @return lista degli ID dei ristoranti posseduti (vuota se nessuno)
     */
    public List<String> getOwnedRestaurants(String username) {
        return ownershipMap.getOrDefault(username, new ArrayList<>());
    }
    /**
     * Associa un ristorante a un proprietario, salvando l'associazione
     * sia nel file CSV che nella mappa {@code ownershipMap}.
     *
     * @param ristoranteNome nome o ID del ristorante da associare
     * @param username       nome utente del proprietario
     */
    public void associaRistoranteAProprietario(String ristoranteNome, String username) {
        try (FileWriter writer = new FileWriter(OWNERSHIP_FILE_PATH, true)) {
            writer.write(String.format("%s,%s%n", username, ristoranteNome));
            ownershipMap.computeIfAbsent(username, k -> new ArrayList<>()).add(ristoranteNome);
        } catch (IOException e) {
            System.err.println("Errore durante l'associazione del ristorante: " + e.getMessage());
        }
    }

    /**
     * Aggiorna la mappa {@code ownershipMap} rileggendo i dati
     * dal file CSV {@code OWNERSHIP_FILE_PATH}.
     */
    public void refreshOwnershipData() {
        loadOwnershipData();
    }
}