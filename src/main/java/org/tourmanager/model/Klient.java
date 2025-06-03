package org.tourmanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "klient")
public class Klient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String imie;

    @Column(nullable = false, length = 50)
    private String nazwisko;

    @Column(length = 11, unique = true)
    private String pesel;

    @Column(name = "numer_dowodu", length = 20)
    private String numerDowodu;

    @Column(length = 20)
    private String telefon;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String adres;

    @Column(name = "data_urodzenia")
    private LocalDate dataUrodzenia;

    @Column(name = "data_rejestracji")
    private LocalDate dataRejestracji;

    @Column(name = "status_klienta", length = 30)
    private String statusKlienta;

    @OneToMany(mappedBy = "klient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Umowa> umowy = new ArrayList<>();

    public Klient() {}

    public Klient(String imie, String nazwisko, String pesel, String telefon, String email) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.pesel = pesel;
        this.telefon = telefon;
        this.email = email;
        this.dataRejestracji = LocalDate.now();
        this.statusKlienta = "AKTYWNY";
    }

    public boolean dodajKlienta() {
        try {
            if (imie == null || imie.trim().isEmpty()) {
                throw new IllegalArgumentException("Imię klienta nie może być puste");
            }
            if (nazwisko == null || nazwisko.trim().isEmpty()) {
                throw new IllegalArgumentException("Nazwisko klienta nie może być puste");
            }
            if (pesel != null && !walidujPesel()) {
                throw new IllegalArgumentException("Niepoprawny numer PESEL");
            }
            if (telefon == null || telefon.trim().isEmpty()) {
                throw new IllegalArgumentException("Numer telefonu jest wymagany");
            }
            if (email != null && !email.contains("@")) {
                throw new IllegalArgumentException("Niepoprawny adres email");
            }

            this.dataRejestracji = LocalDate.now();
            this.statusKlienta = "AKTYWNY";

            System.out.println("Klient " + imie + " " + nazwisko + " został pomyślnie zarejestrowany");
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd walidacji klienta: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas rejestracji klienta: " + e.getMessage());
            return false;
        }
    }

    public boolean walidujPesel() {
        if (pesel == null || pesel.length() != 11) {
            return false;
        }

        try {
            Long.parseLong(pesel);

            int[] wagi = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
            int suma = 0;

            for (int i = 0; i < 10; i++) {
                suma += Character.getNumericValue(pesel.charAt(i)) * wagi[i];
            }

            int cyfraKontrolna = (10 - (suma % 10)) % 10;
            int ostatniaCyfra = Character.getNumericValue(pesel.charAt(10));

            return cyfraKontrolna == ostatniaCyfra;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    public List<Umowa> sprawdzHistorieWyjazdow() {
        return umowy;
    }

    /**
     * Oblicza łączną wartość wszystkich umów klienta
     */
    public BigDecimal obliczLacznaWartoscUmow() {
        return umowy.stream()
                .filter(umowa -> !"ANULOWANA".equals(umowa.getStatusUmowy()))
                .map(Umowa::getCenaCalkowita)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Sprawdza czy klient jest VIP (więcej niż 5 wyjazdów)
     */
    public boolean sprawdzCzyVIP() {
        long liczbaWyjazdow = umowy.stream()
                .filter(umowa -> "OPLACONA".equals(umowa.getStatusUmowy()) ||
                        "ZREALIZOWANA".equals(umowa.getStatusUmowy()))
                .count();
        return liczbaWyjazdow >= 5;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public String getPesel() { return pesel; }
    public void setPesel(String pesel) { this.pesel = pesel; }

    public String getNumerDowodu() { return numerDowodu; }
    public void setNumerDowodu(String numerDowodu) { this.numerDowodu = numerDowodu; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public LocalDate getDataUrodzenia() { return dataUrodzenia; }
    public void setDataUrodzenia(LocalDate dataUrodzenia) { this.dataUrodzenia = dataUrodzenia; }

    public LocalDate getDataRejestracji() { return dataRejestracji; }
    public void setDataRejestracji(LocalDate dataRejestracji) { this.dataRejestracji = dataRejestracji; }

    public String getStatusKlienta() { return statusKlienta; }
    public void setStatusKlienta(String statusKlienta) { this.statusKlienta = statusKlienta; }

    public List<Umowa> getUmowy() { return umowy; }
    public void setUmowy(List<Umowa> umowy) { this.umowy = umowy; }

    @Override
    public String toString() {
        return imie + " " + nazwisko + " (" + telefon + ")";
    }
}