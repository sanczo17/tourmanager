package org.tourmanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "umowa")
public class Umowa {

    @Id
    @Column(name = "nr_umowy", length = 20)
    private String nrUmowy;

    @Column(name = "data_zawarcia")
    private LocalDate dataZawarcia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klient_id", nullable = false)
    private Klient klient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaTurystyczna oferta;

    @Column(name = "liczba_osob")
    private Integer liczbaOsob;

    @Column(name = "cena_calkowita", precision = 10, scale = 2)
    private BigDecimal cenaCalkowita;

    @Column(precision = 10, scale = 2)
    private BigDecimal zaliczka;

    @Column(name = "data_wplaty_zaliczki")
    private LocalDate dataWplatyZaliczki;

    @Column(name = "pozostala_doplata", precision = 10, scale = 2)
    private BigDecimal pozostalaDoplata;

    @Column(name = "termin_doplaty")
    private LocalDate terminDoplaty;

    @Column(name = "status_umowy", length = 30)
    private String statusUmowy;

    @Column(columnDefinition = "TEXT")
    private String uwagi;

    @OneToMany(mappedBy = "umowa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Platnosc> platnosci = new ArrayList<>();

    @OneToOne(mappedBy = "umowa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Rezygnacja rezygnacja;


    public Umowa() {}

    public Umowa(Klient klient, OfertaTurystyczna oferta, Integer liczbaOsob) {
        this.klient = klient;
        this.oferta = oferta;
        this.liczbaOsob = liczbaOsob;
        this.dataZawarcia = LocalDate.now();
        this.statusUmowy = "UTWORZONA";
        this.nrUmowy = generujNumerUmowy();
    }


    /**
     * METODA 9: Tworzy nową umowę z walidacją i obliczeniem kosztów
     * @return true jeśli umowa została pomyślnie utworzona
     */
    public boolean utworzUmowe() {
        try {
            if (klient == null) {
                throw new IllegalArgumentException("Klient musi być określony");
            }

            if (oferta == null) {
                throw new IllegalArgumentException("Oferta musi być określona");
            }

            if (liczbaOsob == null || liczbaOsob <= 0) {
                throw new IllegalArgumentException("Liczba osób musi być większa od zera");
            }

            int dostepneMiejsca = oferta.sprawdzDostepnosc();
            if (dostepneMiejsca < liczbaOsob) {
                throw new IllegalStateException("Brak wystarczającej liczby miejsc. Dostępne: " +
                        dostepneMiejsca + ", wymagane: " + liczbaOsob);
            }

            this.cenaCalkowita = oferta.obliczCeneCalkowita(liczbaOsob);

            this.zaliczka = cenaCalkowita.multiply(new BigDecimal("0.30"));

            this.pozostalaDoplata = cenaCalkowita.subtract(zaliczka);

            this.terminDoplaty = oferta.getDataWyjazdu().minusDays(14);

            if (this.nrUmowy == null) {
                this.nrUmowy = generujNumerUmowy();
            }

            this.dataZawarcia = LocalDate.now();

            if (!oferta.rezerwujMiejsca(liczbaOsob)) {
                throw new IllegalStateException("Nie udało się zarezerwować miejsc");
            }

            this.statusUmowy = "PODPISANA";

            System.out.println("Umowa " + nrUmowy + " została pomyślnie utworzona");
            System.out.println("Klient: " + klient.getImie() + " " + klient.getNazwisko());
            System.out.println("Oferta: " + oferta.getNazwa());
            System.out.println("Cena całkowita: " + cenaCalkowita + " PLN");
            System.out.println("Zaliczka: " + zaliczka + " PLN");

            return true;

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Błąd podczas tworzenia umowy: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas tworzenia umowy: " + e.getMessage());
            return false;
        }
    }

    /**
     * METODA 10: Generuje unikalny numer umowy w formacie UM/RRRR/NNNN
     * @return wygenerowany numer umowy
     */
    public String generujNumerUmowy() {
        try {
            int aktualnyRok = LocalDate.now().getYear();
            int kolejnyNumer = pobierzKolejnyNumerUmowy(aktualnyRok);
            String numerFormatowany = String.format("%04d", kolejnyNumer);
            String numerUmowy = "UM/" + aktualnyRok + "/" + numerFormatowany;

            System.out.println("Wygenerowano numer umowy: " + numerUmowy);
            return numerUmowy;

        } catch (Exception e) {
            System.err.println("Błąd podczas generowania numeru umowy: " + e.getMessage());
            long timestamp = System.currentTimeMillis() % 10000;
            String fallbackNumer = "UM/" + LocalDate.now().getYear() + "/" + String.format("%04d", timestamp);
            System.out.println("Użyto numeru fallback: " + fallbackNumer);
            return fallbackNumer;
        }
    }

    /**
     * METODA 11: Rejestruje wpłatę zaliczki
     * @param kwotaZaliczki wpłacona kwota zaliczki
     * @param metodaPlatnosci sposób płatności
     * @return true jeśli zaliczka została zarejestrowana
     */
    public boolean zarejestrujZaliczke(BigDecimal kwotaZaliczki, String metodaPlatnosci) {
        try {
            if (kwotaZaliczki == null || kwotaZaliczki.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Kwota zaliczki musi być większa od zera");
            }

            if (metodaPlatnosci == null || metodaPlatnosci.trim().isEmpty()) {
                throw new IllegalArgumentException("Metoda płatności musi być określona");
            }

            if (dataWplatyZaliczki != null) {
                throw new IllegalStateException("Zaliczka została już wpłacona w dniu: " + dataWplatyZaliczki);
            }

            BigDecimal roznica = kwotaZaliczki.subtract(zaliczka).abs();
            if (roznica.compareTo(new BigDecimal("10.00")) > 0) {
                System.out.println("Uwaga: Wpłacona zaliczka (" + kwotaZaliczki +
                        " PLN) różni się od wymaganej (" + zaliczka + " PLN)");
            }

            Platnosc platnosc = new Platnosc();
            platnosc.setUmowa(this);
            platnosc.setKwota(kwotaZaliczki);
            platnosc.setDataWplaty(LocalDate.now());
            platnosc.setMetodaPlatnosci(metodaPlatnosci);
            platnosc.setStatusPlatnosci("ZREALIZOWANA");
            platnosc.setOpis("Zaliczka za wycieczkę");

            this.platnosci.add(platnosc);

            this.dataWplatyZaliczki = LocalDate.now();
            this.pozostalaDoplata = this.cenaCalkowita.subtract(kwotaZaliczki);

            if (kwotaZaliczki.compareTo(cenaCalkowita) >= 0) {
                this.statusUmowy = "OPLACONA";
                this.pozostalaDoplata = BigDecimal.ZERO;
                System.out.println("Umowa została w pełni opłacona");
            } else {
                this.statusUmowy = "ZALICZKA_WPLACONA";
                System.out.println("Zaliczka wpłacona. Pozostała dopłata: " + pozostalaDoplata + " PLN");
            }

            System.out.println("Zarejestrowano zaliczkę: " + kwotaZaliczki + " PLN (" + metodaPlatnosci + ")");
            return true;

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Błąd podczas rejestracji zaliczki: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas rejestracji zaliczki: " + e.getMessage());
            return false;
        }
    }

    /**
     * METODA 12: Anuluje umowę z obliczeniem kosztów rezygnacji
     * @param powodRezygnacji powód anulowania
     * @return true jeśli anulowanie się powiodło
     */
    public boolean anulujUmowe(String powodRezygnacji) {
        try {
            if ("ANULOWANA".equals(statusUmowy)) {
                throw new IllegalStateException("Umowa została już anulowana");
            }

            if ("ZREALIZOWANA".equals(statusUmowy)) {
                throw new IllegalStateException("Nie można anulować zrealizowanej umowy");
            }

            BigDecimal kosztRezygnacji = obliczKosztRezygnacji();
            BigDecimal kwotaZwrotu = obliczKwoteZwrotu(kosztRezygnacji);

            Rezygnacja nowaRezygnacja = new Rezygnacja();
            nowaRezygnacja.setUmowa(this);
            nowaRezygnacja.setDataRezygnacji(LocalDate.now());
            nowaRezygnacja.setPowodRezygnacji(powodRezygnacji);
            nowaRezygnacja.setKosztRezygnacji(kosztRezygnacji);
            nowaRezygnacja.setKwotaZwrotu(kwotaZwrotu);
            nowaRezygnacja.setStatusRezygnacji("ZAREJESTROWANA");

            this.rezygnacja = nowaRezygnacja;

            oferta.setDostepneMiejsca(oferta.getDostepneMiejsca() + liczbaOsob);

            this.statusUmowy = "ANULOWANA";

            System.out.println("Umowa " + nrUmowy + " została anulowana");
            System.out.println("Powód: " + powodRezygnacji);
            System.out.println("Koszt rezygnacji: " + kosztRezygnacji + " PLN");
            System.out.println("Kwota zwrotu: " + kwotaZwrotu + " PLN");

            return true;

        } catch (IllegalStateException e) {
            System.err.println("Błąd podczas anulowania umowy: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas anulowania umowy: " + e.getMessage());
            return false;
        }
    }



    /**
     * Oblicza koszt rezygnacji na podstawie czasu do wyjazdu
     */
    private BigDecimal obliczKosztRezygnacji() {
        long dniDoWyjazdu = LocalDate.now().until(oferta.getDataWyjazdu()).getDays();

        if (dniDoWyjazdu >= 30) {
            // Powyżej 30 dni - 10% kary
            return cenaCalkowita.multiply(new BigDecimal("0.10"));
        } else if (dniDoWyjazdu >= 14) {
            // 14-29 dni - 25% kary
            return cenaCalkowita.multiply(new BigDecimal("0.25"));
        } else if (dniDoWyjazdu >= 7) {
            // 7-13 dni - 50% kary
            return cenaCalkowita.multiply(new BigDecimal("0.50"));
        } else {
            // Mniej niż 7 dni - 100% kary
            return cenaCalkowita;
        }
    }

    /**
     * Oblicza kwotę zwrotu po odjęciu kosztów rezygnacji
     */
    private BigDecimal obliczKwoteZwrotu(BigDecimal kosztRezygnacji) {
        BigDecimal wplaconeKwoty = platnosci.stream()
                .filter(p -> "ZREALIZOWANA".equals(p.getStatusPlatnosci()))
                .map(Platnosc::getKwota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal zwrot = wplaconeKwoty.subtract(kosztRezygnacji);
        return zwrot.compareTo(BigDecimal.ZERO) > 0 ? zwrot : BigDecimal.ZERO;
    }

    /**
     * Symulacja pobierania kolejnego numeru umowy z bazy danych
     */
    private int pobierzKolejnyNumerUmowy(int rok) {
        // W rzeczywistej aplikacji byłoby to zapytanie SQL:
        // SELECT COALESCE(MAX(CAST(SUBSTRING(nr_umowy, 9) AS INTEGER)), 0) + 1
        // FROM umowa WHERE nr_umowy LIKE 'UM/rok/%'

        // Symulacja - zwracanie losowego numeru dla demonstracji
        return (int) (Math.random() * 9999) + 1;
    }

    public String getNrUmowy() { return nrUmowy; }
    public void setNrUmowy(String nrUmowy) { this.nrUmowy = nrUmowy; }

    public LocalDate getDataZawarcia() { return dataZawarcia; }
    public void setDataZawarcia(LocalDate dataZawarcia) { this.dataZawarcia = dataZawarcia; }

    public Klient getKlient() { return klient; }
    public void setKlient(Klient klient) { this.klient = klient; }

    public OfertaTurystyczna getOferta() { return oferta; }
    public void setOferta(OfertaTurystyczna oferta) { this.oferta = oferta; }

    public Integer getLiczbaOsob() { return liczbaOsob; }
    public void setLiczbaOsob(Integer liczbaOsob) { this.liczbaOsob = liczbaOsob; }

    public BigDecimal getCenaCalkowita() { return cenaCalkowita; }
    public void setCenaCalkowita(BigDecimal cenaCalkowita) { this.cenaCalkowita = cenaCalkowita; }

    public BigDecimal getZaliczka() { return zaliczka; }
    public void setZaliczka(BigDecimal zaliczka) { this.zaliczka = zaliczka; }

    public LocalDate getDataWplatyZaliczki() { return dataWplatyZaliczki; }
    public void setDataWplatyZaliczki(LocalDate dataWplatyZaliczki) { this.dataWplatyZaliczki = dataWplatyZaliczki; }

    public BigDecimal getPozostalaDoplata() { return pozostalaDoplata; }
    public void setPozostalaDoplata(BigDecimal pozostalaDoplata) { this.pozostalaDoplata = pozostalaDoplata; }

    public LocalDate getTerminDoplaty() { return terminDoplaty; }
    public void setTerminDoplaty(LocalDate terminDoplaty) { this.terminDoplaty = terminDoplaty; }

    public String getStatusUmowy() { return statusUmowy; }
    public void setStatusUmowy(String statusUmowy) { this.statusUmowy = statusUmowy; }

    public String getUwagi() { return uwagi; }
    public void setUwagi(String uwagi) { this.uwagi = uwagi; }

    public List<Platnosc> getPlatnosci() { return platnosci; }
    public void setPlatnosci(List<Platnosc> platnosci) { this.platnosci = platnosci; }

    public Rezygnacja getRezygnacja() { return rezygnacja; }
    public void setRezygnacja(Rezygnacja rezygnacja) { this.rezygnacja = rezygnacja; }

    @Override
    public String toString() {
        return nrUmowy + " - " + klient.getImie() + " " + klient.getNazwisko() +
                " (" + oferta.getNazwa() + ")";
    }
}