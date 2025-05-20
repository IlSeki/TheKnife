package com.example.theknife;

import java.time.LocalDateTime;

/**
 * La classe {@code Recensione} rappresenta una recensione inserita da un cliente per un ristorante
 * nell'applicazione "TheKnife". Una recensione è caratterizzata da un testo descrittivo, un punteggio
 * in stelle (da 1 a 5), il riferimento all'utente che l'ha scritta e al ristorante recensito,
 * la data di creazione e un'eventuale risposta del ristoratore.
 *
 * <p>
 * Le funzionalità principali includono:
 * <ul>
 *   <li>Creazione e modifica della recensione</li>
 *   <li>Risposta da parte del ristoratore</li>
 *   <li>Validazione dei dati inseriti, come il controllo del range delle stelle (1-5)</li>
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
public class Recensione {

    /**
     * L'utente che ha scritto la recensione.
     */
    private Utente autore;

    /**
     * Il ristorante oggetto della recensione.
     */
    private Ristorante ristorante;

    /**
     * Il numero di stelle assegnate (da 1 a 5).
     */
    private int stelle;

    /**
     * Il testo della recensione.
     */
    private String testo;

    /**
     * La data e ora di creazione della recensione.
     */
    private LocalDateTime dataCreazione;

    /**
     * La data e ora dell'ultima modifica della recensione.
     */
    private LocalDateTime dataModifica;

    /**
     * La risposta del ristoratore alla recensione (può essere null).
     */
    private String risposta;

    /**
     * La data e ora della risposta del ristoratore (può essere null).
     */
    private LocalDateTime dataRisposta;

    /**
     * Crea un nuovo oggetto {@code Recensione} con i parametri specificati.
     *
     * @param autore l'utente che ha scritto la recensione.
     * @param ristorante il ristorante oggetto della recensione.
     * @param stelle il numero di stelle assegnate (da 1 a 5).
     * @param testo il testo della recensione.
     * @throws IllegalArgumentException se il numero di stelle non è compreso tra 1 e 5 o se l'autore
     *         non è un cliente.
     */
    public Recensione(Utente autore, Ristorante ristorante, int stelle, String testo) {
        if (autore.getRuolo() != Utente.Ruolo.CLIENTE) {
            throw new IllegalArgumentException("Solo i clienti possono scrivere recensioni");
        }

        if (stelle < 1 || stelle > 5) {
            throw new IllegalArgumentException("Il numero di stelle deve essere compreso tra 1 e 5");
        }

        this.autore = autore;
        this.ristorante = ristorante;
        this.stelle = stelle;
        this.testo = testo;
        this.dataCreazione = LocalDateTime.now();
        this.dataModifica = this.dataCreazione;
        this.risposta = null;
        this.dataRisposta = null;
    }

    /**
     * Modifica il testo e il numero di stelle della recensione.
     *
     * @param nuovoTesto il nuovo testo della recensione.
     * @param nuoveStelle il nuovo numero di stelle (da 1 a 5).
     * @throws IllegalArgumentException se il numero di stelle non è compreso tra 1 e 5.
     */
    public void modificaRecensione(String nuovoTesto, int nuoveStelle) {
        if (nuoveStelle < 1 || nuoveStelle > 5) {
            throw new IllegalArgumentException("Il numero di stelle deve essere compreso tra 1 e 5");
        }

        this.testo = nuovoTesto;
        this.stelle = nuoveStelle;
        this.dataModifica = LocalDateTime.now();
    }

    /**
     * Aggiunge una risposta del ristoratore alla recensione.
     *
     * @param risposta il testo della risposta.
     * @throws IllegalStateException se è già presente una risposta.
     */
    public void aggiungiRisposta(String risposta) {
        if (this.risposta != null) {
            throw new IllegalStateException("È già presente una risposta a questa recensione");
        }

        this.risposta = risposta;
        this.dataRisposta = LocalDateTime.now();
    }

    /**
     * Restituisce l'utente autore della recensione.
     *
     * @return l'autore della recensione.
     */
    public Utente getAutore() {
        return autore;
    }

    /**
     * Restituisce il ristorante oggetto della recensione.
     *
     * @return il ristorante recensito.
     */
    public Ristorante getRistorante() {
        return ristorante;
    }

    /**
     * Restituisce il numero di stelle assegnate.
     *
     * @return il numero di stelle (da 1 a 5).
     */
    public int getStelle() {
        return stelle;
    }

    /**
     * Restituisce il testo della recensione.
     *
     * @return il testo della recensione.
     */
    public String getTesto() {
        return testo;
    }

    /**
     * Restituisce la data e ora di creazione della recensione.
     *
     * @return la data e ora di creazione.
     */
    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Restituisce la data e ora dell'ultima modifica della recensione.
     *
     * @return la data e ora dell'ultima modifica.
     */
    public LocalDateTime getDataModifica() {
        return dataModifica;
    }

    /**
     * Restituisce la risposta del ristoratore.
     *
     * @return la risposta del ristoratore, può essere null.
     */
    public String getRisposta() {
        return risposta;
    }

    /**
     * Restituisce la data e ora della risposta del ristoratore.
     *
     * @return la data e ora della risposta, può essere null.
     */
    public LocalDateTime getDataRisposta() {
        return dataRisposta;
    }

    /**
     * Verifica se la recensione ha ricevuto una risposta dal ristoratore.
     *
     * @return true se è presente una risposta, false altrimenti.
     */
    public boolean haRisposta() {
        return risposta != null;
    }

    /**
     * Restituisce una rappresentazione testuale dell'oggetto {@code Recensione}.
     *
     * @return una stringa che descrive la recensione e le sue proprietà.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Recensione[stelle=").append(stelle)
                .append(", autore=").append(autore.getUsername())
                .append(", ristorante=").append(ristorante.getNome())
                .append(", data=").append(dataCreazione)
                .append(", testo=").append(testo);

        if (risposta != null) {
            builder.append(", risposta=").append(risposta)
                    .append(", dataRisposta=").append(dataRisposta);
        }

        builder.append("]");
        return builder.toString();
    }
}