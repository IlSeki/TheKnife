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
 * Gestisce operazioni quali:
 * <ul>
 *     <li>Caricamento dei ristoranti dal CSV</li>
 *     <li>Gestione dei proprietari dei ristoranti</li>
 *     <li>Recupero, aggiunta e verifica di ristoranti</li>
 * </ul>
 *
 * @author Samuele Secchi, 761031
 * @author Flavio Marin, 759910
 * @author Matilde Lecchi, 759875
 * @author Davide Caccia, 760742
 * @version 1.0
 * @since 2025-05-20
 */
public class GestioneRistorante {
    private static final String CSV_FILE = "/data/michelin_my_maps.csv";
    private static GestioneRistorante instance;
    private final Map<String, Ristorante> ristoranti;
    private final Map<String, Set<String>> proprietariRistoranti; // username -> set nomi ristoranti
    private static final String PROPRIETARI_FILE = "src/main/resources/data/proprietari_ristoranti.csv";

    /**
     * Costruttore privato della classe {@code GestioneRistorante}.
     * <p>
     * Inizializza le mappe dei ristoranti e dei proprietari
     * e carica i dati dai file CSV.
     * </p>
     * <p>Pattern utilizzato: <b>Singleton</b></p>
     */
    private GestioneRistorante() {
        ristoranti = new HashMap<>();
        proprietariRistoranti = new HashMap<>();
        caricaRistoranti();
        caricaProprietari();
    }
    /**
     * Restituisce l'istanza unica (singleton) del servizio.
     * Se non esiste ancora, viene creata all’occorrenza.
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
     * Carica i ristoranti dal file CSV e li memorizza nella mappa {@code ristoranti}.
     * <p>
     * Ogni riga del CSV corrisponde a un ristorante, con i vari campi separati da virgole.
     * Le eventuali eccezioni di conversione dei numeri vengono gestite e loggate a console.
     * </p>
     */
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

    /**
     * Effettua il parsing di una singola riga CSV in una lista di valori.
     * <p>
     * Gestisce correttamente i campi racchiusi tra virgolette e contenenti virgole.
     * </p>
     *
     * @param line riga CSV da elaborare
     * @return lista di valori parsati e ripuliti
     */
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
    /**
     * Carica l’associazione tra ristoranti e i rispettivi proprietari
     * leggendo i dati dal file {@code PROPRIETARI_FILE}.
     * Se il file non esiste, viene mostrato un messaggio di errore su console.
     */
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
     * Recupera un ristorante dal suo nome usando la cache aggiornata.
     *
     * @param nome nome del ristorante
     * @return l'oggetto {@code Ristorante} corrispondente o {@code null} se non trovato
     */
    public Ristorante getRistorante(String nome) {
        caricaRistoranti();
        return ristoranti.get(nome);
    }

    /**
     * Recupera tutti i ristoranti presenti nel CSV.
     *
     * @return lista aggiornata di tutti i ristoranti
     */
    public List<Ristorante> getTuttiRistoranti() {
        caricaRistoranti();
        return new ArrayList<>(ristoranti.values());
    }

    /**
     * Recupera una lista di ristoranti dati i loro nomi.
     *
     * @param nomi collezione dei nomi dei ristoranti
     * @return lista di ristoranti corrispondenti ai nomi forniti
     */
    public List<Ristorante> getRistorantiByNomi(Collection<String> nomi) {
        caricaRistoranti();
        return nomi.stream()
            .map(this::getRistorante)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Recupera i ristoranti di proprietà di un ristoratore.
     *
     * @param username username del ristoratore
     * @return lista osservabile dei ristoranti del ristoratore
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
     * Verifica se un utente è proprietario di un ristorante.
     *
     * @param username username dell’utente
     * @param nomeRistorante nome del ristorante
     * @return true se l’utente possiede il ristorante, false altrimenti
     */
    public boolean isProprietario(String username, String nomeRistorante) {
        return proprietariRistoranti.containsKey(username) &&
               proprietariRistoranti.get(username).contains(nomeRistorante);
    }

    /**
     * Aggiunge un nuovo ristorante e lo associa a un proprietario.
     *
     * @param username username del proprietario
     * @param ristorante oggetto {@code Ristorante} da aggiungere
     * @return true se l’operazione ha avuto successo, false altrimenti
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

    /**
     * Salva le associazioni tra utenti e ristoranti di cui sono proprietari su file CSV.
     * <p>
     * Se la directory non esiste, viene creata.
     * In caso di errore di scrittura, l’eccezione viene loggata su console.
     * </p>
     */
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