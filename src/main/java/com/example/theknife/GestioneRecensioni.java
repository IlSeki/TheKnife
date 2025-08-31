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
 * Implementa il pattern Singleton per garantire un'unica istanza.
 */
public class GestioneRecensioni {
    private static final String CSV_FILE = "data/recensioni.csv";
    private static final String CSV_HEADER = "username,ristorante,stelle,testo,data,risposta";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static GestioneRecensioni instance;
    private final Map<String, List<Recensione>> recensioniMap = new HashMap<>();
    private final ObservableList<Recensione> allRecensioni = FXCollections.observableArrayList();

    private GestioneRecensioni() {
        caricaRecensioni();
    }

    public static GestioneRecensioni getInstance() {
        if (instance == null) {
            instance = new GestioneRecensioni();
        }
        return instance;
    }

    /**
     * Carica tutte le recensioni dal file CSV.
     */
    private void caricaRecensioni() {
        recensioniMap.clear();
        allRecensioni.clear();

        File csvFile = new File(CSV_FILE);

        if (!csvFile.exists()) {
            createReviewsFile(csvFile);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                processReviewLine(line);
            }
        } catch (IOException e) {
            System.err.println("Errore nel caricamento delle recensioni: " + e.getMessage());
        }
    }

    /**
     * Crea il file delle recensioni con header se non esiste.
     */
    private void createReviewsFile(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.append(CSV_HEADER + "\n");
        } catch (IOException e) {
            System.err.println("Errore durante la creazione del file recensioni.csv: " + e.getMessage());
        }
    }

    /**
     * Processa una singola riga del CSV delle recensioni.
     */
    private void processReviewLine(String line) {
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length >= 5) {
            try {
                String username = parts[0].trim();
                String ristoranteId = parts[1].trim();
                int stelle = Integer.parseInt(parts[2].trim());
                String testo = cleanCsvValue(parts[3]);
                String data = parts[4].trim();
                String risposta = parts.length > 5 ? cleanCsvValue(parts[5]) : "";

                Recensione recensione = new Recensione(stelle, testo, ristoranteId, username);
                recensione.setData(data);
                if (!risposta.isEmpty()) {
                    recensione.setRisposta(risposta);
                }

                recensioniMap.computeIfAbsent(ristoranteId, k -> new ArrayList<>()).add(recensione);
                allRecensioni.add(recensione);
            } catch (NumberFormatException e) {
                System.err.println("Errore nel parsing della recensione: " + e.getMessage());
            }
        }
    }

    /**
     * Pulisce un valore CSV rimuovendo virgolette.
     */
    private String cleanCsvValue(String value) {
        return value.replace("\"", "").trim();
    }

    /**
     * Salva tutte le recensioni su file CSV.
     */
    private void salvaRecensioni() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println(CSV_HEADER);

            recensioniMap.values().stream()
                    .flatMap(List::stream)
                    .forEach(r -> writer.printf("%s,%s,%d,\"%s\",%s,\"%s\"%n",
                            r.getUsername(), r.getRistoranteId(), r.getStelle(),
                            r.getTesto(), r.getData(), r.getRisposta()));

        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle recensioni: " + e.getMessage());
        }
    }

    /**
     * Aggiunge una nuova recensione.
     */
    public void aggiungiRecensione(Recensione recensione) {
        recensione.setData(LocalDateTime.now().format(DATE_FORMATTER));
        recensioniMap.computeIfAbsent(recensione.getRistoranteId(), k -> new ArrayList<>()).add(recensione);
        allRecensioni.add(recensione);
        salvaRecensioni();
    }

    /**
     * Modifica una recensione esistente.
     */
    public void modificaRecensione(String username, String ristoranteId, String nuovoTesto, int nuoveStelle) {
        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni != null) {
            recensioni.stream()
                    .filter(r -> r.getUsername().equals(username) && r.getRistoranteId().equals(ristoranteId))
                    .findFirst()
                    .ifPresent(r -> {
                        r.setTesto(nuovoTesto);
                        r.setStelle(nuoveStelle);
                        r.setData(LocalDateTime.now().format(DATE_FORMATTER));
                    });
            salvaRecensioni();
        }
    }

    /**
     * Elimina una recensione.
     */
    public void eliminaRecensione(String username, String ristoranteId) {
        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni != null) {
            recensioni.removeIf(r -> r.getUsername().equals(username) && r.getRistoranteId().equals(ristoranteId));
            allRecensioni.removeIf(r -> r.getUsername().equals(username) && r.getRistoranteId().equals(ristoranteId));
            salvaRecensioni();
        }
    }

    /**
     * Aggiunge una risposta a una recensione.
     */
    public void aggiungiRisposta(String username, String ristoranteId, String risposta) {
        String currentUser = SessioneUtente.getUsernameUtente();
        if (!GestioneRistorante.getInstance().isProprietario(currentUser, ristoranteId)) {
            throw new IllegalStateException("Non sei autorizzato a rispondere a questa recensione");
        }

        List<Recensione> recensioni = recensioniMap.get(ristoranteId);
        if (recensioni != null) {
            recensioni.stream()
                    .filter(r -> r.getUsername().equals(username) && r.getRistoranteId().equals(ristoranteId))
                    .findFirst()
                    .ifPresent(r -> r.setRisposta(risposta));
            salvaRecensioni();
        }
    }

    /**
     * Salva la risposta a una recensione.
     */
    public void salvaRispostaRecensione(Recensione recensione) {
        salvaRecensioni();
    }

    /**
     * Restituisce tutte le recensioni di un ristorante.
     */
    public List<Recensione> getRecensioniRistorante(String nomeRistorante) {
        caricaRecensioni(); // Ricarica per avere dati aggiornati
        return recensioniMap.getOrDefault(nomeRistorante, new ArrayList<>());
    }

    /**
     * Restituisce tutte le recensioni fatte da un utente.
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
     */
    public double getMediaStelleRistorante(String ristoranteId) {
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
     */
    public boolean isRecensioneOwner(String username, Recensione recensione) {
        return recensione != null && recensione.getUsername().equals(username);
    }
}