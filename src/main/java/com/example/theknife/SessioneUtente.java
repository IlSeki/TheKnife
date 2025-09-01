package com.example.theknife;

/**
 * La classe {@code SessioneUtente} gestisce lo stato dell'utente attualmente loggato
 * all'interno dell'applicazione.
 * <p>
 * Implementa il pattern **Singleton** per garantire che esista una e una sola istanza
 * di questa classe in ogni momento, fornendo un punto di accesso globale alle
 * informazioni dell'utente corrente. Mantiene i dati essenziali come nome, cognome,
 * username e ruolo, e offre metodi per verificare lo stato di login e il ruolo
 * dell'utente.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class SessioneUtente {

    /**
     * L'unica istanza della classe SessioneUtente (Singleton).
     */
    private static SessioneUtente istanza;

    /**
     * Il nome dell'utente corrente.
     */
    private String nome;
    /**
     * Il cognome dell'utente corrente.
     */
    private String cognome;
    /**
     * Il domicilio dell'utente corrente.
     */
    private String domicilio;
    /**
     * Lo username dell'utente corrente.
     */
    private String username;
    /**
     * Il ruolo dell'utente corrente (es. "cliente", "ristoratore", "ospite").
     */
    private String ruolo;
    /**
     * Flag che indica se un utente è attualmente loggato.
     */
    private boolean isLoggato;

    /**
     * Costruttore privato per prevenire l'istanziazione diretta e
     * per implementare il pattern Singleton.
     */
    private SessioneUtente() {
        this.isLoggato = false;
    }

    /**
     * Restituisce l'istanza singleton di {@code SessioneUtente}.
     * <p>
     * Questo metodo garantisce che venga creata una sola istanza della classe,
     * rendendola accessibile in modo thread-safe da qualsiasi punto dell'applicazione.
     * </p>
     *
     * @return L'istanza unica di {@code SessioneUtente}.
     */
    public static synchronized SessioneUtente getIstanza() {
        if (istanza == null) {
            istanza = new SessioneUtente();
        }
        return istanza;
    }

    /**
     * Imposta i dati dell'utente che ha appena effettuato il login e
     * attiva la sessione.
     *
     * @param nome     Il nome dell'utente.
     * @param cognome  Il cognome dell'utente.
     * @param username Lo username univoco dell'utente.
     * @param ruolo    Il ruolo dell'utente ("cliente", "ristoratore", "ospite").
     */
    public static void impostaUtenteCorrente(String nome, String cognome, String username, String ruolo, String domicilio) {
        SessioneUtente sessione = getIstanza();
        sessione.nome = nome;
        sessione.cognome = cognome;
        sessione.username = username;
        sessione.ruolo = ruolo;
        sessione.domicilio = domicilio;
        sessione.isLoggato = true;

        System.out.println("DEBUG: Sessione utente impostata - " + nome + " " + cognome + " (" + ruolo + ") " + domicilio);
    }

    /**
     * Verifica se un utente è attualmente autenticato e la sessione è attiva.
     *
     * @return {@code true} se un utente è autenticato, {@code false} altrimenti.
     */
    public static boolean isUtenteLoggato() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato && sessione.username != null && !sessione.username.isEmpty();
    }

    /**
     * Restituisce lo username dell'utente loggato.
     *
     * @return Lo username dell'utente corrente, oppure {@code null} se nessun utente è autenticato.
     */
    public static String getUsernameUtente() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.username : null;
    }

    /**
     * Restituisce il ruolo dell'utente loggato.
     *
     * @return Il ruolo dell'utente corrente, oppure {@code null} se nessun utente è autenticato.
     */
    public static String getRuoloUtente() {
        return getIstanza().ruolo;
    }

    /**
     * Restituisce il nome completo dell'utente corrente.
     *
     * @return Il nome completo (nome + cognome), la stringa "Ospite" se il ruolo è ospite,
     * oppure una stringa vuota se nessun utente è loggato.
     */
    public static String getNomeCompleto() {
        SessioneUtente sessione = getIstanza();
        if (!sessione.isLoggato) return "";
        if (isOspite()) return "Ospite";
        return sessione.nome + " " + sessione.cognome;
    }

    /**
     * Restituisce il ruolo dell'utente corrente.
     *
     * @return Il ruolo dell'utente, oppure {@code null} se nessun utente è autenticato.
     */
    public static String getRuolo() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.ruolo : null;
    }

    /**
     * Restituisce il domicilio dell'utente corrente.
     *
     * @return Il domicilio dell'utente.
     */
    public static String getDomicilio() {
        SessioneUtente sessione = getIstanza();
        return sessione.domicilio;  // Correzione: uso sessione.domicilio invece di this.domicilio
    }
    /**
     * Verifica se l'utente corrente ha il ruolo di "cliente".
     *
     * @return {@code true} se l'utente è un cliente (confronto insensibile alle maiuscole/minuscole), {@code false} altrimenti.
     */
    public static boolean isCliente() {
        return "cliente".equalsIgnoreCase(getRuoloUtente());
    }

    /**
     * Verifica se l'utente corrente ha il ruolo di "ristoratore".
     *
     * @return {@code true} se l'utente è un ristoratore (confronto insensibile alle maiuscole/minuscole), {@code false} altrimenti.
     */
    public static boolean isRistoratore() {
        return "ristoratore".equalsIgnoreCase(getRuoloUtente());
    }

    /**
     * Verifica se l'utente corrente ha il ruolo di "ospite".
     *
     * @return {@code true} se l'utente è un ospite (confronto insensibile alle maiuscole/minuscole), {@code false} altrimenti.
     */
    public static boolean isOspite() {
        return "ospite".equalsIgnoreCase(getRuoloUtente());
    }

    /**
     * Termina la sessione corrente, resettando tutti i dati dell'utente.
     * Questo metodo riporta la sessione a uno stato non autenticato.
     */
    public static void pulisciSessione() {
        SessioneUtente sessione = getIstanza();
        sessione.nome = null;
        sessione.cognome = null;
        sessione.username = null;
        sessione.ruolo = null;
        sessione.isLoggato = false;

        System.out.println("DEBUG: Sessione utente pulita");
    }

    /**
     * Alias per {@link #pulisciSessione()}. Fornisce una semantica più chiara per l'operazione di logout.
     */
    public static void eseguiLogout() {
        pulisciSessione();
    }

    /**
     * Restituisce una rappresentazione in formato stringa della sessione utente corrente.
     *
     * @return Una stringa che descrive lo stato della sessione, inclusi i dati dell'utente se loggato,
     * altrimenti indica che non c'è una sessione attiva.
     */
    @Override
    public String toString() {
        if (isLoggato) {
            return String.format("SessioneUtente{nome='%s', cognome='%s', username='%s', ruolo='%s'}",
                    nome, cognome, username, ruolo);
        } else {
            return "SessioneUtente{Non loggato}";
        }
    }

    /**
     * Metodo statico per ottenere la rappresentazione stringa della sessione corrente.
     *
     * @return Una stringa con le informazioni della sessione corrente, richiamando il metodo {@link #toString()}.
     */
    public static String getStringaSessione() {
        return getIstanza().toString();
    }
}