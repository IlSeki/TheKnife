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
 *   <li>Fornire informazioni di debug per il troubleshooting</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @version 1.1
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
        SessioneUtente sessione = getIstanza();
        return sessione.isLoggato ? sessione.ruolo : null;
    }

    /**
     * Restituisce il nome completo dell'utente corrente.
     *
     * @return Il nome completo (nome + cognome), "Ospite" se ospite, stringa vuota se non loggato.
     */
    public static String getNomeCompletoUtente() {
        SessioneUtente sessione = getIstanza();
        if (sessione.isLoggato) {
            if ("ospite".equalsIgnoreCase(sessione.ruolo)) {
                return "Ospite";
            }
            if (sessione.nome != null && sessione.cognome != null) {
                return sessione.nome + " " + sessione.cognome;
            }
        }
        return "";
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
     * Utile per il troubleshooting e il monitoraggio dello stato della sessione.
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