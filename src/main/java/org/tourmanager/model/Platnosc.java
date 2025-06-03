package org.tourmanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "platnosc")
public class Platnosc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "umowa_nr", nullable = false)
    private Umowa umowa;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal kwota;

    @Column(name = "data_wplaty")
    private LocalDate dataWplaty;

    @Column(name = "metoda_platnosci", length = 50)
    private String metodaPlatnosci;

    @Column(name = "status_platnosci", length = 30)
    private String statusPlatnosci;

    @Column(length = 200)
    private String opis;

    @Column(name = "numer_transakcji", length = 100)
    private String numerTransakcji;

    public Platnosc() {}

    public Platnosc(Umowa umowa, BigDecimal kwota, String metodaPlatnosci, String opis) {
        this.umowa = umowa;
        this.kwota = kwota;
        this.metodaPlatnosci = metodaPlatnosci;
        this.opis = opis;
        this.dataWplaty = LocalDate.now();
        this.statusPlatnosci = "OCZEKUJACA";
        this.numerTransakcji = generujNumerTransakcji();
    }

    public boolean zrealizujPlatnosc() {
        if ("OCZEKUJACA".equals(statusPlatnosci)) {
            this.statusPlatnosci = "ZREALIZOWANA";
            this.dataWplaty = LocalDate.now();
            System.out.println("Płatność " + numerTransakcji + " została zrealizowana: " + kwota + " PLN");
            return true;
        }
        return false;
    }

    public boolean anulujPlatnosc(String powod) {
        if (!"ANULOWANA".equals(statusPlatnosci)) {
            this.statusPlatnosci = "ANULOWANA";
            this.opis = (opis != null ? opis + " | " : "") + "Anulowana: " + powod;
            System.out.println("Płatność " + numerTransakcji + " została anulowana: " + powod);
            return true;
        }
        return false;
    }

    private String generujNumerTransakcji() {
        return "TR/" + LocalDate.now().getYear() + "/" + System.currentTimeMillis() % 100000;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Umowa getUmowa() { return umowa; }
    public void setUmowa(Umowa umowa) { this.umowa = umowa; }

    public BigDecimal getKwota() { return kwota; }
    public void setKwota(BigDecimal kwota) { this.kwota = kwota; }

    public LocalDate getDataWplaty() { return dataWplaty; }
    public void setDataWplaty(LocalDate dataWplaty) { this.dataWplaty = dataWplaty; }

    public String getMetodaPlatnosci() { return metodaPlatnosci; }
    public void setMetodaPlatnosci(String metodaPlatnosci) { this.metodaPlatnosci = metodaPlatnosci; }

    public String getStatusPlatnosci() { return statusPlatnosci; }
    public void setStatusPlatnosci(String statusPlatnosci) { this.statusPlatnosci = statusPlatnosci; }

    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }

    public String getNumerTransakcji() { return numerTransakcji; }
    public void setNumerTransakcji(String numerTransakcji) { this.numerTransakcji = numerTransakcji; }

    @Override
    public String toString() {
        return numerTransakcji + ": " + kwota + " PLN (" + metodaPlatnosci + ") - " + statusPlatnosci;
    }
}