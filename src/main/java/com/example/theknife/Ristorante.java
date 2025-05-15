package com.example.theknife;

/**
 * @author [Samuele Secchi, 761031, Sede CO]
 * @author [Flavio Marin, 759910, Sede CO]
 * @author [Matilde Lecchi, 759875, Sede CO]
 * @author [Davide Caccia, 760742, Sede CO]
 * @version 1.0
 */

public class Ristorante {

    private String nome;
    private String indirizzo;
    private String localita;
    private String prezzo;
    private String cucina;
    private double longitudine;
    private double latitudine;
    private String numeroTelefono;
    private String url;
    private String sitoWeb;
    private String premio;
    private String stellaVerde;
    private String servizi;
    private String descrizione;

    // Costruttore
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

    // Getters necessari per popolare il TableView
    public String getNome() { return nome; }
    public String getIndirizzo() { return indirizzo; }
    public String getLocalita() { return localita; }
    public String getPrezzo() { return prezzo; }
    public String getCucina() { return cucina; }
    public double getLongitudine() { return longitudine; }
    public double getLatitudine() { return latitudine; }
    public String getNumeroTelefono() { return numeroTelefono; }
    public String getUrl() { return url; }
    public String getSitoWeb() { return sitoWeb; }
    public String getPremio() { return premio; }
    public String getStellaVerde() { return stellaVerde; }
    public String getServizi() { return servizi; }
    public String getDescrizione() { return descrizione; }

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

