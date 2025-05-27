package com.example.theknife;

/**
 * La classe {@code Utente} rappresenta un utente del sistema TheKnife.
 * Contiene tutte le informazioni personali e di accesso dell'utente.
 *
 * <p>
 * Un utente può avere diversi ruoli:
 * <ul>
 *   <li><b>cliente</b>: può visualizzare ristoranti, lasciare recensioni e gestire preferiti</li>
 *   <li><b>ristoratore</b>: può gestire i propri ristoranti e rispondere alle recensioni</li>
 *   <li><b>ospite</b>: può solo visualizzare informazioni pubbliche</li>
 * </ul>
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @version 1.0
 * @since 2025-05-27
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
     * Costruttore per creare un nuovo utente con tutti i parametri.
     *
     * @param nome Il nome dell'utente
     * @param cognome Il cognome dell'utente
     * @param username Lo username univoco per l'accesso
     * @param passwordHash La password cifrata con hash SHA-256
     * @param dataNascita La data di nascita in formato YYYY-MM-DD (può essere null)
     * @param luogoDomicilio Il luogo di domicilio dell'utente
     * @param ruolo Il ruolo dell'utente ("cliente", "ristoratore", "ospite")
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
     * Costruttore vuoto per inizializzazione senza parametri.
     */
    public Utente() {
    }

    // Getter methods

    /**
     * @return Il nome dell'utente
     */
    public String getNome() {
        return nome;
    }

    /**
     * @return Il cognome dell'utente
     */
    public String getCognome() {
        return cognome;
    }

    /**
     * @return Lo username dell'utente
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return L'hash della password dell'utente
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @return La data di nascita dell'utente
     */
    public String getDataNascita() {
        return dataNascita;
    }

    /**
     * @return Il luogo di domicilio dell'utente
     */
    public String getLuogoDomicilio() {
        return luogoDomicilio;
    }

    /**
     * @return Il ruolo dell'utente
     */
    public String getRuolo() {
        return ruolo;
    }

    // Setter methods

    /**
     * Imposta il nome dell'utente.
     * @param nome Il nuovo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Imposta il cognome dell'utente.
     * @param cognome Il nuovo cognome
     */
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    /**
     * Imposta lo username dell'utente.
     * @param username Il nuovo username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Imposta l'hash della password dell'utente.
     * @param passwordHash Il nuovo hash della password
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Imposta la data di nascita dell'utente.
     * @param dataNascita La nuova data di nascita
     */
    public void setDataNascita(String dataNascita) {
        this.dataNascita = dataNascita;
    }

    /**
     * Imposta il luogo di domicilio dell'utente.
     * @param luogoDomicilio Il nuovo luogo di domicilio
     */
    public void setLuogoDomicilio(String luogoDomicilio) {
        this.luogoDomicilio = luogoDomicilio;
    }

    /**
     * Imposta il ruolo dell'utente.
     * @param ruolo Il nuovo ruolo ("cliente", "ristoratore", "ospite")
     */
    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    // Metodi utility

    /**
     * Restituisce il nome completo dell'utente.
     * @return Il nome completo (nome + cognome)
     */
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    /**
     * Verifica se l'utente è un cliente.
     * @return true se il ruolo è "cliente"
     */
    public boolean isCliente() {
        return "cliente".equalsIgnoreCase(ruolo);
    }

    /**
     * Verifica se l'utente è un ristoratore.
     * @return true se il ruolo è "ristoratore"
     */
    public boolean isRistoratore() {
        return "ristoratore".equalsIgnoreCase(ruolo);
    }

    /**
     * Verifica se l'utente è un ospite.
     * @return true se il ruolo è "ospite"
     */
    public boolean isOspite() {
        return "ospite".equalsIgnoreCase(ruolo);
    }

    /**
     * Verifica se la password fornita corrisponde a quella dell'utente.
     * @param passwordHashDaVerificare L'hash della password da verificare
     * @return true se le password corrispondono
     */
    public boolean verificaPassword(String passwordHashDaVerificare) {
        return this.passwordHash != null && this.passwordHash.equals(passwordHashDaVerificare);
    }

    @Override
    public String toString() {
        return String.format("Utente{nome='%s', cognome='%s', username='%s', ruolo='%s', luogo='%s'}",
                nome, cognome, username, ruolo, luogoDomicilio);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Utente utente = (Utente) obj;
        return username != null && username.equals(utente.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}