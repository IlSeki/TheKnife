package com.example.theknife;

/**
 * La classe {@code SessioneUtente} gestisce i dati della sessione utente corrente.
 * Utilizza il pattern Singleton per mantenere le informazioni dell'utente
 * autenticato durante tutta la sessione dell'applicazione.
 *
 * <p>
 * Questa classe permette di:
 * <ul>
 *   <li>Memorizzare i dati dell'utente dopo l'autenticazione</li>
 *   <li>Verificare se un utente è autenticato</li>
 *   <li>Ottenere informazioni sull'utente corrente</li>
 *   <li>Terminare la sessione (logout)</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @version 1.0
 * @since 2025-05-27
 */
public class SessioneUtente {

    private static SessioneUtente istanza;

    private String nome;
    private String cognome;
    private String username;
    private String ruolo;
    private boolean isLoggato;

    /**
     * Costruttore privato per implementare il pattern Singleton.
     */
    private SessioneUtente() {
        this.isLoggato = false;
    }

    /**
     * Restituisce l'istanza singleton di SessioneUtente.
     *
     * @return L'istanza corrente di SessioneUtente.
     */
    public static SessioneUtente getIstanza() {
        if (istanza == null) {
            istanza = new SessioneUtente();
        }
        return istanza;
    }

    /**
     * Imposta i dati dell'utente corrente e segna la sessione come attiva.
     *
     * @param nome Il nome dell'utente.
     * @param cognome Il cognome dell'utente.
     * @param username Lo username dell'utente.
     * @param ruolo Il ruolo dell'utente ("cliente", "ristoratore", "ospite").
     */
    public static void impostaUtenteCorrente(String nome, String cognome, String username, String ruolo) {
        SessioneUtente sessione = getIstanza();
        sessione.nome = nome;
        sessione.cognome = cognome;
        sessione.username = username;
        sessione.ruolo = ruolo;
        sessione.isLoggato = true;
    }

    /**
     * Verifica se un utente è attualmente autenticato.
     *
     * @return true se un utente è autenticato, false altrimenti.
     */
    public static boolean isUtenteLoggato() {
        return getIstanza().isLoggato;
    }

    /**
     * Restituisce il nome dell'utente corrente.
     *
     * @return Il nome dell'utente o null se nessun utente è autenticato.
     */
    public static String getNomeUtenteCorrente() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.nome : null;
    }

    /**
     * Restituisce il cognome dell'utente corrente.
     *
     * @return Il cognome dell'utente o null se nessun utente è autenticato.
     */
    public static String getCognomeUtenteCorrente() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.cognome : null;
    }

    /**
     * Restituisce lo username dell'utente corrente.
     *
     * @return Lo username dell'utente o null se nessun utente è autenticato.
     */
    public static String getUsernameCorrente() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.username : null;
    }

    /**
     * Restituisce il ruolo dell'utente corrente.
     *
     * @return Il ruolo dell'utente o null se nessun utente è autenticato.
     */
    public static String getRuoloUtenteCorrente() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.ruolo : null;
    }

    /**
     * Restituisce il nome completo dell'utente corrente.
     *
     * @return Il nome completo (nome + cognome) o "Ospite" se non autenticato.
     */
    public static String getNomeCompletoUtenteCorrente() {
        SessioneUtente sessione = getIstanza();
        if (sessione.isLoggato) {
            if ("ospite".equals(sessione.ruolo)) {
                return "Ospite";
            }
            return sessione.nome + " " + sessione.cognome;
        }
        return "Ospite";
    }

    /**
     * Verifica se l'utente corrente è un cliente.
     *
     * @return true se l'utente è un cliente, false altrimenti.
     */
    public static boolean isCliente() {
        return "cliente".equalsIgnoreCase(getRuoloUtenteCorrente());
    }

    /**
     * Verifica se l'utente corrente è un ristoratore.
     *
     * @return true se l'utente è un ristoratore, false altrimenti.
     */
    public static boolean isRistoratore() {
        return "ristoratore".equalsIgnoreCase(getRuoloUtenteCorrente());
    }

    /**
     * Verifica se l'utente corrente è un ospite (non registrato).
     *
     * @return true se l'utente è un ospite, false altrimenti.
     */
    public static boolean isOspite() {
        return "ospite".equalsIgnoreCase(getRuoloUtenteCorrente());
    }

    /**
     * Termina la sessione corrente (logout).
     * Cancella tutti i dati dell'utente e segna la sessione come non attiva.
     */
    public static void eseguiLogout() {
        SessioneUtente sessione = getIstanza();
        sessione.nome = null;
        sessione.cognome = null;
        sessione.username = null;
        sessione.ruolo = null;
        sessione.isLoggato = false;
    }

    /**
     * Restituisce una rappresentazione stringa della sessione utente corrente.
     *
     * @return Una stringa con le informazioni della sessione.
     */
    @Override
    public String toString() {
        if (isLoggato) {
            return String.format("SessioneUtente{nome='%s', cognome='%s', username='%s', ruolo='%s'}",
                    nome, cognome, username, ruolo);
        } else {
            return "SessioneUtente{non loggato}";
        }
    }
}