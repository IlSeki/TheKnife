package com.example.theknife;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * La classe {@code Utente} rappresenta un utente dell'applicazione "TheKnife".
 * Un utente può essere di tipo cliente o ristoratore, con funzionalità diverse a seconda del ruolo.
 * La classe gestisce le informazioni personali dell'utente, le credenziali di accesso, e le liste
 * di ristoranti preferiti (per i clienti) o gestiti (per i ristoratori).
 *
 * <p>
 * Le funzionalità principali includono:
 * <ul>
 *   <li>Gestione dei dati personali (nome, cognome, data di nascita, ecc.)</li>
 *   <li>Cifratura della password tramite algoritmo SHA-256</li>
 *   <li>Gestione dei ristoranti preferiti per gli utenti di tipo cliente</li>
 *   <li>Gestione dei ristoranti di proprietà per gli utenti di tipo ristoratore</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class Utente {

    /**
     * Enumerazione che definisce i possibili ruoli di un utente nell'applicazione.
     */
    public enum Ruolo {
        /** Utente che cerca ristoranti e può lasciare recensioni */
        CLIENTE,
        /** Utente che gestisce uno o più ristoranti */
        RISTORATORE
    }

    /**
     * Il nome dell'utente.
     */
    private String nome;

    /**
     * Il cognome dell'utente.
     */
    private String cognome;

    /**
     * Lo username univoco dell'utente.
     */
    private String username;

    /**
     * La password dell'utente, memorizzata in formato cifrato.
     */
    private String passwordCifrata;

    /**
     * La data di nascita dell'utente (campo facoltativo).
     */
    private LocalDate dataNascita;

    /**
     * Il luogo di domicilio dell'utente.
     */
    private String luogoDomicilio;

    /**
     * Il ruolo dell'utente nell'applicazione (cliente o ristoratore).
     */
    private Ruolo ruolo;

    /**
     * Lista dei ristoranti preferiti (utilizzata solo se l'utente è un cliente).
     */
    private List<Ristorante> ristorantiPreferiti;

    /**
     * Lista dei ristoranti gestiti (utilizzata solo se l'utente è un ristoratore).
     */
    private List<Ristorante> ristorantiGestiti;

    /**
     * Crea un nuovo oggetto {@code Utente} con i parametri specificati.
     *
     * @param nome il nome dell'utente.
     * @param cognome il cognome dell'utente.
     * @param username lo username univoco dell'utente.
     * @param password la password in chiaro (verrà cifrata durante la creazione dell'utente).
     * @param dataNascita la data di nascita dell'utente (può essere null).
     * @param luogoDomicilio il luogo di domicilio dell'utente.
     * @param ruolo il ruolo dell'utente (cliente o ristoratore).
     * @throws NoSuchAlgorithmException se l'algoritmo di cifratura non è disponibile.
     */
    public Utente(String nome, String cognome, String username, String password,
                  LocalDate dataNascita, String luogoDomicilio, Ruolo ruolo) throws NoSuchAlgorithmException {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.passwordCifrata = cifraPassword(password);
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
        this.ruolo = ruolo;

        // Inizializza le liste appropriate in base al ruolo
        if (ruolo == Ruolo.CLIENTE) {
            this.ristorantiPreferiti = new ArrayList<>();
            this.ristorantiGestiti = null;
        } else {
            this.ristorantiPreferiti = null;
            this.ristorantiGestiti = new ArrayList<>();
        }
    }

    /**
     * Verifica se la password fornita corrisponde a quella memorizzata.
     *
     * @param passwordInserita la password da verificare.
     * @return true se la password corrisponde, false altrimenti.
     * @throws NoSuchAlgorithmException se l'algoritmo di cifratura non è disponibile.
     */
    public boolean verificaPassword(String passwordInserita) throws NoSuchAlgorithmException {
        String passwordInseritaCifrata = cifraPassword(passwordInserita);
        return passwordInseritaCifrata.equals(this.passwordCifrata);
    }

    /**
     * Cifra una password utilizzando l'algoritmo SHA-256.
     *
     * @param password la password in chiaro da cifrare.
     * @return la password cifrata come stringa esadecimale.
     * @throws NoSuchAlgorithmException se l'algoritmo SHA-256 non è disponibile.
     */
    private String cifraPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());

        // Converte l'array di byte in una stringa esadecimale
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Aggiunge un ristorante alla lista dei preferiti dell'utente.
     * Questa operazione è valida solo per utenti di tipo cliente.
     *
     * @param ristorante il ristorante da aggiungere ai preferiti.
     * @return true se l'operazione è riuscita, false se l'utente non è un cliente.
     */
    public boolean aggiungiRistorantePreferito(Ristorante ristorante) {
        if (ruolo != Ruolo.CLIENTE) return false;

        if (!ristorantiPreferiti.contains(ristorante)) {
            ristorantiPreferiti.add(ristorante);
        }
        return true;
    }

    /**
     * Rimuove un ristorante dalla lista dei preferiti dell'utente.
     * Questa operazione è valida solo per utenti di tipo cliente.
     *
     * @param ristorante il ristorante da rimuovere dai preferiti.
     * @return true se l'operazione è riuscita, false se l'utente non è un cliente.
     */
    public boolean rimuoviRistorantePreferito(Ristorante ristorante) {
        if (ruolo != Ruolo.CLIENTE) return false;

        return ristorantiPreferiti.remove(ristorante);
    }

    /**
     * Aggiunge un ristorante alla lista dei ristoranti gestiti dall'utente.
     * Questa operazione è valida solo per utenti di tipo ristoratore.
     *
     * @param ristorante il ristorante da aggiungere alla lista dei gestiti.
     * @return true se l'operazione è riuscita, false se l'utente non è un ristoratore.
     */
    public boolean aggiungiRistoranteGestito(Ristorante ristorante) {
        if (ruolo != Ruolo.RISTORATORE) return false;

        if (!ristorantiGestiti.contains(ristorante)) {
            ristorantiGestiti.add(ristorante);
        }
        return true;
    }

    /**
     * Restituisce il nome dell'utente.
     *
     * @return il nome dell'utente.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il nome dell'utente.
     *
     * @param nome il nuovo nome dell'utente.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce il cognome dell'utente.
     *
     * @return il cognome dell'utente.
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Imposta il cognome dell'utente.
     *
     * @param cognome il nuovo cognome dell'utente.
     */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /**
     * Restituisce lo username dell'utente.
     *
     * @return lo username dell'utente.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce la data di nascita dell'utente.
     *
     * @return la data di nascita dell'utente, può essere null.
     */
    public LocalDate getDataNascita() {
        return dataNascita;
    }

    /**
     * Imposta la data di nascita dell'utente.
     *
     * @param dataNascita la nuova data di nascita dell'utente.
     */
    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    /**
     * Restituisce il luogo di domicilio dell'utente.
     *
     * @return il luogo di domicilio dell'utente.
     */
    public String getLuogoDomicilio() {
        return luogoDomicilio;
    }

    /**
     * Imposta il luogo di domicilio dell'utente.
     *
     * @param luogoDomicilio il nuovo luogo di domicilio dell'utente.
     */
    public void setLuogoDomicilio(String luogoDomicilio) {
        this.luogoDomicilio = luogoDomicilio;
    }

    /**
     * Restituisce il ruolo dell'utente.
     *
     * @return il ruolo dell'utente (cliente o ristoratore).
     */
    public Ruolo getRuolo() {
        return ruolo;
    }

    /**
     * Restituisce la lista dei ristoranti preferiti dall'utente.
     *
     * @return la lista dei ristoranti preferiti, null se l'utente è un ristoratore.
     */
    public List<Ristorante> getRistorantiPreferiti() {
        return ristorantiPreferiti;
    }

    /**
     * Restituisce la lista dei ristoranti gestiti dall'utente.
     *
     * @return la lista dei ristoranti gestiti, null se l'utente è un cliente.
     */
    public List<Ristorante> getRistorantiGestiti() {
        return ristorantiGestiti;
    }

    /**
     * Restituisce una rappresentazione testuale dell'oggetto {@code Utente}.
     *
     * @return una stringa che descrive l'utente e le sue proprietà.
     */
    @Override
    public String toString() {
        return String.format("Utente[username=%s, nome=%s, cognome=%s, ruolo=%s, luogoDomicilio=%s]",
                username, nome, cognome, ruolo, luogoDomicilio);
    }
}