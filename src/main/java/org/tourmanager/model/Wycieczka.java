package org.tourmanager.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "wycieczka")
public class Wycieczka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaTurystyczna oferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilot_id")
    private Pilot pilot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "data_rozpoczecia")
    private LocalDate dataRozpoczecia;

    @Column(name = "data_zakonczenia")
    private LocalDate dataZakonczenia;

    @Column(name = "liczba_uczestnikow")
    private Integer liczbaUczestnikow;

    @Column(name = "status_wycieczki", length = 30)
    private String statusWycieczki;

    @Column(columnDefinition = "TEXT")
    private String uwagi;

    public Wycieczka() {}

    public Wycieczka(OfertaTurystyczna oferta, LocalDate dataRozpoczecia, LocalDate dataZakonczenia) {
        this.oferta = oferta;
        this.dataRozpoczecia = dataRozpoczecia;
        this.dataZakonczenia = dataZakonczenia;
        this.statusWycieczki = "PLANOWANA";
        this.liczbaUczestnikow = 0;
    }

    public boolean rozpocznijWycieczke() {
        if ("PLANOWANA".equals(statusWycieczki)) {
            this.statusWycieczki = "W_TRAKCIE";
            System.out.println("Wycieczka " + id + " została rozpoczęta");
            return true;
        }
        return false;
    }

    public boolean zakonczWycieczke() {
        if ("W_TRAKCIE".equals(statusWycieczki)) {
            this.statusWycieczki = "ZAKONCZONA";
            System.out.println("Wycieczka " + id + " została zakończona");
            return true;
        }
        return false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OfertaTurystyczna getOferta() { return oferta; }
    public void setOferta(OfertaTurystyczna oferta) { this.oferta = oferta; }

    public Pilot getPilot() { return pilot; }
    public void setPilot(Pilot pilot) { this.pilot = pilot; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public LocalDate getDataRozpoczecia() { return dataRozpoczecia; }
    public void setDataRozpoczecia(LocalDate dataRozpoczecia) { this.dataRozpoczecia = dataRozpoczecia; }

    public LocalDate getDataZakonczenia() { return dataZakonczenia; }
    public void setDataZakonczenia(LocalDate dataZakonczenia) { this.dataZakonczenia = dataZakonczenia; }

    public Integer getLiczbaUczestnikow() { return liczbaUczestnikow; }
    public void setLiczbaUczestnikow(Integer liczbaUczestnikow) { this.liczbaUczestnikow = liczbaUczestnikow; }

    public String getStatusWycieczki() { return statusWycieczki; }
    public void setStatusWycieczki(String statusWycieczki) { this.statusWycieczki = statusWycieczki; }

    public String getUwagi() { return uwagi; }
    public void setUwagi(String uwagi) { this.uwagi = uwagi; }

    @Override
    public String toString() {
        return "Wycieczka " + id + ": " + (oferta != null ? oferta.getNazwa() : "Brak oferty") +
                " (" + statusWycieczki + ")";
    }
}