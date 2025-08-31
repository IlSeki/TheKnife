package com.example.theknife;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servizio per la gestione dei ristoranti.
 * Implementa il pattern Singleton e gestisce tutte le operazioni CRUD sui ristoranti,
 * inclusa la persistenza su file CSV.
 */
public class GestioneRistorante {
    private static final String CSV_FILE = "data/michelin_my_maps.csv";
    private static final String PROPRIETARI_FILE = "data/proprietari_ristoranti.csv";
    private static final String CSV_HEADER = "nome,indirizzo,localita,prezzo,cucina,longitudine,latitudine,numeroTelefono,url,sitoWeb,premio,stellaVerde,servizi,descrizione";

    private static GestioneRistorante instance;
    private final Map<String, Ristorante> ristoranti = new HashMap<>();
    private final Map<String, Set<String>> proprietariRistoranti = new HashMap<>();

    private GestioneRistorante() {}

    public static GestioneRistorante getInstance() {
        if (instance == null) {
            instance = new GestioneRistorante();
        }
        return instance;
    }

    /**
     * Inizializza i dati caricando dai file CSV.
     */
    public void initializeData() {
        caricaRistoranti();
        caricaProprietari();
    }

    /**
     * Forza il ricaricamento completo dei dati dai file.
     */
    public void forceRefresh() {
        ristoranti.clear();
        proprietariRistoranti.clear();
        initializeData();
    }

    /**
     * Carica i ristoranti dal file CSV.
     */
    public void caricaRistoranti() {
        File file = new File(CSV_FILE);

        if (!file.exists()) {
            createFileWithHeader(file, CSV_HEADER);
            return;
        }

        ristoranti.clear();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CSV_FILE), StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                processRistoranteLine(line);
            }
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dei ristoranti: " + e.getMessage());
        }
    }

    /**
     * Processa una singola riga del CSV dei ristoranti.
     */
    private void processRistoranteLine(String line) {
        List<String> values = parseCsvLine(line);
        if (values.size() >= 14) {
            try {
                Ristorante ristorante = new Ristorante(
                        values.get(0), values.get(1), values.get(2), values.get(3),
                        values.get(4), Double.parseDouble(values.get(5).trim()),
                        Double.parseDouble(values.get(6).trim()), values.get(7),
                        values.get(8), values.get(9), values.get(10), values.get(11),
                        values.get(12), values.get(13)
                );
                ristoranti.put(ristorante.getNome(), ristorante);
            } catch (NumberFormatException e) {
                System.err.println("Errore nella conversione dei dati per il ristorante " + values.get(0) + ": " + e.getMessage());
            }
        }
    }

    /**
     * Parser CSV migliorato per gestire virgole all'interno delle virgolette.
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(cleanValue(currentValue.toString()));
                currentValue.setLength(0);
            } else {
                currentValue.append(c);
            }
        }
        result.add(cleanValue(currentValue.toString()));
        return result;
    }

    /**
     * Pulisce un valore CSV rimuovendo spazi e virgolette.
     */
    private String cleanValue(String value) {
        return value.trim().replaceAll("^\"|\"$", "");
    }

    /**
     * Carica i proprietari dal file CSV.
     */
    private void caricaProprietari() {
        File file = new File(PROPRIETARI_FILE);
        if (!file.exists()) {
            System.err.println("File proprietari non trovato: " + PROPRIETARI_FILE);
            return;
        }

        proprietariRistoranti.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String ristoranteId = parts[1].trim();
                    proprietariRistoranti.computeIfAbsent(username, k -> new HashSet<>()).add(ristoranteId);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dei proprietari: " + e.getMessage());
        }
    }

    /**
     * Crea un file con header se non esiste.
     */
    private void createFileWithHeader(File file, String header) {
        try {
            file.getParentFile().mkdirs();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                writer.println(header);
            }
        } catch (IOException e) {
            System.err.println("Errore nella creazione del file: " + e.getMessage());
        }
    }

    // Metodi di accesso pubblici
    public Ristorante getRistorante(String nome) {
        return ristoranti.get(nome);
    }

    public List<Ristorante> getTuttiRistoranti() {
        return new ArrayList<>(ristoranti.values());
    }

    public List<Ristorante> getRistorantiByNomi(Collection<String> nomi) {
        return nomi.stream()
                .map(this::getRistorante)
                .filter(Objects::nonNull)
                .toList();
    }

    public ObservableList<Ristorante> getRistorantiByRistoratore(String username) {
        Set<String> ristorantiIds = proprietariRistoranti.getOrDefault(username, Collections.emptySet());
        List<Ristorante> ristoranti = ristorantiIds.stream()
                .map(this::getRistorante)
                .filter(Objects::nonNull)
                .toList();
        return FXCollections.observableArrayList(ristoranti);
    }

    public boolean isProprietario(String username, String nomeRistorante) {
        return proprietariRistoranti.containsKey(username) &&
                proprietariRistoranti.get(username).contains(nomeRistorante);
    }

    /**
     * Aggiunge un nuovo ristorante e lo associa al proprietario.
     */
    public boolean aggiungiRistorante(String username, Ristorante ristorante) {
        if (username == null || ristorante == null) return false;

        // Aggiunge alla mappa locale
        ristoranti.put(ristorante.getNome(), ristorante);
        proprietariRistoranti.computeIfAbsent(username, k -> new HashSet<>()).add(ristorante.getNome());

        // Salva su file
        boolean ristoranteSaved = appendRistoranteToFile(ristorante);
        boolean proprietarioSaved = salvaProprietari();

        return ristoranteSaved && proprietarioSaved;
    }

    /**
     * Appende un ristorante al file CSV.
     */
    private boolean appendRistoranteToFile(Ristorante ristorante) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {
            writer.printf("%s,%s,%s,%s,%s,%.6f,%.6f,%s,%s,%s,%s,%s,%s,%s%n",
                    ristorante.getNome(), ristorante.getIndirizzo(), ristorante.getLocalita(),
                    ristorante.getPrezzo(), ristorante.getCucina(), ristorante.getLongitudine(),
                    ristorante.getLatitudine(), ristorante.getNumeroTelefono(), ristorante.getUrl(),
                    ristorante.getSitoWeb(), ristorante.getPremio(), ristorante.getStellaVerde(),
                    ristorante.getServizi(), ristorante.getDescrizione()
            );
            return true;
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del ristorante: " + e.getMessage());
            return false;
        }
    }

    /**
     * Salva tutti i proprietari nel file CSV.
     */
    private boolean salvaProprietari() {
        File file = new File(PROPRIETARI_FILE);
        file.getParentFile().mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("username,ristorante");
            proprietariRistoranti.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream()
                            .map(ristorante -> entry.getKey() + "," + ristorante))
                    .forEach(writer::println);
            return true;
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dei proprietari: " + e.getMessage());
            return false;
        }
    }
}