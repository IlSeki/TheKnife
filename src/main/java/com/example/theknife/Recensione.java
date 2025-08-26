package com.example.theknife;

import javafx.beans.property.*;

/**
 * Rappresenta una recensione per un ristorante.
 * <p>
 * Ogni recensione contiene:
 * <ul>
 *     <li>Numero di stelle (1-5)</li>
 *     <li>Testo della recensione</li>
 *     <li>ID del ristorante recensito</li>
 *     <li>Username dell'utente che ha scritto la recensione</li>
 *     <li>Data di creazione della recensione</li>
 *     <li>Risposta del ristorante (opzionale)</li>
 * </ul>
 * </p>
 *
 * Le proprietà utilizzano le classi di binding di JavaFX per supportare
 * l'aggiornamento dinamico dell'interfaccia utente.
 *
 * @author Samuele
 * @version 1.0
 * @since 2025-05-20
 */
public class Recensione {
    private final IntegerProperty stelle;
    private final StringProperty testo;
    private final StringProperty ristoranteId;
    private final StringProperty username;
    private final StringProperty data;
    private final StringProperty risposta;

    /**
     * Costruttore per creare una nuova recensione.
     *
     * @param stelle numero di stelle della recensione (da 1 a 5)
     * @param testo testo della recensione
     * @param ristoranteId identificativo del ristorante recensito
     * @param username username dell'utente che scrive la recensione
     */
    public Recensione(int stelle, String testo, String ristoranteId, String username) {
        this.stelle = new SimpleIntegerProperty(stelle);
        this.testo = new SimpleStringProperty(testo);
        this.ristoranteId = new SimpleStringProperty(ristoranteId);
        this.username = new SimpleStringProperty(username);
        this.data = new SimpleStringProperty(java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        this.risposta = new SimpleStringProperty("");
    }

    // ------------------ Property Getters ------------------

    /** Restituisce la property delle stelle */
    public IntegerProperty stelleProperty() { return stelle; }

    /** Restituisce la property del testo della recensione */
    public StringProperty testoProperty() { return testo; }

    /** Restituisce la property dell'ID del ristorante */
    public StringProperty ristoranteProperty() { return ristoranteId; }

    /** Restituisce la property dell'username dell'utente */
    public StringProperty usernameProperty() { return username; }

    /** Restituisce la property della data della recensione */
    public StringProperty dataProperty() { return data; }

    /** Restituisce la property della risposta del ristorante */
    public StringProperty rispostaProperty() { return risposta; }

    // ------------------ Value Getters ------------------

    /** Restituisce il numero di stelle della recensione */
    public int getStelle() { return stelle.get(); }

    /** Restituisce il testo della recensione */
    public String getTesto() { return testo.get(); }

    /** Restituisce l'ID del ristorante recensito */
    public String getRistoranteId() { return ristoranteId.get(); }

    /** Restituisce l'username dell'utente che ha scritto la recensione */
    public String getUsername() { return username.get(); }

    /** Restituisce la data di creazione della recensione */
    public String getData() { return data.get(); }

    /** Restituisce la risposta del ristorante */
    public String getRisposta() { return risposta.get(); }

    // ------------------ Value Setters ------------------

    /** Imposta il numero di stelle della recensione */
    public void setStelle(int value) { stelle.set(value); }

    /** Imposta il testo della recensione */
    public void setTesto(String value) { testo.set(value); }

    /** Imposta la data della recensione */
    public void setData(String value) { data.set(value); }

    /** Imposta la risposta del ristorante */
    public void setRisposta(String value) { risposta.set(value); }

    /**
     * Rappresentazione testuale della recensione.
     *
     * @return stringa contenente stelle, testo, ristorante, utente e data
     */
    @Override
    public String toString() {
        return String.format("Recensione{stelle=%d, testo='%s', ristorante='%s', utente='%s', data='%s'}",
                getStelle(), getTesto(), getRistoranteId(), getUsername(), getData());
    }
}