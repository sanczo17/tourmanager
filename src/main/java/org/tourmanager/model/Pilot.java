package org.tourmanager.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pilot")
public class Pilot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String imie;

    @Column(nullable = false, length = 50)
    private String nazwisko;

    @Column(length = 20)
    private String telefon;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String adres;

    @Column(name = "data_urodzenia")
    private LocalDate dataUrodzenia;

    @Column(name = "data_zatrudnienia")
    private LocalDate dataZatrudnienia;

    @Column(name = "numer_licencji", length = 50)
    private String numerLicencji;

    @Column(name = "data_waznosci_licencji")
    private LocalDate dataWaznosciLicencji;

    @Column(name = "znajomosc_jezykow", length = 200)
    private String znajomoscJezykow; // JSON format: ["Polski", "Angielski", "Niemiecki"]

    @Column(length = 200)
    private String specjalizacje; // JSON format: ["Chorwacja", "Grecja"]

    @Column(name = "dostepnosc_od")
    private LocalDate dostepnoscOd;

    @Column(name = "dostepnosc_do")
    private LocalDate dostepnoscDo;

    @Column(name = "status_pilota", length = 30)
    private String statusPilota;

    @OneToMany(mappedBy = "pilot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wycieczka> wycieczki = new ArrayList<>();


    public Pilot() {}

    public Pilot(String imie, String nazwisko, String telefon, String email, String numerLicencji) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.telefon = telefon;
        this.email = email;
        this.numerLicencji = numerLicencji;
        this.dataZatrudnienia = LocalDate.now();
        this.statusPilota = "AKTYWNY";
        this.znajomoscJezykow = "[\"Polski\"]"; // Domyślnie polski
    }


    /**
     * METODA 13: Waliduje i rejestruje nowego pilota w systemie
     * @return true jeśli pilot został pomyślnie zarejestrowany
     */
    public boolean dodajPilota() {
        try {
            if (imie == null || imie.trim().isEmpty()) {
                throw new IllegalArgumentException("Imię pilota nie może być puste");
            }

            if (nazwisko == null || nazwisko.trim().isEmpty()) {
                throw new IllegalArgumentException("Nazwisko pilota nie może być puste");
            }

            if (telefon == null || telefon.trim().isEmpty()) {
                throw new IllegalArgumentException("Numer telefonu jest wymagany");
            }

            if (email == null || !email.contains("@")) {
                throw new IllegalArgumentException("Poprawny adres email jest wymagany");
            }

            if (numerLicencji == null || numerLicencji.trim().isEmpty()) {
                throw new IllegalArgumentException("Numer licencji pilota jest wymagany");
            }

            if (dataWaznosciLicencji == null || dataWaznosciLicencji.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Data ważności licencji musi być z przyszłości");
            }

            this.dataZatrudnienia = LocalDate.now();
            this.statusPilota = "AKTYWNY";

            if (znajomoscJezykow == null || znajomoscJezykow.trim().isEmpty()) {
                this.znajomoscJezykow = "[\"Polski\"]";
            }

            System.out.println("Pilot " + imie + " " + nazwisko + " został pomyślnie zarejestrowany");
            System.out.println("Licencja: " + numerLicencji + " (ważna do: " + dataWaznosciLicencji + ")");

            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd walidacji pilota: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas rejestracji pilota: " + e.getMessage());
            return false;
        }
    }

    /**
     * METODA 14: Sprawdza czy pilot jest dostępny w określonym okresie
     * @param dataOd początek okresu
     * @param dataDo koniec okresu
     * @return true jeśli pilot jest dostępny
     */
    public boolean sprawdzDostepnosc(LocalDate dataOd, LocalDate dataDo) {
        try {
            if (dataOd == null || dataDo == null) {
                throw new IllegalArgumentException("Daty sprawdzania dostępności nie mogą być null");
            }

            if (dataOd.isAfter(dataDo)) {
                throw new IllegalArgumentException("Data początkowa nie może być późniejsza niż końcowa");
            }

            if (!"AKTYWNY".equals(statusPilota)) {
                System.out.println("Pilot " + imie + " " + nazwisko + " nie jest aktywny");
                return false;
            }

            if (dataWaznosciLicencji != null && dataWaznosciLicencji.isBefore(dataOd)) {
                System.out.println("Licencja pilota " + imie + " " + nazwisko + " wygaśnie przed terminem wycieczki");
                return false;
            }

            if (dostepnoscOd != null && dostepnoscDo != null) {
                if (dataOd.isBefore(dostepnoscOd) || dataDo.isAfter(dostepnoscDo)) {
                    System.out.println("Pilot " + imie + " " + nazwisko + " nie jest dostępny w podanym okresie");
                    return false;
                }
            }

            if (wycieczki != null) {
                for (Wycieczka wycieczka : wycieczki) {
                    if ("PLANOWANA".equals(wycieczka.getStatusWycieczki()) ||
                            "W_TRAKCIE".equals(wycieczka.getStatusWycieczki())) {

                        LocalDate wycRozpoczecie = wycieczka.getDataRozpoczecia();
                        LocalDate wycZakonczenie = wycieczka.getDataZakonczenia();

                        if (wycRozpoczecie != null && wycZakonczenie != null) {
                            if (!(dataDo.isBefore(wycRozpoczecie) || dataOd.isAfter(wycZakonczenie))) {
                                System.out.println("Pilot " + imie + " " + nazwisko + " ma konflikt terminów z inną wycieczką");
                                return false;
                            }
                        }
                    }
                }
            }

            System.out.println("Pilot " + imie + " " + nazwisko + " jest dostępny w okresie " +
                    dataOd + " - " + dataDo);
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd parametru: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas sprawdzania dostępności: " + e.getMessage());
            return false;
        }
    }

    /**
     * METODA 15: Sprawdza ważność licencji i wysyła przypomnienia
     * @return true jeśli licencja jest ważna
     */
    public boolean sprawdzWaznoscLicencji() {
        try {
            if (dataWaznosciLicencji == null) {
                System.err.println("Brak daty ważności licencji dla pilota: " + imie + " " + nazwisko);
                return false;
            }

            LocalDate dzisiaj = LocalDate.now();
            long dniDoWygasniecia = dzisiaj.until(dataWaznosciLicencji).getDays();

            if (dniDoWygasniecia < 0) {
                System.err.println("UWAGA: Licencja pilota " + imie + " " + nazwisko +
                        " wygasła " + Math.abs(dniDoWygasniecia) + " dni temu!");
                this.statusPilota = "NIEAKTYWNY_LICENCJA";
                return false;
            } else if (dniDoWygasniecia <= 30) {
                System.out.println("PRZYPOMNIENIE: Licencja pilota " + imie + " " + nazwisko +
                        " wygaśnie za " + dniDoWygasniecia + " dni");
                return true;
            } else {
                System.out.println("Licencja pilota " + imie + " " + nazwisko + " jest ważna do " + dataWaznosciLicencji);
                return true;
            }

        } catch (Exception e) {
            System.err.println("Błąd podczas sprawdzania ważności licencji: " + e.getMessage());
            return false;
        }
    }

    /**
     * METODA 16: Przydziela pilota do konkretnej wycieczki
     * @param wycieczka wycieczka do przypisania
     * @return true jeśli przypisanie się powiodło
     */
    public boolean przydzielDoWycieczki(Wycieczka wycieczka) {
        try {
            if (wycieczka == null) {
                throw new IllegalArgumentException("Wycieczka nie może być null");
            }

            if (!sprawdzDostepnosc(wycieczka.getDataRozpoczecia(), wycieczka.getDataZakonczenia())) {
                return false;
            }

            String wymaganyJezyk = pobierzWymaganyJezyk(wycieczka);
            if (wymaganyJezyk != null && !sprawdzZnajomoscJezyka(wymaganyJezyk)) {
                System.err.println("Pilot " + imie + " " + nazwisko +
                        " nie zna wymaganego języka: " + wymaganyJezyk);
                return false;
            }

            String wymaganaSpecjalizacja = pobierzWymaganaSpecjalizacje(wycieczka);
            if (wymaganaSpecjalizacja != null && !sprawdzSpecjalizacje(wymaganaSpecjalizacja)) {
                System.out.println("Uwaga: Pilot " + imie + " " + nazwisko +
                        " nie ma specjalizacji: " + wymaganaSpecjalizacja);
            }

            wycieczka.setPilot(this);
            this.wycieczki.add(wycieczka);

            System.out.println("Pilot " + imie + " " + nazwisko +
                    " został przydzielony do wycieczki: " + wycieczka.getId());

            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Błąd podczas przydzielania pilota: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Nieoczekiwany błąd podczas przydzielania pilota: " + e.getMessage());
            return false;
        }
    }


    /**
     * Sprawdza czy pilot zna określony język
     */
    public boolean sprawdzZnajomoscJezyka(String jezyk) {
        if (znajomoscJezykow == null || jezyk == null) {
            return false;
        }

        return znajomoscJezykow.toLowerCase().contains(jezyk.toLowerCase());
    }

    /**
     * Sprawdza czy pilot ma określoną specjalizację
     */
    public boolean sprawdzSpecjalizacje(String specjalizacja) {
        if (specjalizacje == null || specjalizacja == null) {
            return false;
        }

        return specjalizacje.toLowerCase().contains(specjalizacja.toLowerCase());
    }

    /**
     * Dodaje nowy język do listy znanych języków
     */
    public boolean dodajJezyk(String nowyJezyk) {
        try {
            if (nowyJezyk == null || nowyJezyk.trim().isEmpty()) {
                throw new IllegalArgumentException("Nazwa języka nie może być pusta");
            }

            if (sprawdzZnajomoscJezyka(nowyJezyk)) {
                System.out.println("Pilot już zna język: " + nowyJezyk);
                return true;
            }

            if (znajomoscJezykow == null || znajomoscJezykow.isEmpty()) {
                znajomoscJezykow = "[\"" + nowyJezyk + "\"]";
            } else {
                znajomoscJezykow = znajomoscJezykow.substring(0, znajomoscJezykow.length() - 1) +
                        ", \"" + nowyJezyk + "\"]";
            }

            System.out.println("Dodano język " + nowyJezyk + " dla pilota " + imie + " " + nazwisko);
            return true;

        } catch (Exception e) {
            System.err.println("Błąd podczas dodawania języka: " + e.getMessage());
            return false;
        }
    }

    /**
     * Dodaje nową specjalizację
     */
    public boolean dodajSpecjalizacje(String nowaSpecjalizacja) {
        try {
            if (nowaSpecjalizacja == null || nowaSpecjalizacja.trim().isEmpty()) {
                throw new IllegalArgumentException("Nazwa specjalizacji nie może być pusta");
            }

            if (sprawdzSpecjalizacje(nowaSpecjalizacja)) {
                System.out.println("Pilot już ma specjalizację: " + nowaSpecjalizacja);
                return true;
            }

            if (specjalizacje == null || specjalizacje.isEmpty()) {
                specjalizacje = "[\"" + nowaSpecjalizacja + "\"]";
            } else {
                specjalizacje = specjalizacje.substring(0, specjalizacje.length() - 1) +
                        ", \"" + nowaSpecjalizacja + "\"]";
            }

            System.out.println("Dodano specjalizację " + nowaSpecjalizacja + " dla pilota " + imie + " " + nazwisko);
            return true;

        } catch (Exception e) {
            System.err.println("Błąd podczas dodawania specjalizacji: " + e.getMessage());
            return false;
        }
    }

    /**
     * Pobiera wymagany język na podstawie destynacji wycieczki
     */
    private String pobierzWymaganyJezyk(Wycieczka wycieczka) {
        if (wycieczka.getOferta() != null) {
            String kraj = wycieczka.getOferta().getKrajDocelowy();
            if (kraj != null) {
                switch (kraj.toLowerCase()) {
                    case "niemcy": return "Niemiecki";
                    case "francja": return "Francuski";
                    case "hiszpania": return "Hiszpański";
                    case "włochy": return "Włoski";
                    case "grecja": return "Grecki";
                    case "chorwacja": return "Chorwacki";
                    default: return "Angielski"; // Domyślny język międzynarodowy
                }
            }
        }
        return null;
    }

    /**
     * Pobiera wymaganą specjalizację na podstawie destynacji wycieczki
     */
    private String pobierzWymaganaSpecjalizacje(Wycieczka wycieczka) {
        if (wycieczka.getOferta() != null) {
            return wycieczka.getOferta().getKrajDocelowy();
        }
        return null;
    }

    /**
     * Oblicza liczbę wycieczek przeprowadzonych w tym roku
     */
    public long obliczLiczbeWycieczekWTymRoku() {
        int aktualnyRok = LocalDate.now().getYear();

        return wycieczki.stream()
                .filter(w -> "ZAKONCZONA".equals(w.getStatusWycieczki()))
                .filter(w -> w.getDataRozpoczecia().getYear() == aktualnyRok)
                .count();
    }

    /**
     * Sprawdza czy pilot jest doświadczony (więcej niż 20 wycieczek)
     */
    public boolean sprawdzCzyDoswiadczony() {
        long liczbaWycieczek = wycieczki.stream()
                .filter(w -> "ZAKONCZONA".equals(w.getStatusWycieczki()))
                .count();

        return liczbaWycieczek >= 20;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImie() { return imie; }
    public void setImie(String imie) { this.imie = imie; }

    public String getNazwisko() { return nazwisko; }
    public void setNazwisko(String nazwisko) { this.nazwisko = nazwisko; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }

    public LocalDate getDataUrodzenia() { return dataUrodzenia; }
    public void setDataUrodzenia(LocalDate dataUrodzenia) { this.dataUrodzenia = dataUrodzenia; }

    public LocalDate getDataZatrudnienia() { return dataZatrudnienia; }
    public void setDataZatrudnienia(LocalDate dataZatrudnienia) { this.dataZatrudnienia = dataZatrudnienia; }

    public String getNumerLicencji() { return numerLicencji; }
    public void setNumerLicencji(String numerLicencji) { this.numerLicencji = numerLicencji; }

    public LocalDate getDataWaznosciLicencji() { return dataWaznosciLicencji; }
    public void setDataWaznosciLicencji(LocalDate dataWaznosciLicencji) {
        this.dataWaznosciLicencji = dataWaznosciLicencji;
    }

    public String getZnajomoscJezykow() { return znajomoscJezykow; }
    public void setZnajomoscJezykow(String znajomoscJezykow) { this.znajomoscJezykow = znajomoscJezykow; }

    public String getSpecjalizacje() { return specjalizacje; }
    public void setSpecjalizacje(String specjalizacje) { this.specjalizacje = specjalizacje; }

    public LocalDate getDostepnoscOd() { return dostepnoscOd; }
    public void setDostepnoscOd(LocalDate dostepnoscOd) { this.dostepnoscOd = dostepnoscOd; }

    public LocalDate getDostepnoscDo() { return dostepnoscDo; }
    public void setDostepnoscDo(LocalDate dostepnoscDo) { this.dostepnoscDo = dostepnoscDo; }

    public String getStatusPilota() { return statusPilota; }
    public void setStatusPilota(String statusPilota) { this.statusPilota = statusPilota; }

    public List<Wycieczka> getWycieczki() { return wycieczki; }
    public void setWycieczki(List<Wycieczka> wycieczki) { this.wycieczki = wycieczki; }

    @Override
    public String toString() {
        return imie + " " + nazwisko + " (Lic: " + numerLicencji + ")";
    }
}