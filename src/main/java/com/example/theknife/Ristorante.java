package com.example.theknife;

/**
 * La classe {@code Ristorante} rappresenta un'entità che contiene tutte le informazioni relative
 * ad un ristorante, quali il nome, l'indirizzo, la località, il prezzo, la tipologia di cucina, le coordinate
 * geografiche e altre informazioni utili. <br>
 * Viene utilizzata per mappare i dati caricati da un file CSV e per popolare la {@code TableView} nell'interfaccia
 * grafica dell'applicazione "TheKnife".
 *
 * <p>
 * La classe fornisce un costruttore per inizializzare tutti gli attributi e una serie di metodi getter per
 * accedere ai dati. Inoltre, il metodo {@link #toString()} restituisce una rappresentazione testuale dell'oggetto,
 * utile per scopi di debug e logging.
 * </p>
 *
 * @author Samuele Secchi, 761031, Sede CO
 * @author Flavio Marin, 759910, Sede CO
 * @author Matilde Lecchi, 759875, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @author Davide Caccia, 760742, Sede CO
 * @version 1.0
 * @since 2025-05-20
 */
public class Ristorante {

    /**
     * Il nome del ristorante.
     */
    private String nome;

    /**
     * L'indirizzo del ristorante.
     */
    private String indirizzo;

    /**
     * La località in cui si trova il ristorante.
     */
    private String localita;

    /**
     * Il prezzo medio espresso come stringa.
     */
    private String prezzo;

    /**
     * Il tipo di cucina offerto dal ristorante.
     */
    private String cucina;

    /**
     * La longitudine della posizione del ristorante.
     */
    private double longitudine;

    /**
     * La latitudine della posizione del ristorante.
     */
    private double latitudine;

    /**
     * Il numero di telefono del ristorante.
     */
    private String numeroTelefono;

    /**
     * L'URL associato al ristorante.
     */
    private String url;

    /**
     * Il sito web del ristorante.
     */
    private String sitoWeb;

    /**
     * Il premio assegnato al ristorante.
     */
    private String premio;

    /**
     * Il riconoscimento "stella verde" del ristorante.
     */
    private String stellaVerde;

    /**
     * I servizi offerti dal ristorante.
     */
    private String servizi;

    /**
     * Una descrizione del ristorante.
     */
    private String descrizione;

    /**
     * Crea un nuovo oggetto {@code Ristorante} con i dettagli specificati.
     *
     * @param nome           il nome del ristorante.
     * @param indirizzo      l'indirizzo del ristorante.
     * @param localita       la località in cui si trova il ristorante.
     * @param prezzo         il prezzo medio espresso come stringa.
     * @param cucina         il tipo di cucina offerto.
     * @param longitudine    la longitudine della posizione del ristorante.
     * @param latitudine     la latitudine della posizione del ristorante.
     * @param numeroTelefono il numero di telefono del ristorante.
     * @param url            l'URL associato al ristorante.
     * @param sitoWeb        il sito web del ristorante.
     * @param premio         il premio inglobato nel riconoscimento.
     * @param stellaVerde    il riconoscimento "stella verde" assegnato.
     * @param servizi        i servizi offerti dal ristorante.
     * @param descrizione    una descrizione del ristorante.
     */
    public Ristorante(String nome, String indirizzo, String localita, String prezzo, String cucina,
                      double longitudine, double latitudine, String numeroTelefono, String url,
                      String sitoWeb, String premio, String stellaVerde, String servizi, String descrizione) {
        this.nome = nome;
        this.indirizzo = indirizzo;
        this.localita = localita;
        this.prezzo = prezzo;
        this.cucina = cucina;
        this.longitudine = longitudine;
        this.latitudine = latitudine;
        this.numeroTelefono = numeroTelefono;
        this.url = url;
        this.sitoWeb = sitoWeb;
        this.premio = premio;
        this.stellaVerde = stellaVerde;
        this.servizi = servizi;
        this.descrizione = descrizione;
    }

    /**
     * Restituisce il nome del ristorante.
     *
     * @return il nome del ristorante.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce l'indirizzo del ristorante.
     *
     * @return l'indirizzo del ristorante.
     */
    public String getIndirizzo() {
        return indirizzo;
    }

    /**
     * Restituisce la località del ristorante.
     *
     * @return la località del ristorante.
     */
    public String getLocalita() {
        return localita;
    }

    /**
     * Restituisce il prezzo del ristorante.
     *
     * @return il prezzo come stringa.
     */
    public String getPrezzo() {
        return prezzo;
    }

    /**
     * Restituisce il tipo di cucina offerto dal ristorante.
     *
     * @return il tipo di cucina.
     */
    public String getCucina() {
        return cucina;
    }

    /**
     * Restituisce la longitudine della posizione del ristorante.
     *
     * @return la longitudine.
     */
    public double getLongitudine() {
        return longitudine;
    }

    /**
     * Restituisce la latitudine della posizione del ristorante.
     *
     * @return la latitudine.
     */
    public double getLatitudine() {
        return latitudine;
    }

    /**
     * Restituisce il numero di telefono del ristorante.
     *
     * @return il numero di telefono.
     */
    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    /**
     * Restituisce l'URL associato al ristorante.
     *
     * @return l'URL del ristorante.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Restituisce il sito web del ristorante.
     *
     * @return il sito web del ristorante.
     */
    public String getSitoWeb() {
        return sitoWeb;
    }

    /**
     * Restituisce il premio assegnato al ristorante.
     *
     * @return il premio.
     */
    public String getPremio() {
        return premio;
    }

    /**
     * Restituisce lo status "stella verde" del ristorante.
     *
     * @return lo status "stella verde".
     */
    public String getStellaVerde() {
        return stellaVerde;
    }

    /**
     * Restituisce i servizi offerti dal ristorante.
     *
     * @return i servizi.
     */
    public String getServizi() {
        return servizi;
    }

    /**
     * Restituisce una descrizione del ristorante.
     *
     * @return la descrizione.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Restituisce una rappresentazione testuale dell'oggetto {@code Ristorante}, utile per scopi di debug.
     *
     * @return una stringa che descrive il ristorante e le sue proprietà.
     */
    @Override
    public String toString() {
        return "Ristorante{" +
                "nome='" + nome + '\'' +
                ", indirizzo='" + indirizzo + '\'' +
                ", localita='" + localita + '\'' +
                ", prezzo='" + prezzo + '\'' +
                ", cucina='" + cucina + '\'' +
                ", longitudine=" + longitudine +
                ", latitudine=" + latitudine +
                ", numeroTelefono='" + numeroTelefono + '\'' +
                ", url='" + url + '\'' +
                ", sitoWeb='" + sitoWeb + '\'' +
                ", premio='" + premio + '\'' +
                ", stellaVerde='" + stellaVerde + '\'' +
                ", servizi='" + servizi + '\'' +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}
