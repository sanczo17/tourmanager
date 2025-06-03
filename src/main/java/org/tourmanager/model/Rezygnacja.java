package org.tourmanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rezygnacja")
public class Rezygnacja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "umowa_nr", nullable = false)
    private Umowa umowa;

    @Column(name = "data_rezygnacji")
    private LocalDate dataRezygnacji;

    @Column(name = "powod_rezygnacji", length = 200)
    private String powodRezygnacji;

    @Column(name = "koszt_rezygnacji", precision = 10, scale = 2)
    private BigDecimal kosztRezygnacji;

    @Column(name = "kwota_zwrotu", precision = 10, scale = 2)
    private BigDecimal kwotaZwrotu;

    @Column(name = "status_rezygnacji", length = 30)
    private String statusRezygnacji;

    @Column(name = "data_zwrotu")
    private LocalDate dataZwrotu;

    @Column(name = "sposob_zwrotu", length = 50)
    private String sposobZwrotu;

    @Column(columnDefinition = "TEXT")
    private String uwagi;

    public Rezygnacja() {}

    public Rezygnacja(Umowa umowa, String powodRezygnacji) {
        this.umowa = umowa;
        this.powodRezygnacji = powodRezygnacji;
        this.dataRezygnacji = LocalDate.now();
        this.statusRezygnacji = "ZAREJESTROWANA";
    }

    public boolean zatwierdzRezygnacje() {
        if ("ZAREJESTROWANA".equals(statusRezygnacji)) {
            this.statusRezygnacji = "ZATWIERDZONA";
            System.out.println("Rezygnacja dla umowy " +
                    (umowa != null ? umowa.getNrUmowy() : "BRAK") + " została zatwierdzona");
            return true;
        }
        return false;
    }

    public boolean zrealizujZwrot(String sposobZwrotu) {
        if ("ZATWIERDZONA".equals(statusRezygnacji)) {
            this.statusRezygnacji = "ZWROT_ZREALIZOWANY";
            this.dataZwrotu = LocalDate.now();
            this.sposobZwrotu = sposobZwrotu;
            System.out.println("Zwrot " + kwotaZwrotu + " PLN został zrealizowany (" + sposobZwrotu + ")");
            return true;
        }
        return false;
    }

    public boolean odrzucRezygnacje(String powod) {
        if ("ZAREJESTROWANA".equals(statusRezygnacji)) {
            this.statusRezygnacji = "ODRZUCONA";
            this.uwagi = (uwagi != null ? uwagi + " | " : "") + "Odrzucona: " + powod;
            System.out.println("Rezygnacja dla umowy " +
                    (umowa != null ? umowa.getNrUmowy() : "BRAK") + " została odrzucona: " + powod);
            return true;
        }
        return false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Umowa getUmowa() { return umowa; }
    public void setUmowa(Umowa umowa) { this.umowa = umowa; }

    public LocalDate getDataRezygnacji() { return dataRezygnacji; }
    public void setDataRezygnacji(LocalDate dataRezygnacji) { this.dataRezygnacji = dataRezygnacji; }

    public String getPowodRezygnacji() { return powodRezygnacji; }
    public void setPowodRezygnacji(String powodRezygnacji) { this.powodRezygnacji = powodRezygnacji; }

    public BigDecimal getKosztRezygnacji() { return kosztRezygnacji; }
    public void setKosztRezygnacji(BigDecimal kosztRezygnacji) { this.kosztRezygnacji = kosztRezygnacji; }

    public BigDecimal getKwotaZwrotu() { return kwotaZwrotu; }
    public void setKwotaZwrotu(BigDecimal kwotaZwrotu) { this.kwotaZwrotu = kwotaZwrotu; }

    public String getStatusRezygnacji() { return statusRezygnacji; }
    public void setStatusRezygnacji(String statusRezygnacji) { this.statusRezygnacji = statusRezygnacji; }

    public LocalDate getDataZwrotu() { return dataZwrotu; }
    public void setDataZwrotu(LocalDate dataZwrotu) { this.dataZwrotu = dataZwrotu; }

    public String getSposobZwrotu() { return sposobZwrotu; }
    public void setSposobZwrotu(String sposobZwrotu) { this.sposobZwrotu = sposobZwrotu; }

    public String getUwagi() { return uwagi; }
    public void setUwagi(String uwagi) { this.uwagi = uwagi; }

    @Override
    public String toString() {
        return "Rezygnacja " + id + " dla " + (umowa != null ? umowa.getNrUmowy() : "brak umowy") +
                " (" + statusRezygnacji + ")";
    }
}