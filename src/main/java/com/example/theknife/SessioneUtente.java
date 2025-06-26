package com.example.theknife;

/**
 * Classe per la gestione della sessione utente.
 * Mantiene le informazioni dell'utente correntemente loggato
 * durante l'esecuzione dell'applicazione.
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
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
     * Implementazione thread-safe del pattern Singleton.
     *
     * @return L'istanza corrente di SessioneUtente.
     */
    public static synchronized SessioneUtente getIstanza() {
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

        System.out.println("DEBUG: Sessione utente impostata - " + nome + " " + cognome + " (" + ruolo + ")");
    }

    /**
     * Verifica se un utente è attualmente autenticato.
     * Controlla sia il flag isLoggato che la presenza dello username.
     *
     * @return true se un utente è autenticato, false altrimenti.
     */
    public static boolean isUtenteLoggato() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato && sessione.username != null && !sessione.username.isEmpty();
    }

    /**
     * Restituisce il nome dell'utente corrente.
     *
     * @return Il nome dell'utente o null se nessun utente è autenticato.
     */
    public static String getNomeUtente() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.nome : null;
    }

    /**
     * Restituisce il cognome dell'utente corrente.
     *
     * @return Il cognome dell'utente o null se nessun utente è autenticato.
     */
    public static String getCognomeUtente() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.cognome : null;
    }

    /**
     * Restituisce lo username dell'utente corrente.
     *
     * @return Lo username dell'utente o null se nessun utente è autenticato.
     */
    public static String getUsernameUtente() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.username : null;
    }

    /**
     * Restituisce il ruolo dell'utente corrente.
     *
     * @return Il ruolo dell'utente o null se nessun utente è autenticato.
     */
    public static String getRuoloUtente() {
        return getIstanza().ruolo;
    }

    /**
     * Restituisce il nome completo dell'utente corrente.
     *
     * @return Il nome completo (nome + cognome), "Ospite" se ospite, stringa vuota se non loggato.
     */
    public static String getNomeCompleto() {
        SessioneUtente sessione = getIstanza();
        if (!sessione.isLoggato) return "";
        if (isOspite()) return "Ospite";
        return sessione.nome + " " + sessione.cognome;
    }

    /**
     * Alias per getNomeCompleto().
     *
     * @return Il nome completo dell'utente corrente.
     */
    public static String getNomeCompletoUtente() {
        return getNomeCompleto();
    }

    /**
     * Restituisce il ruolo dell'utente corrente.
     *
     * @return Il ruolo dell'utente o null se nessun utente è autenticato.
     */
    public static String getRuolo() {
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.ruolo : null;
    }

    /**
     * Verifica se l'utente corrente è un cliente.
     *
     * @return true se l'utente è un cliente, false altrimenti.
     */
    public static boolean isCliente() {
        return "cliente".equalsIgnoreCase(getRuoloUtente());
    }

    /**
     * Verifica se l'utente corrente è un ristoratore.
     *
     * @return true se l'utente è un ristoratore, false altrimenti.
     */
    public static boolean isRistoratore() {
        return "ristoratore".equalsIgnoreCase(getRuoloUtente());
    }

    /**
     * Verifica se l'utente corrente è un ospite (non registrato).
     *
     * @return true se l'utente è un ospite, false altrimenti.
     */
    public static boolean isOspite() {
        return "ospite".equalsIgnoreCase(getRuoloUtente());
    }

    /**
     * Termina la sessione corrente (logout).
     * Cancella tutti i dati dell'utente e segna la sessione come non attiva.
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
     * Alias per pulisciSessione() per compatibilità.
     */
    public static void eseguiLogout() {
        pulisciSessione();
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
            return "SessioneUtente{Non loggato}";
        }
    }

    /**
     * Metodo statico per ottenere la rappresentazione stringa della sessione corrente.
     *
     * @return Una stringa con le informazioni della sessione corrente.
     */
    public static String getStringaSessione() {
        return getIstanza().toString();
    }

    /**
     * Restituisce informazioni dettagliate di debug sulla sessione corrente.
     *
     * @return Stringa con informazioni complete di debug.
     */
    public static String getDebugInfo() {
        SessioneUtente sessione = getIstanza();
        return String.format("DEBUG - Sessione: loggato=%s, nome=%s, cognome=%s, username=%s, ruolo=%s, istanza=%s",
                sessione.isLoggato, sessione.nome, sessione.cognome, sessione.username, sessione.ruolo,
                sessione.hashCode());
    }

    /**
     * Restituisce informazioni di stato della sessione in formato compatto.
     *
     * @return Stringa con stato della sessione.
     */
    public static String getStatoSessione() {
        if (isUtenteLoggato()) {
            return String.format("Utente loggato: %s (%s)", getNomeCompletoUtente(), getRuoloUtente());
        } else {
            return "Nessun utente loggato";
        }
    }

    /**
     * Metodo di utilità per verificare se la sessione è valida.
     * Controlla la coerenza dei dati della sessione.
     *
     * @return true se la sessione è in uno stato valido, false altrimenti.
     */
    public static boolean isSessioneValida() {
        SessioneUtente sessione = getIstanza();
        if (!sessione.isLoggato) {
            return true; // Sessione non loggata è valida
        }

        // Se loggato, deve avere almeno username e ruolo
        return sessione.username != null && !sessione.username.isEmpty() &&
                sessione.ruolo != null && !sessione.ruolo.isEmpty();
    }
}