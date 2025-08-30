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
 * Gestisce operazioni quali:
 * <ul>
 * <li>Recupero dei ristoranti posseduti da un utente</li>
 * <li>Verifica se un utente è proprietario di un ristorante</li>
 * <li>Associazione di un ristorante a un proprietario</li>
 * <li>Aggiornamento dei dati dal CSV</li>
 * </ul>
 *
 * @author Samuele Secchi, 761031
 * @author Flavio Marin, 759910
 * @author Matilde Lecchi, 759875
 * @author Davide Caccia, 760742
 * @version 1.0
 * @since 2025-05-20
 */
public class GestionePossessoRistorante {
    private static GestionePossessoRistorante instance;
    private final Map<String, List<String>> ownershipMap = new HashMap<>();
    private static final String OWNERSHIP_FILE_PATH = "data/proprietari_ristoranti.csv";
    private static boolean isInitialized = false;

    /**
     * Costruttore privato per il Singleton.
     */
    private GestionePossessoRistorante() {
    }

    /**
     * Restituisce l'istanza singleton del servizio.
     *
     * @return istanza unica di {@link GestionePossessoRistorante}
     */
    public static GestionePossessoRistorante getInstance() {
        if (instance == null) {
            instance = new GestionePossessoRistorante();
        }
        return instance;
    }

    /**
     * Metodo per inizializzare esplicitamente i dati, garantendo che
     * GestioneRistorante sia già stato caricato.
     */
    public void initialize() {
        if (!isInitialized) {
            loadOwnershipData();
            isInitialized = true;
        }
    }

    /**
     * Carica i dati di proprietà dal file CSV nella mappa in memoria.
     * Se il file non esiste, lo crea con l'header corretto.
     */
    private void loadOwnershipData() {
        File file = new File(OWNERSHIP_FILE_PATH);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("DEBUG: Directory '" + parentDir.getPath() + "' creata.");
        }

        if (!file.exists()) {
            System.err.println("File " + OWNERSHIP_FILE_PATH + " non trovato. Creazione di un nuovo file.");
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write("username,ristorante\n");
                System.out.println("DEBUG: Nuovo file creato con header.");
            } catch (IOException e) {
                System.err.println("Errore nella creazione del file: " + e.getMessage());
                return;
            }
        }

        // Svuota la mappa prima di ricaricare i dati
        ownershipMap.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Salta l'header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String ristoranteId = parts[1].trim();
                    if (!username.isEmpty() && !ristoranteId.isEmpty()) {
                        if (GestioneRistorante.getInstance().getRistorante(ristoranteId) != null) {
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

    /**
     * Restituisce la lista dei ristoranti posseduti da un utente.
     * NON RICARICA PIÙ I DATI, utilizza la mappa in memoria.
     *
     * @param username username dell'utente
     * @return lista di nomi di ristoranti posseduti; lista vuota se nessun ristorante
     */
    public List<String> getOwnedRestaurants(String username) {
        // Usa direttamente la mappa in memoria che è stata caricata all'inizializzazione
        return ownershipMap.getOrDefault(username, new ArrayList<>());
    }

    /**
     * Verifica se un utente è proprietario di un determinato ristorante.
     *
     * @param username username dell'utente
     * @param ristoranteId nome del ristorante
     * @return true se l'utente possiede il ristorante, false altrimenti
     */
    public boolean isOwner(String username, String ristoranteId) {
        // Usa direttamente la mappa in memoria
        List<String> ownedRestaurants = ownershipMap.get(username);
        return ownedRestaurants != null && ownedRestaurants.contains(ristoranteId);
    }

    /**
     * Associa un ristorante a un proprietario.
     * Aggiorna la mappa in memoria e salva su file.
     *
     * @param ristoranteNome nome del ristorante da associare
     * @param username username del proprietario
     * @throws IOException se non è possibile scrivere sul file
     */
    public void associaRistoranteAProprietario(String ristoranteNome, String username) {
        try (FileWriter writer = new FileWriter(OWNERSHIP_FILE_PATH, true)) {
            writer.write(String.format("%s,%s%n", username, ristoranteNome));
            // Aggiorna la mappa in memoria dopo aver scritto
            ownershipMap.computeIfAbsent(username, _ -> new ArrayList<>()).add(ristoranteNome);
        } catch (IOException e) {
            System.err.println("Errore durante l'associazione del ristorante: " + e.getMessage());
        }
    }

    /**
     * Ricarica i dati di proprietà dal file CSV.
     * Utile per aggiornare la mappa in memoria dopo modifiche esterne.
     */
    public void refreshOwnershipData() {
        loadOwnershipData();
    }
}