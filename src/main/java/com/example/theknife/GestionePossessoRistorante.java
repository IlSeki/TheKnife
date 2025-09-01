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
 * NOTA: Questa classe è ora deprecata in favore di GestioneRistorante
 * che gestisce anche la proprietà dei ristoranti in modo più efficiente.
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

    public List<String> getOwnedRestaurants(String username) {
        return ownershipMap.getOrDefault(username, new ArrayList<>());
    }

    public boolean isOwner(String username, String ristoranteId) {
        List<String> ownedRestaurants = ownershipMap.get(username);
        return ownedRestaurants != null && ownedRestaurants.contains(ristoranteId);
    }

    /**
     * Associa un ristorante a un proprietario.
     */
    public void associaRistoranteAProprietario(String ristoranteNome, String username) {
        try (FileWriter writer = new FileWriter(OWNERSHIP_FILE_PATH, true)) {
            writer.write(String.format("%s,%s%n", username, ristoranteNome));
            ownershipMap.computeIfAbsent(username, k -> new ArrayList<>()).add(ristoranteNome);
        } catch (IOException e) {
            System.err.println("Errore durante l'associazione del ristorante: " + e.getMessage());
        }
    }

    public void refreshOwnershipData() {
        loadOwnershipData();
    }
}