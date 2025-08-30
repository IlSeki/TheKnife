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
    private static GestioneRistorante instance;
    private final Map<String, Ristorante> ristoranti;
    private final Map<String, Set<String>> proprietariRistoranti; // username -> set nomi ristoranti
    private static boolean isInitialized = false;

    /**
     * Costruttore privato. Inizializza le mappe ma non carica i dati.
     */
    private GestioneRistorante() {
        ristoranti = new HashMap<>();
        proprietariRistoranti = new HashMap<>();
    }

    /**
     * Restituisce l'istanza unica (singleton).
     *
     * @return istanza di {@code GestioneRistorante}
     */
    public static GestioneRistorante getInstance() {
        if (instance == null) {
            instance = new GestioneRistorante();
        }
        return instance;
    }

    /**
     * Metodo pubblico per inizializzare il caricamento dei dati.
     */
    public void initializeData() {
        if (!isInitialized) {
            caricaRistoranti();
            caricaProprietari();
            isInitialized = true;
        }
    }

    /**
     * Carica i ristoranti dal file CSV esterno.
     */
    private void caricaRistoranti() {
        File file = new File(CSV_FILE);
        // Controlla se il file esiste e lo crea con l'header
        if (!file.exists()) {
            System.err.println("File " + CSV_FILE + " non trovato. Creazione di un nuovo file.");
            try {
                file.getParentFile().mkdirs();
                try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                    writer.println("nome,indirizzo,localita,prezzo,cucina,longitudine,latitudine,numeroTelefono,url,sitoWeb,premio,stellaVerde,servizi,descrizione");
                }
            } catch (IOException e) {
                System.err.println("Errore nella creazione del file: " + e.getMessage());
                return;
            }
        }
        ristoranti.clear();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CSV_FILE), StandardCharsets.UTF_8)) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
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
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dei ristoranti: " + e.getMessage());
        }
    }

    /**
     * Effettua il parsing di una singola riga CSV.
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString().trim().replaceAll("^\"|\"$", ""));
                currentValue.setLength(0);
            } else {
                currentValue.append(c);
            }
        }
        result.add(currentValue.toString().trim().replaceAll("^\"|\"$", ""));
        return result;
    }

    /**
     * Carica l'associazione tra ristoranti e i rispettivi proprietari dal file esterno.
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

            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String ristoranteId = parts[1].trim();
                    proprietariRistoranti.computeIfAbsent(username, _ -> new HashSet<>()).add(ristoranteId);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel caricamento dei proprietari: " + e.getMessage());
        }
    }

    // Qui sotto i metodi di utilizzo della classe, modificati per rimuovere le chiamate ridondanti a caricaRistoranti() e caricaProprietari().

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
        return FXCollections.observableArrayList(
                ristorantiIds.stream()
                        .map(this::getRistorante)
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    public boolean isProprietario(String username, String nomeRistorante) {
        return proprietariRistoranti.containsKey(username) &&
                proprietariRistoranti.get(username).contains(nomeRistorante);
    }

    public boolean aggiungiRistorante(String username, Ristorante ristorante) {
        if (username == null || ristorante == null) return false;

        proprietariRistoranti.computeIfAbsent(username, _ -> new HashSet<>()).add(ristorante.getNome());
        salvaProprietari();

        ristoranti.put(ristorante.getNome(), ristorante);

        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {
            writer.println(String.format("%s,%s,%s,%s,%s,%.6f,%.6f,%s,%s,%s,%s,%s,%s,%s",
                    ristorante.getNome(),
                    ristorante.getIndirizzo(),
                    ristorante.getLocalita(),
                    ristorante.getPrezzo(),
                    ristorante.getCucina(),
                    ristorante.getLongitudine(),
                    ristorante.getLatitudine(),
                    ristorante.getNumeroTelefono(),
                    ristorante.getUrl(),
                    ristorante.getSitoWeb(),
                    ristorante.getPremio(),
                    ristorante.getStellaVerde(),
                    ristorante.getServizi(),
                    ristorante.getDescrizione()
            ));
            return true;
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del ristorante: " + e.getMessage());
            return false;
        }
    }

    private void salvaProprietari() {
        File dir = new File("data");
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("Impossibile creare la directory: " + dir.getAbsolutePath());
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(PROPRIETARI_FILE))) {
            writer.println("username,ristorante");
            for (Map.Entry<String, Set<String>> entry : proprietariRistoranti.entrySet()) {
                String username = entry.getKey();
                for (String ristoranteId : entry.getValue()) {
                    writer.println(username + "," + ristoranteId);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio dei proprietari: " + e.getMessage());
        }
    }
}