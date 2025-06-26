package com.example.theknife;

import javafx.beans.property.*;

/**
 * Rappresenta una recensione per un ristorante.
 * Ogni recensione ha un numero di stelle (da 1 a 5), un testo,
 * l'ID del ristorante recensito e l'username dell'utente che ha fatto la recensione.
 *
 * @author Samuele Secchi
 * @version 1.0
 */
public class Recensione {
    private final IntegerProperty stelle;
    private final StringProperty testo;
    private final StringProperty ristoranteId;
    private final StringProperty username;
    private final StringProperty data;
    private final StringProperty risposta;

    /**
     * Costruttore per una nuova recensione
     * 
     * @param stelle numero di stelle (1-5)
     * @param testo testo della recensione
     * @param ristoranteId identificativo del ristorante
     * @param username username dell'utente che ha fatto la recensione
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

    // Property getters
    public IntegerProperty stelleProperty() { return stelle; }
    public StringProperty testoProperty() { return testo; }
    public StringProperty ristoranteProperty() { return ristoranteId; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty dataProperty() { return data; }
    public StringProperty rispostaProperty() { return risposta; }

    // Value getters
    public int getStelle() { return stelle.get(); }
    public String getTesto() { return testo.get(); }
    public String getRistoranteId() { return ristoranteId.get(); }
    public String getUsername() { return username.get(); }
    public String getData() { return data.get(); }
    public String getRisposta() { return risposta.get(); }

    // Value setters
    public void setStelle(int value) { stelle.set(value); }
    public void setTesto(String value) { testo.set(value); }
    public void setData(String value) { data.set(value); }
    public void setRisposta(String value) { risposta.set(value); }

    @Override
    public String toString() {
        return String.format("Recensione{stelle=%d, testo='%s', ristorante='%s', utente='%s', data='%s'}",
                getStelle(), getTesto(), getRistoranteId(), getUsername(), getData());
    }
}