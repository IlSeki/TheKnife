package com.example.theknife;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistoranteOwnershipService {
    private static RistoranteOwnershipService instance;
    private final Map<String, List<String>> ownershipMap = new HashMap<>();
    private static final String OWNERSHIP_FILE_PATH = "src/main/resources/data/proprietari_ristoranti.csv";

    private RistoranteOwnershipService() {
        loadOwnershipData();
    }

    public static RistoranteOwnershipService getInstance() {
        if (instance == null) {
            instance = new RistoranteOwnershipService();
        }
        return instance;
    }

    private void loadOwnershipData() {
        File file = new File(OWNERSHIP_FILE_PATH);
        if (!file.exists()) {
            System.err.println("File proprietari_ristoranti.csv non trovato - verrà creato quando necessario");
            try {
                // Crea il file con l'header se non esiste
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("username,ristorante\n");
                }
            } catch (IOException e) {
                System.err.println("Errore nella creazione del file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String ristoranteId = parts[1].trim();
                    // Verifica che entrambi i valori non siano vuoti
                    if (!username.isEmpty() && !ristoranteId.isEmpty()) {
                        // Verifica che il ristorante esista veramente
                        if (RistoranteService.getInstance().getRistorante(ristoranteId) != null) {
                            ownershipMap.computeIfAbsent(username, _ -> new ArrayList<>()).add(ristoranteId);
                        } else {
                            System.err.println("Ristorante non trovato nel database: " + ristoranteId);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dei dati di proprietà: " + e.getMessage());
        }
    }

    public List<String> getOwnedRestaurants(String username) {
        // Ricarica sempre i dati dal filesystem per assicurarsi di avere i dati più aggiornati
        ownershipMap.clear();
        loadOwnershipData();
        return ownershipMap.getOrDefault(username, new ArrayList<>());
    }

    public boolean isOwner(String username, String ristoranteId) {
        // Ricarica i dati per assicurarsi di avere lo stato più aggiornato
        List<String> ownedRestaurants = getOwnedRestaurants(username);
        return ownedRestaurants.contains(ristoranteId);
    }

    public void associaRistoranteAProprietario(String ristoranteNome, String username) throws IOException {
        // Crea la directory se non esiste
        File dir = new File("src/main/resources/data");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Impossibile creare la directory: " + dir.getPath());
        }

        // Verifica se il file esiste e ha l'header
        File file = new File(OWNERSHIP_FILE_PATH);
        boolean fileExists = file.exists();
        boolean hasValidHeader = false;

        if (fileExists) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream(file), StandardCharsets.UTF_8))) {
                String firstLine = reader.readLine();
                hasValidHeader = "username,ristorante".equals(firstLine);
            } catch (IOException e) {
                System.err.println("Errore nella lettura del file: " + e.getMessage());
            }
        }

        // Se il file non esiste o l'header non è valido, ricrea il file
        if (!fileExists || !hasValidHeader) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("username,ristorante\n");
            }
        }

        // Aggiungi la nuova associazione
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(String.format("%s,%s%n", username, ristoranteNome));
            
            // Aggiorna la mappa in memoria
            ownershipMap.computeIfAbsent(username, _ -> new ArrayList<>()).add(ristoranteNome);
        }
    }

    public void refreshOwnershipData() {
        ownershipMap.clear();
        loadOwnershipData();
    }
}