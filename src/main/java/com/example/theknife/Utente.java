package com.example.theknife;

/**
 * La classe {@code Utente} rappresenta un utente del sistema TheKnife.
 * Contiene tutte le informazioni personali e di accesso necessarie per gestire le interazioni con l'applicazione.
 *
 * <p>
 * Un utente può avere diversi ruoli, ciascuno con permessi specifici:
 * <ul>
 * <li><b>cliente</b>: può visualizzare i ristoranti, lasciare recensioni, e gestire la lista dei preferiti.</li>
 * <li><b>ristoratore</b>: può aggiungere e modificare i propri ristoranti e rispondere alle recensioni ricevute.</li>
 * <li><b>ospite</b>: può solo visualizzare le informazioni pubbliche dei ristoranti senza poter interagire.</li>
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

    private String nome;
    private String cognome;
    private String username;
    private String passwordHash;
    private String dataNascita;
    private String luogoDomicilio;
    private String ruolo;

    /**
     * Costruttore completo per inizializzare un nuovo utente con tutti i parametri necessari.
     * Questo costruttore è utilizzato per la registrazione e il caricamento dei dati da database.
     *
     * @param nome Il nome dell'utente.
     * @param cognome Il cognome dell'utente.
     * @param username Lo username univoco utilizzato per l'accesso e l'identificazione.
     * @param passwordHash L'hash della password dell'utente, generato tramite algoritmo di hashing sicuro.
     * @param dataNascita La data di nascita dell'utente in formato YYYY-MM-DD. Può essere una stringa vuota se non fornita.
     * @param luogoDomicilio La città o il luogo di domicilio dell'utente.
     * @param ruolo Il ruolo assegnato all'utente, che definisce i suoi permessi all'interno del sistema (es. "cliente", "ristoratore", "ospite").
     */
    public Utente(String nome, String cognome, String username, String passwordHash,
                  String dataNascita, String luogoDomicilio, String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.passwordHash = passwordHash;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = luogoDomicilio;
        this.ruolo = ruolo;
    }

    /**
     * Costruttore vuoto, utile per la deserializzazione o l'inizializzazione di un oggetto Utente
     * prima di impostarne i valori tramite i metodi setter.
     */
    public Utente() {
    }

    // Metodi Getter

    /**
     * Restituisce il nome dell'utente.
     *
     * @return Il nome dell'utente.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce il cognome dell'utente.
     *
     * @return Il cognome dell'utente.
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * Restituisce lo username dell'utente.
     *
     * @return Lo username dell'utente.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Restituisce l'hash della password dell'utente.
     *
     * @return La stringa che rappresenta l'hash della password.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Restituisce la data di nascita dell'utente.
     *
     * @return La data di nascita in formato YYYY-MM-DD.
     */
    public String getDataNascita() {
        return dataNascita;
    }

    /**
     * Restituisce il luogo di domicilio dell'utente.
     *
     * @return Il luogo di domicilio.
     */
    public String getLuogoDomicilio() {
        return luogoDomicilio;
    }

    /**
     * Restituisce il ruolo dell'utente.
     *
     * @return La stringa che rappresenta il ruolo (es. "cliente", "ristoratore", "ospite").
     */
    public String getRuolo() {
        return ruolo;
    }

    // Metodi Setter

    /**
     * Imposta il nome dell'utente.
     *
     * @param nome Il nuovo nome.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Imposta il cognome dell'utente.
     *
     * @param cognome Il nuovo cognome.
     */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /**
     * Imposta lo username dell'utente.
     *
     * @param username Il nuovo username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Imposta l'hash della password dell'utente.
     *
     * @param passwordHash Il nuovo hash della password.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Imposta la data di nascita dell'utente.
     *
     * @param dataNascita La nuova data di nascita in formato YYYY-MM-DD.
     */
    public void setDataNascita(String dataNascita) {
        this.dataNascita = dataNascita;
    }

    /**
     * Imposta il luogo di domicilio dell'utente.
     *
     * @param luogoDomicilio Il nuovo luogo di domicilio.
     */
    public void setLuogoDomicilio(String luogoDomicilio) {
        this.luogoDomicilio = luogoDomicilio;
    }

    /**
     * Imposta il ruolo dell'utente.
     *
     * @param ruolo Il nuovo ruolo da assegnare (es. "cliente", "ristoratore", "ospite").
     */
    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    // Metodi Utility

    /**
     * Combina il nome e il cognome per restituire il nome completo dell'utente.
     *
     * @return Una stringa che contiene il nome e il cognome, separati da uno spazio.
     */
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    /**
     * Verifica se il ruolo dell'utente è "cliente".
     *
     * @return {@code true} se il ruolo dell'utente è "cliente", ignorando la distinzione tra maiuscole e minuscole; {@code false} altrimenti.
     */
    public boolean isCliente() {
        return "cliente".equalsIgnoreCase(ruolo);
    }

    /**
     * Verifica se il ruolo dell'utente è "ristoratore".
     *
     * @return {@code true} se il ruolo dell'utente è "ristoratore", ignorando la distinzione tra maiuscole e minuscole; {@code false} altrimenti.
     */
    public boolean isRistoratore() {
        return "ristoratore".equalsIgnoreCase(ruolo);
    }

    /**
     * Verifica se il ruolo dell'utente è "ospite".
     *
     * @return {@code true} se il ruolo dell'utente è "ospite", ignorando la distinzione tra maiuscole e minuscole; {@code false} altrimenti.
     */
    public boolean isOspite() {
        return "ospite".equalsIgnoreCase(ruolo);
    }

    // Override dei metodi Object

    /**
     * Restituisce una rappresentazione in formato stringa dell'oggetto {@code Utente}.
     * Questa stringa è utile per il debug e la visualizzazione delle informazioni chiave dell'utente.
     *
     * @return Una stringa formattata che include nome, cognome, username, ruolo e luogo di domicilio.
     */
    @Override
    public String toString() {
        return String.format("Utente{nome='%s', cognome='%s', username='%s', ruolo='%s', luogo='%s'}",
                nome, cognome, username, ruolo, luogoDomicilio);
    }

    /**
     * Confronta questo oggetto {@code Utente} con un altro per verificarne l'uguaglianza.
     * L'uguaglianza è basata sull'unicità dello username. Se entrambi gli oggetti hanno lo stesso username, sono considerati uguali.
     *
     * @param obj L'oggetto con cui confrontare l'istanza corrente.
     * @return {@code true} se l'oggetto specificato è un'istanza di {@code Utente} e ha lo stesso username di questo oggetto; {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Utente utente = (Utente) obj;
        return username != null && username.equals(utente.username);
    }

    /**
     * Calcola l'hash code per questo oggetto {@code Utente}.
     * L'hash code è basato unicamente sull'username, in coerenza con il metodo {@code equals}.
     *
     * @return L'hash code intero basato sullo username. Restituisce {@code 0} se lo username è null.
     */
    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}