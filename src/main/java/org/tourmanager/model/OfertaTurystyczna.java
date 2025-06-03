package org.tourmanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "oferta_turystyczna")
public class OfertaTurystyczna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nazwa;

    @Column(columnDefinition = "TEXT")
    private String opis;

    @Column(name = "kraj_docelowy", length = 100)
    private String krajDocelowy;

    @Column(name = "data_wyjazdu")
    private LocalDate dataWyjazdu;

    @Column(name = "data_powrotu")
    private LocalDate dataPowrotu;

    @Column(name = "cena_za_osobe", precision = 10, scale = 2)
    private BigDecimal cenaZaOsobe;

    @Column(name = "maks_liczba_uczestnikow")
    private Integer maksLiczbaUczestnikow;

    @Column(name = "dostepne_miejsca")
    private Integer dostepneMiejsca;

    @Column(name = "typ_wycieczki", length = 50)
    private String typWycieczki;

    @Column(name = "status_oferty", length = 30)
    private String statusOferty;

    @Column(name = "data_utworzenia")
    private LocalDate dataUtworzenia;

    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Umowa> umowy = new ArrayList<>();

    public OfertaTurystyczna() {}

    public OfertaTurystyczna(String nazwa, String opis, String krajDocelowy,
                             LocalDate dataWyjazdu, LocalDate dataPowrotu,
                             BigDecimal cenaZaOsobe, Integer maksLiczbaUczestnikow, String typWycieczki) {
        this.nazwa = nazwa;
        this.opis = opis;
        this.krajDocelowy = krajDocelowy;
        this.dataWyjazdu = dataWyjazdu;
        this.dataPowrotu = dataPowrotu;
        this.cenaZaOsobe = cenaZaOsobe;
        this.maksLiczbaUczestnikow = maksLiczbaUczestnikow;
        this.dostepneMiejsca = maksLiczbaUczestnikow;
        this.typWycieczki = typWycieczki;
        this.statusOferty = "AKTYWNA";
        this.dataUtworzenia = LocalDate.now();
    }

    public boolean dodajOferte() {
        try {
            if (nazwa == null || nazwa.trim().isEmpty()) {
                throw new IllegalArgumentException("Nazwa oferty nie może być pusta");
            }
            if (dataWyjazdu == null || dataPowrotu == null) {
                throw new IllegalArgumentException("Daty wyjazdu i powrotu muszą być określone");
            }
            if (dataWyjazdu.isAfter(dataPowrotu)) {
                throw new IllegalArgumentException("Data wyjazdu nie może być późniejsza niż data powrotu");
            }
            if (dataWyjazdu.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Data wyjazdu nie może być z przeszłości");
            }
            if (cenaZaOsobe == null || cenaZaOsobe.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cena musi być większa od zera");
            }
            if (maksLiczbaUczestnikow == null || maksLiczbaUczestnikow <= 0) {
                throw new IllegalArgumentException("Maksymalna liczba uczestników musi być większa od zera");
            }

            this.dostepneMiejsca = this.maksLiczbaUczestnikow;
            this.statusOferty = "AKTYWNA";
            this.dataUtworzenia = LocalDate.now();

            System.out.println("Oferta '" + nazwa + "' została pomyślnie utworzona");
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd walidacji: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas tworzenia oferty: " + e.getMessage());
            return false;
        }
    }

    public int sprawdzDostepnosc() {
        try {
            if (!"AKTYWNA".equals(statusOferty)) {
                System.out.println("Oferta '" + nazwa + "' nie jest aktywna");
                return 0;
            }

            if (dataWyjazdu != null && dataWyjazdu.isBefore(LocalDate.now())) {
                System.out.println("Termin wyjazdu dla oferty '" + nazwa + "' już minął");
                this.statusOferty = "NIEAKTYWNA"; // Automatycznie dezaktywuj przeszłe oferty
                return 0;
            }

            int zarezerwowaneMiejsca = 0;
            if (umowy != null) {
                for (Umowa umowa : umowy) {
                    if ("PODPISANA".equals(umowa.getStatusUmowy()) ||
                            "OPLACONA".equals(umowa.getStatusUmowy()) ||
                            "ZALICZKA_WPLACONA".equals(umowa.getStatusUmowy())) {
                        zarezerwowaneMiejsca += umowa.getLiczbaOsob();
                    }
                }
            }

            this.dostepneMiejsca = this.maksLiczbaUczestnikow - zarezerwowaneMiejsca;
            if (this.dostepneMiejsca < 0) {
                this.dostepneMiejsca = 0;
            }

            System.out.println("Dostępne miejsca dla oferty '" + nazwa + "': " + dostepneMiejsca);
            return dostepneMiejsca;

        } catch (Exception e) {
            System.err.println("Błąd podczas sprawdzania dostępności: " + e.getMessage());
            return 0;
        }
    }

    public boolean rezerwujMiejsca(int liczba) {
        try {
            if (liczba <= 0) {
                throw new IllegalArgumentException("Liczba miejsc do rezerwacji musi być większa od zera");
            }

            int aktualneDostepne = sprawdzDostepnosc();

            if (aktualneDostepne < liczba) {
                System.err.println("Brak wystarczającej liczby miejsc. Dostępne: " +
                        aktualneDostepne + ", wymagane: " + liczba);
                return false;
            }

            this.dostepneMiejsca -= liczba;

            System.out.println("Zarezerwowano " + liczba + " miejsc w ofercie '" + nazwa + "'");
            System.out.println("Pozostałe dostępne miejsca: " + this.dostepneMiejsca);

            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd parametru: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas rezerwacji: " + e.getMessage());
            return false;
        }
    }

    public BigDecimal obliczCeneCalkowita(int liczbaOsob) {
        try {
            if (liczbaOsob <= 0) {
                throw new IllegalArgumentException("Liczba osób musi być większa od zera");
            }

            if (cenaZaOsobe == null) {
                throw new IllegalStateException("Cena za osobę nie została ustawiona");
            }

            BigDecimal cenaCalkowita = cenaZaOsobe.multiply(new BigDecimal(liczbaOsob));

            // Rabat grupowy (powyżej 10 osób - 5% rabatu)
            if (liczbaOsob >= 10) {
                BigDecimal rabat = cenaCalkowita.multiply(new BigDecimal("0.05"));
                cenaCalkowita = cenaCalkowita.subtract(rabat);
                System.out.println("Zastosowano rabat grupowy 5%: -" + rabat + " PLN");
            }

            // Rabat za wczesną rezerwację (powyżej 60 dni - 3% rabatu)
            long dniDoWyjazdu = LocalDate.now().until(dataWyjazdu).getDays();
            if (dniDoWyjazdu >= 60) {
                BigDecimal rabatWczesny = cenaCalkowita.multiply(new BigDecimal("0.03"));
                cenaCalkowita = cenaCalkowita.subtract(rabatWczesny);
                System.out.println("Zastosowano rabat za wczesną rezerwację 3%: -" + rabatWczesny + " PLN");
            }

            System.out.println("Cena całkowita dla " + liczbaOsob + " osób: " + cenaCalkowita + " PLN");
            return cenaCalkowita;

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Błąd podczas obliczania ceny: " + e.getMessage());
            return BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas obliczania ceny: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }

    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }

    public String getKrajDocelowy() { return krajDocelowy; }
    public void setKrajDocelowy(String krajDocelowy) { this.krajDocelowy = krajDocelowy; }

    public LocalDate getDataWyjazdu() { return dataWyjazdu; }
    public void setDataWyjazdu(LocalDate dataWyjazdu) { this.dataWyjazdu = dataWyjazdu; }

    public LocalDate getDataPowrotu() { return dataPowrotu; }
    public void setDataPowrotu(LocalDate dataPowrotu) { this.dataPowrotu = dataPowrotu; }

    public BigDecimal getCenaZaOsobe() { return cenaZaOsobe; }
    public void setCenaZaOsobe(BigDecimal cenaZaOsobe) { this.cenaZaOsobe = cenaZaOsobe; }

    public Integer getMaksLiczbaUczestnikow() { return maksLiczbaUczestnikow; }
    public void setMaksLiczbaUczestnikow(Integer maksLiczbaUczestnikow) {
        this.maksLiczbaUczestnikow = maksLiczbaUczestnikow;
    }

    public Integer getDostepneMiejsca() { return dostepneMiejsca; }
    public void setDostepneMiejsca(Integer dostepneMiejsca) { this.dostepneMiejsca = dostepneMiejsca; }

    public String getTypWycieczki() { return typWycieczki; }
    public void setTypWycieczki(String typWycieczki) { this.typWycieczki = typWycieczki; }

    public String getStatusOferty() { return statusOferty; }
    public void setStatusOferty(String statusOferty) { this.statusOferty = statusOferty; }

    public LocalDate getDataUtworzenia() { return dataUtworzenia; }
    public void setDataUtworzenia(LocalDate dataUtworzenia) { this.dataUtworzenia = dataUtworzenia; }

    public List<Umowa> getUmowy() { return umowy; }
    public void setUmowy(List<Umowa> umowy) { this.umowy = umowy; }

    @Override
    public String toString() {
        return nazwa + " (" + krajDocelowy + ") - " + dataWyjazdu + " do " + dataPowrotu;
    }
}