package com.example.theknife;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Servizio per la gestione dei ristoranti.
 * Implementa il pattern Singleton e gestisce tutte le operazioni CRUD sui ristoranti,
 * inclusa la persistenza su file CSV.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class RistoranteService {
    private static final String CSV_FILE = "/data/michelin_my_maps.csv";
    private static RistoranteService instance;
    private final Map<String, Ristorante> ristoranti;
    private final Map<String, Set<String>> proprietariRistoranti; // username -> set nomi ristoranti
    private static final String PROPRIETARI_FILE = "src/main/resources/data/proprietari_ristoranti.csv";

    private RistoranteService() {
        ristoranti = new HashMap<>();
        proprietariRistoranti = new HashMap<>();
        caricaRistoranti();
        caricaProprietari();
    }

    public static RistoranteService getInstance() {
        if (instance == null) {
            instance = new RistoranteService();
        }
        return instance;
    }

    private void caricaRistoranti() {
        try (InputStream is = getClass().getResourceAsStream(CSV_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
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
                            values.get(0),  // nome
                            values.get(1),  // indirizzo
                            values.get(2),  // localita
                            values.get(3),  // prezzo
                            values.get(4),  // cucina
                            Double.parseDouble(values.get(5).trim()),  // longitudine
                            Double.parseDouble(values.get(6).trim()),  // latitudine
                            values.get(7),  // telefono
                            values.get(8),  // url
                            values.get(9),  // sito web
                            values.get(10), // premio
                            values.get(11), // stella verde
                            values.get(12), // servizi
                            values.get(13)  // descrizione
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

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // Se troviamo una virgoletta, cambiamo lo stato
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                // Se troviamo una virgola e non siamo tra virgolette, abbiamo finito il valore corrente
                result.add(currentValue.toString().trim().replaceAll("^\"|\"$", ""));
                currentValue.setLength(0);
            } else {
                // Altrimenti aggiungiamo il carattere al valore corrente
                currentValue.append(c);
            }
        }
        
        // Aggiungi l'ultimo valore
        result.add(currentValue.toString().trim().replaceAll("^\"|\"$", ""));
        
        return result;
    }

    private void caricaProprietari() {
        File file = new File(PROPRIETARI_FILE);
        if (!file.exists()) {
            System.err.println("File proprietari non trovato: " + PROPRIETARI_FILE);
            return;
        }

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

    /**
     * Recupera un ristorante dal suo nome usando la cache aggiornata
     */
    public Ristorante getRistorante(String nome) {
        caricaRistoranti();
        return ristoranti.get(nome);
    }

    /**
     * Recupera tutti i ristoranti (sempre aggiornati dal CSV)
     */
    public List<Ristorante> getTuttiRistoranti() {
        caricaRistoranti();
        return new ArrayList<>(ristoranti.values());
    }

    /**
     * Recupera una lista di ristoranti dai loro nomi (sempre aggiornati dal CSV)
     */
    public List<Ristorante> getRistorantiByNomi(Collection<String> nomi) {
        caricaRistoranti();
        return nomi.stream()
            .map(this::getRistorante)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Recupera i ristoranti di proprietà di un ristoratore (sempre aggiornati dal CSV)
     */
    public ObservableList<Ristorante> getRistorantiByRistoratore(String username) {
        caricaRistoranti();
        caricaProprietari();
        Set<String> ristorantiIds = proprietariRistoranti.getOrDefault(username, Collections.emptySet());
        return FXCollections.observableArrayList(
            ristorantiIds.stream()
                .map(this::getRistorante)
                .filter(Objects::nonNull)
                .toList()
        );
    }

    /**
     * Verifica se un utente è proprietario di un ristorante
     */
    public boolean isProprietario(String username, String nomeRistorante) {
        return proprietariRistoranti.containsKey(username) &&
               proprietariRistoranti.get(username).contains(nomeRistorante);
    }

    /**
     * Aggiunge un ristorante e lo associa a un proprietario
     */
    public boolean aggiungiRistorante(String username, Ristorante ristorante) {
        if (username == null || ristorante == null) return false;
        
        // Aggiunge alla mappa dei proprietari
        proprietariRistoranti.computeIfAbsent(username, _ -> new HashSet<>()).add(ristorante.getNome());
        salvaProprietari();
        
        // Aggiunge alla cache dei ristoranti
        ristoranti.put(ristorante.getNome(), ristorante);
        
        // Aggiunge al file CSV
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
        File dir = new File("src/main/resources/data");
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