package org.tourmanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotel")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nazwa;

    @Column(length = 200)
    private String adres;

    @Column(length = 100)
    private String miasto;

    @Column(length = 50)
    private String kraj;

    @Column
    private Integer kategoria; // 1-5 gwiazdek

    @Column(length = 20)
    private String telefon;

    @Column(length = 100)
    private String email;

    @Column(name = "osoba_kontaktowa", length = 100)
    private String osobaKontaktowa;

    @Column(name = "warunki_wspolpracy", columnDefinition = "TEXT")
    private String warunkiWspolpracy;

    @Column(name = "ocena_jakosci", precision = 3, scale = 2)
    private BigDecimal ocenaJakosci; // 1.00 - 10.00

    @Column(name = "status_wspolpracy", length = 30)
    private String statusWspolpracy;

    @Column(name = "data_rozpoczecia_wspolpracy")
    private LocalDate dataRozpoczeciaWspolpracy;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wycieczka> wycieczki = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OcenaHotelu> oceny = new ArrayList<>();


    public Hotel() {}

    public Hotel(String nazwa, String adres, String miasto, String kraj, Integer kategoria,
                 String telefon, String email, String osobaKontaktowa) {
        this.nazwa = nazwa;
        this.adres = adres;
        this.miasto = miasto;
        this.kraj = kraj;
        this.kategoria = kategoria;
        this.telefon = telefon;
        this.email = email;
        this.osobaKontaktowa = osobaKontaktowa;
        this.statusWspolpracy = "AKTYWNA";
        this.dataRozpoczeciaWspolpracy = LocalDate.now();
        this.ocenaJakosci = new BigDecimal("5.00"); // Domyślna ocena
    }


    /**
     * METODA 17: Dodaje nowy hotel do bazy partnerów
     * @return true jeśli hotel został pomyślnie dodany
     */
    public boolean dodajHotel() {
        try {
            if (nazwa == null || nazwa.trim().isEmpty()) {
                throw new IllegalArgumentException("Nazwa hotelu nie może być pusta");
            }

            if (miasto == null || miasto.trim().isEmpty()) {
                throw new IllegalArgumentException("Miasto jest wymagane");
            }

            if (kraj == null || kraj.trim().isEmpty()) {
                throw new IllegalArgumentException("Kraj jest wymagany");
            }

            if (kategoria == null || kategoria < 1 || kategoria > 5) {
                throw new IllegalArgumentException("Kategoria hotelu musi być w przedziale 1-5 gwiazdek");
            }

            if (telefon == null || telefon.trim().isEmpty()) {
                throw new IllegalArgumentException("Numer telefonu jest wymagany");
            }

            if (email != null && !email.contains("@")) {
                throw new IllegalArgumentException("Niepoprawny format adresu email");
            }

            this.statusWspolpracy = "AKTYWNA";
            this.dataRozpoczeciaWspolpracy = LocalDate.now();
            this.ocenaJakosci = new BigDecimal("5.00"); // Neutralna ocena startowa

            System.out.println("Hotel '" + nazwa + "' został pomyślnie dodany do bazy partnerów");
            System.out.println("Lokalizacja: " + miasto + ", " + kraj);
            System.out.println("Kategoria: " + kategoria + " gwiazdek");

            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd walidacji hotelu: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas dodawania hotelu: " + e.getMessage());
            return false;
        }
    }

    /**
     * METODA 18: Sprawdza dostępność hotelu w określonym okresie
     * @param dataOd data początku pobytu
     * @param dataDo data końca pobytu
     * @return true jeśli hotel jest dostępny
     */
    public boolean sprawdzDostepnosc(LocalDate dataOd, LocalDate dataDo) {
        try {
            if (dataOd == null || dataDo == null) {
                throw new IllegalArgumentException("Daty sprawdzania dostępności nie mogą być null");
            }

            if (dataOd.isAfter(dataDo)) {
                throw new IllegalArgumentException("Data początku nie może być późniejsza niż data końca");
            }

            if (dataOd.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Data początku nie może być z przeszłości");
            }

            if (!"AKTYWNA".equals(statusWspolpracy)) {
                System.out.println("Hotel '" + nazwa + "' nie ma aktywnej współpracy");
                return false;
            }

            for (Wycieczka wycieczka : wycieczki) {
                if ("PLANOWANA".equals(wycieczka.getStatusWycieczki()) ||
                        "W_TRAKCIE".equals(wycieczka.getStatusWycieczki())) {

                    LocalDate wycOd = wycieczka.getDataRozpoczecia();
                    LocalDate wycDo = wycieczka.getDataZakonczenia();

                    if (!(dataDo.isBefore(wycOd) || dataOd.isAfter(wycDo))) {
                        System.out.println("Hotel '" + nazwa + "' ma konflikt terminów z inną wycieczką");
                        return false;
                    }
                }
            }

            System.out.println("Hotel '" + nazwa + "' jest dostępny w okresie " + dataOd + " - " + dataDo);
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd parametru: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas sprawdzania dostępności hotelu: " + e.getMessage());
            return false;
        }
    }

    /**
     * METODA 19: Dodaje ocenę jakości hotelu
     * @param ocena ocena w skali 1-10
     * @param komentarz dodatkowy komentarz
     * @return true jeśli ocena została dodana
     */
    public boolean dodajOcene(int ocena, String komentarz) {
        try {
            if (ocena < 1 || ocena > 10) {
                throw new IllegalArgumentException("Ocena musi być w przedziale 1-10");
            }

            OcenaHotelu nowaOcena = new OcenaHotelu();
            nowaOcena.setHotel(this);
            nowaOcena.setOcena(ocena);
            nowaOcena.setKomentarz(komentarz);
            nowaOcena.setDataOceny(LocalDate.now());

            if (this.oceny == null) {
                this.oceny = new ArrayList<>();
            }

            this.oceny.add(nowaOcena);
            obliczSredniaOcene();

            System.out.println("Dodano ocenę " + ocena + "/10 dla hotelu '" + nazwa + "'");
            System.out.println("Nowa średnia ocena: " + this.ocenaJakosci);

            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd podczas dodawania oceny: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas dodawania oceny: " + e.getMessage());
            return false;
        }
    }

    /**
     * METODA 20: Oblicza średnią ocenę hotelu
     * @return średnia ocena jakości
     */
    public BigDecimal obliczSredniaOcene() {
        try {
            if (oceny.isEmpty()) {
                this.ocenaJakosci = new BigDecimal("5.00"); // Domyślna ocena
                return this.ocenaJakosci;
            }

            // Obliczenie średniej arytmetycznej
            double suma = 0;
            int liczba = 0;

            for (OcenaHotelu ocena : oceny) {
                suma += ocena.getOcena();
                liczba++;
            }

            double srednia = suma / liczba;
            this.ocenaJakosci = new BigDecimal(srednia).setScale(2, RoundingMode.HALF_UP);

            System.out.println("Średnia ocena hotelu '" + nazwa + "': " + this.ocenaJakosci +
                    "/10 (na podstawie " + liczba + " ocen)");

            return this.ocenaJakosci;

        } catch (Exception e) {
            System.err.println("Błąd podczas obliczania średniej oceny: " + e.getMessage());
            return new BigDecimal("5.00");
        }
    }


    /**
     * Aktualizuje dane kontaktowe hotelu
     */
    public boolean aktualizujDaneKontaktowe(String nowyTelefon, String nowyEmail, String nowaOsobaKontaktowa) {
        try {
            if (nowyTelefon != null && !nowyTelefon.trim().isEmpty()) {
                this.telefon = nowyTelefon.trim();
                System.out.println("Zaktualizowano telefon hotelu na: " + this.telefon);
            }

            if (nowyEmail != null && !nowyEmail.trim().isEmpty()) {
                if (nowyEmail.contains("@")) {
                    this.email = nowyEmail.trim();
                    System.out.println("Zaktualizowano email hotelu na: " + this.email);
                } else {
                    throw new IllegalArgumentException("Niepoprawny format adresu email");
                }
            }

            if (nowaOsobaKontaktowa != null && !nowaOsobaKontaktowa.trim().isEmpty()) {
                this.osobaKontaktowa = nowaOsobaKontaktowa.trim();
                System.out.println("Zaktualizowano osobę kontaktową na: " + this.osobaKontaktowa);
            }

            return true;

        } catch (Exception e) {
            System.err.println("Błąd podczas aktualizacji danych kontaktowych: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sprawdza czy hotel ma wysoką ocenę (powyżej 8.0)
     */
    public boolean sprawdzCzyWysokieOceny() {
        return ocenaJakosci != null && ocenaJakosci.compareTo(new BigDecimal("8.00")) >= 0;
    }

    /**
     * Oblicza liczbę wycieczek zrealizowanych w hotelu
     */
    public long obliczLiczbeZrealizowanychWycieczek() {
        return wycieczki.stream()
                .filter(w -> "ZAKONCZONA".equals(w.getStatusWycieczki()))
                .count();
    }

    /**
     * Sprawdza czy hotel jest stałym partnerem (powyżej 10 wycieczek)
     */
    public boolean sprawdzCzyStałyPartner() {
        return obliczLiczbeZrealizowanychWycieczek() >= 10;
    }

    /**
     * Generuje pełną nazwę hotelu z kategorią
     */
    public String generujPełnaNazwe() {
        StringBuilder sb = new StringBuilder();
        sb.append(nazwa);

        if (kategoria != null) {
            sb.append(" (");
            for (int i = 0; i < kategoria; i++) {
                sb.append("★");
            }
            sb.append(")");
        }

        sb.append(" - ").append(miasto);

        return sb.toString();
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNazwa() { return nazwa; }
    public void setNazwa(String nazwa) { this.nazwa = nazwa; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public String getMiasto() { return miasto; }
    public void setMiasto(String miasto) { this.miasto = miasto; }

    public String getKraj() { return kraj; }
    public void setKraj(String kraj) { this.kraj = kraj; }

    public Integer getKategoria() { return kategoria; }
    public void setKategoria(Integer kategoria) { this.kategoria = kategoria; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOsobaKontaktowa() { return osobaKontaktowa; }
    public void setOsobaKontaktowa(String osobaKontaktowa) { this.osobaKontaktowa = osobaKontaktowa; }

    public String getWarunkiWspolpracy() { return warunkiWspolpracy; }
    public void setWarunkiWspolpracy(String warunkiWspolpracy) { this.warunkiWspolpracy = warunkiWspolpracy; }

    public BigDecimal getOcenaJakosci() { return ocenaJakosci; }
    public void setOcenaJakosci(BigDecimal ocenaJakosci) { this.ocenaJakosci = ocenaJakosci; }

    public String getStatusWspolpracy() { return statusWspolpracy; }
    public void setStatusWspolpracy(String statusWspolpracy) { this.statusWspolpracy = statusWspolpracy; }

    public LocalDate getDataRozpoczeciaWspolpracy() { return dataRozpoczeciaWspolpracy; }
    public void setDataRozpoczeciaWspolpracy(LocalDate dataRozpoczeciaWspolpracy) {
        this.dataRozpoczeciaWspolpracy = dataRozpoczeciaWspolpracy;
    }

    public List<Wycieczka> getWycieczki() { return wycieczki; }
    public void setWycieczki(List<Wycieczka> wycieczki) { this.wycieczki = wycieczki; }

    public List<OcenaHotelu> getOceny() { return oceny; }
    public void setOceny(List<OcenaHotelu> oceny) { this.oceny = oceny; }

    @Override
    public String toString() {
        return generujPełnaNazwe() + " (Ocena: " + ocenaJakosci + "/10)";
    }
}

@Entity
@Table(name = "ocena_hotelu")
class OcenaHotelu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private Integer ocena; // 1-10

    @Column(columnDefinition = "TEXT")
    private String komentarz;

    @Column(name = "data_oceny")
    private LocalDate dataOceny;

    @Column(name = "autor_oceny", length = 100)
    private String autorOceny;

    public OcenaHotelu() {}

    public OcenaHotelu(Hotel hotel, Integer ocena, String komentarz) {
        this.hotel = hotel;
        this.ocena = ocena;
        this.komentarz = komentarz;
        this.dataOceny = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public Integer getOcena() { return ocena; }
    public void setOcena(Integer ocena) { this.ocena = ocena; }

    public String getKomentarz() { return komentarz; }
    public void setKomentarz(String komentarz) { this.komentarz = komentarz; }

    public LocalDate getDataOceny() { return dataOceny; }
    public void setDataOceny(LocalDate dataOceny) { this.dataOceny = dataOceny; }

    public String getAutorOceny() { return autorOceny; }
    public void setAutorOceny(String autorOceny) { this.autorOceny = autorOceny; }
}