package org.tourmanager.service;

import org.tourmanager.model.*;
import org.tourmanager.repository.PilotRepository;
import org.tourmanager.repository.WycieczkaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PilotService {

    @Autowired
    private PilotRepository pilotRepository;

    @Autowired
    private WycieczkaRepository wycieczkaRepository;

    // ================================================================
    // PODSTAWOWE OPERACJE CRUD
    // ================================================================

    /**
     * Dodaje nowego pilota do systemu
     */
    public boolean dodajPilota(Pilot pilot) {
        try {
            if (pilot.dodajPilota()) {
                pilotRepository.save(pilot);
                System.out.println("Pilot zapisany w bazie danych: " + pilot.getImie() + " " + pilot.getNazwisko());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas zapisywania pilota: " + e.getMessage());
            return false;
        }
    }

    /**
     * Aktualizuje dane pilota
     */
    public boolean aktualizujPilota(Pilot pilot) {
        try {
            if (pilot.getId() != null && pilotRepository.existsById(pilot.getId())) {
                pilotRepository.save(pilot);
                System.out.println("Dane pilota zaktualizowane: " + pilot.getImie() + " " + pilot.getNazwisko());
                return true;
            } else {
                System.err.println("Pilot o ID " + pilot.getId() + " nie istnieje");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas aktualizacji pilota: " + e.getMessage());
            return false;
        }
    }

    /**
     * Usuwa pilota z systemu (tylko jeśli nie ma przypisanych wycieczek)
     */
    public boolean usunPilota(Long pilotId) {
        try {
            Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
            if (pilotOpt.isPresent()) {
                Pilot pilot = pilotOpt.get();

                boolean maAktywneWycieczki = pilot.getWycieczki().stream()
                        .anyMatch(w -> "PLANOWANA".equals(w.getStatusWycieczki()) ||
                                "W_TRAKCIE".equals(w.getStatusWycieczki()));

                if (maAktywneWycieczki) {
                    System.err.println("Nie można usunąć pilota - ma przypisane aktywne wycieczki");
                    return false;
                }

                pilot.setStatusPilota("NIEAKTYWNY");
                pilotRepository.save(pilot);

                System.out.println("Pilot " + pilot.getImie() + " " + pilot.getNazwisko() + " został dezaktywowany");
                return true;
            } else {
                System.err.println("Pilot o ID " + pilotId + " nie istnieje");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas usuwania pilota: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // POBIERANIE DANYCH
    // ================================================================

    /**
     * Pobiera pilota według ID
     */
    public Pilot pobierzPilota(Long pilotId) {
        return pilotRepository.findById(pilotId).orElse(null);
    }

    /**
     * Pobiera wszystkich aktywnych pilotów
     */
    public List<Pilot> pobierzAktywnychPilotow() {
        return pilotRepository.findAll().stream()
                .filter(pilot -> "AKTYWNY".equals(pilot.getStatusPilota()))
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wszystkich pilotów
     */
    public List<Pilot> pobierzWszystkichPilotow() {
        return pilotRepository.findAll();
    }

    // ================================================================
    // WYSZUKIWANIE I FILTROWANIE
    // ================================================================

    /**
     * Wyszukuje dostępnych pilotów w określonym okresie
     */
    public List<Pilot> wyszukajDostepnychPilotow(LocalDate dataOd, LocalDate dataDo) {
        List<Pilot> aktywniPiloci = pobierzAktywnychPilotow();

        return aktywniPiloci.stream()
                .filter(pilot -> pilot.sprawdzDostepnosc(dataOd, dataDo))
                .collect(Collectors.toList());
    }

    /**
     * Wyszukuje pilotów znających określony język
     */
    public List<Pilot> wyszukajPilotowZnajacychJezyk(String jezyk) {
        return pilotRepository.findByZnajomoscJezyka(jezyk);
    }

    /**
     * Wyszukuje pilotów według specjalizacji
     */
    public List<Pilot> wyszukajPilotowWgSpecjalizacji(String specjalizacja) {
        return pilotRepository.findBySpecjalizacja(specjalizacja);
    }

    /**
     * Wyszukuje pilotów według imienia i nazwiska
     */
    public List<Pilot> wyszukajPilotowWgImienia(String imie, String nazwisko) {
        return pilotRepository.findByImieAndNazwisko(imie, nazwisko);
    }

    /**
     * Wyszukuje pilota według numeru licencji
     */
    public Pilot wyszukajPilotaWgLicencji(String numerLicencji) {
        return pilotRepository.findByNumerLicencji(numerLicencji);
    }

    /**
     * Wyszukuje doświadczonych pilotów (z określonym stażem)
     */
    public List<Pilot> wyszukajDoswiadczonychPilotow(int minLiczbaMiesiecy) {
        LocalDate dataGraniczna = LocalDate.now().minusMonths(minLiczbaMiesiecy);
        return pilotRepository.findDoswiadczonychPilotow(dataGraniczna);
    }

    // ================================================================
    // ZARZĄDZANIE DOSTĘPNOŚCIĄ
    // ================================================================

    /**
     * Ustala dostępność pilota w określonym okresie
     */
    public boolean ustawDostepnosc(Long pilotId, LocalDate dostepnoscOd, LocalDate dostepnoscDo) {
        try {
            Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
            if (pilotOpt.isPresent()) {
                Pilot pilot = pilotOpt.get();
                pilot.setDostepnoscOd(dostepnoscOd);
                pilot.setDostepnoscDo(dostepnoscDo);
                pilotRepository.save(pilot);

                System.out.println("Ustawiono dostępność pilota " + pilot.getImie() + " " + pilot.getNazwisko() +
                        " od " + dostepnoscOd + " do " + dostepnoscDo);
                return true;
            } else {
                System.err.println("Pilot o ID " + pilotId + " nie istnieje");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas ustawiania dostępności: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sprawdza dostępność pilota w określonym okresie
     */
    public boolean sprawdzDostepnoscPilota(Long pilotId, LocalDate dataOd, LocalDate dataDo) {
        Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
        if (pilotOpt.isPresent()) {
            return pilotOpt.get().sprawdzDostepnosc(dataOd, dataDo);
        }
        return false;
    }

    // ================================================================
    // ZARZĄDZANIE KOMPETENCJAMI
    // ================================================================

    /**
     * Dodaje język do listy znanych języków pilota
     */
    public boolean dodajJezykPilota(Long pilotId, String jezyk) {
        try {
            Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
            if (pilotOpt.isPresent()) {
                Pilot pilot = pilotOpt.get();
                if (pilot.dodajJezyk(jezyk)) {
                    pilotRepository.save(pilot);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas dodawania języka: " + e.getMessage());
            return false;
        }
    }

    /**
     * Dodaje specjalizację pilota
     */
    public boolean dodajSpecjalizacjePilota(Long pilotId, String specjalizacja) {
        try {
            Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
            if (pilotOpt.isPresent()) {
                Pilot pilot = pilotOpt.get();
                if (pilot.dodajSpecjalizacje(specjalizacja)) {
                    pilotRepository.save(pilot);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas dodawania specjalizacji: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // ZARZĄDZANIE LICENCJAMI
    // ================================================================

    /**
     * Sprawdza ważność licencji wszystkich pilotów
     */
    public void sprawdzWaznoscLicencjiWszystkichPilotow() {
        List<Pilot> piloci = pilotRepository.findAll();

        for (Pilot pilot : piloci) {
            pilot.sprawdzWaznoscLicencji();
            pilotRepository.save(pilot);
        }

        System.out.println("Sprawdzono ważność licencji " + piloci.size() + " pilotów");
    }

    /**
     * Pobiera pilotów z wygasającymi licencjami (w ciągu 30 dni)
     */
    public List<Pilot> pobierzPilotowZWygasajacymiLicencjami() {
        LocalDate dataGraniczna = LocalDate.now().plusDays(30);
        return pilotRepository.findPilotowZWygasajacymiLicencjami(dataGraniczna);
    }

    /**
     * Aktualizuje datę ważności licencji pilota
     */
    public boolean aktualizujLicencjePilota(Long pilotId, LocalDate nowaDataWaznosci) {
        try {
            Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
            if (pilotOpt.isPresent()) {
                Pilot pilot = pilotOpt.get();
                pilot.setDataWaznosciLicencji(nowaDataWaznosci);

                // Jeśli licencja została odnowiona, aktywuj pilota
                if (nowaDataWaznosci.isAfter(LocalDate.now()) &&
                        "NIEAKTYWNY_LICENCJA".equals(pilot.getStatusPilota())) {
                    pilot.setStatusPilota("AKTYWNY");
                }

                pilotRepository.save(pilot);
                System.out.println("Zaktualizowano licencję pilota " + pilot.getImie() + " " + pilot.getNazwisko());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas aktualizacji licencji: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // PRZYDZIELANIE DO WYCIECZEK
    // ================================================================

    /**
     * Przydziela pilota do wycieczki
     */
    public boolean przydzielPilotaDoWycieczki(Long pilotId, Long wycieczkaId) {
        try {
            Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
            Optional<Wycieczka> wycieczkaOpt = wycieczkaRepository.findById(wycieczkaId);

            if (pilotOpt.isPresent() && wycieczkaOpt.isPresent()) {
                Pilot pilot = pilotOpt.get();
                Wycieczka wycieczka = wycieczkaOpt.get();

                if (pilot.przydzielDoWycieczki(wycieczka)) {
                    pilotRepository.save(pilot);
                    wycieczkaRepository.save(wycieczka);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas przydzielania pilota: " + e.getMessage());
            return false;
        }
    }

    /**
     * Automatycznie dobiera najlepszego pilota do wycieczki
     */
    public Pilot dobierzNajlepszegoPilota(Wycieczka wycieczka) {
        try {
            List<Pilot> dostepniPiloci = wyszukajDostepnychPilotow(
                    wycieczka.getDataRozpoczecia(),
                    wycieczka.getDataZakonczenia()
            );
            String wymaganyJezyk = pobierzWymaganyJezyk(wycieczka);
            if (wymaganyJezyk != null) {
                dostepniPiloci = dostepniPiloci.stream()
                        .filter(pilot -> pilot.sprawdzZnajomoscJezyka(wymaganyJezyk))
                        .collect(Collectors.toList());
            }

            String wymaganaSpecjalizacja = pobierzWymaganaSpecjalizacje(wycieczka);
            if (wymaganaSpecjalizacja != null) {
                List<Pilot> pilociZeSpecjalizacja = dostepniPiloci.stream()
                        .filter(pilot -> pilot.sprawdzSpecjalizacje(wymaganaSpecjalizacja))
                        .collect(Collectors.toList());

                if (!pilociZeSpecjalizacja.isEmpty()) {
                    dostepniPiloci = pilociZeSpecjalizacja;
                }
            }

            return dostepniPiloci.stream()
                    .filter(Pilot::sprawdzCzyDoswiadczony)
                    .max((p1, p2) -> Long.compare(
                            p1.obliczLiczbeWycieczekWTymRoku(),
                            p2.obliczLiczbeWycieczekWTymRoku()))
                    .orElse(dostepniPiloci.isEmpty() ? null : dostepniPiloci.get(0));

        } catch (Exception e) {
            System.err.println("Błąd podczas dobierania pilota: " + e.getMessage());
            return null;
        }
    }

    /**
     * Usuwa przypisanie pilota z wycieczki
     */
    public boolean usunPilotaZWycieczki(Long wycieczkaId) {
        try {
            Optional<Wycieczka> wycieczkaOpt = wycieczkaRepository.findById(wycieczkaId);
            if (wycieczkaOpt.isPresent()) {
                Wycieczka wycieczka = wycieczkaOpt.get();

                if (wycieczka.getPilot() != null) {
                    Pilot pilot = wycieczka.getPilot();
                    pilot.getWycieczki().remove(wycieczka);
                    wycieczka.setPilot(null);

                    pilotRepository.save(pilot);
                    wycieczkaRepository.save(wycieczka);

                    System.out.println("Usunięto przypisanie pilota z wycieczki " + wycieczkaId);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas usuwania przypisania pilota: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // RAPORTY I STATYSTYKI
    // ================================================================

    /**
     * Pobiera statystyki pilotów
     */
    public List<Object[]> pobierzStatystykiPilotow() {
        return pilotRepository.findStatystykiPilotow();
    }

    /**
     * Pobiera pilotów uporządkowanych według doświadczenia
     */
    public List<Pilot> pobierzRankingPilotow() {
        return pobierzAktywnychPilotow().stream()
                .sorted((p1, p2) -> Long.compare(
                        p2.obliczLiczbeWycieczekWTymRoku(),
                        p1.obliczLiczbeWycieczekWTymRoku()))
                .collect(Collectors.toList());
    }

    /**
     * Oblicza obciążenie pilotów w bieżącym roku
     */
    public String generujRaportObciazeniaPilotow() {
        List<Pilot> aktywniPiloci = pobierzAktywnychPilotow();
        StringBuilder raport = new StringBuilder();

        raport.append("RAPORT OBCIĄŻENIA PILOTÓW - ").append(LocalDate.now().getYear()).append("\n");
        raport.append("═══════════════════════════════════════════════════\n\n");

        for (Pilot pilot : aktywniPiloci) {
            long liczbaWycieczek = pilot.obliczLiczbeWycieczekWTymRoku();
            String status = liczbaWycieczek > 20 ? "WYSOKIE" :
                    liczbaWycieczek > 10 ? "ŚREDNIE" : "NISKIE";

            raport.append(String.format("%-20s %-15s - %2d wycieczek (%s)\n",
                    pilot.getImie() + " " + pilot.getNazwisko(),
                    pilot.getNumerLicencji(),
                    liczbaWycieczek,
                    status));
        }

        return raport.toString();
    }

    /**
     * Eksportuje dane pilotów do formatu CSV
     */
    public String eksportujPilotowDoCSV() {
        List<Pilot> piloci = pobierzWszystkichPilotow();
        StringBuilder csv = new StringBuilder();

        csv.append("ID,Imię,Nazwisko,Telefon,Email,Numer Licencji,Data Ważności,Status,Języki,Specjalizacje\n");

        for (Pilot pilot : piloci) {
            csv.append(pilot.getId()).append(",")
                    .append(escapeCsv(pilot.getImie())).append(",")
                    .append(escapeCsv(pilot.getNazwisko())).append(",")
                    .append(escapeCsv(pilot.getTelefon())).append(",")
                    .append(escapeCsv(pilot.getEmail())).append(",")
                    .append(escapeCsv(pilot.getNumerLicencji())).append(",")
                    .append(pilot.getDataWaznosciLicencji()).append(",")
                    .append(pilot.getStatusPilota()).append(",")
                    .append(escapeCsv(pilot.getZnajomoscJezykow())).append(",")
                    .append(escapeCsv(pilot.getSpecjalizacje()))
                    .append("\n");
        }

        return csv.toString();
    }

    /**
     * Aktywuje/dezaktywuje pilota
     */
    public boolean zmienStatusPilota(Long pilotId, String nowyStatus) {
        try {
            Optional<Pilot> pilotOpt = pilotRepository.findById(pilotId);
            if (pilotOpt.isPresent()) {
                Pilot pilot = pilotOpt.get();
                String staryStatus = pilot.getStatusPilota();
                pilot.setStatusPilota(nowyStatus);
                pilotRepository.save(pilot);

                System.out.println("Zmieniono status pilota " + pilot.getImie() + " " + pilot.getNazwisko() +
                        " z '" + staryStatus + "' na '" + nowyStatus + "'");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas zmiany statusu pilota: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // METODY POMOCNICZE
    // ================================================================

    /**
     * Pobiera wymagany język na podstawie destynacji wycieczki
     */
    private String pobierzWymaganyJezyk(Wycieczka wycieczka) {
        if (wycieczka.getOferta() != null) {
            String kraj = wycieczka.getOferta().getKrajDocelowy();
            if (kraj != null) {
                switch (kraj.toLowerCase()) {
                    case "niemcy":
                        return "Niemiecki";
                    case "francja":
                        return "Francuski";
                    case "hiszpania":
                        return "Hiszpański";
                    case "włochy":
                        return "Włoski";
                    case "grecja":
                        return "Grecki";
                    case "chorwacja":
                        return "Chorwacki";
                    case "czechy":
                        return "Czeski";
                    case "austria":
                        return "Niemiecki";
                    case "węgry":
                        return "Węgierski";
                    case "rumunia":
                        return "Rumuński";
                    case "bułgaria":
                        return "Bułgarski";
                    case "słowacja":
                        return "Słowacki";
                    case "słowenia":
                        return "Słoweński";
                    case "litwa":
                    case "łotwa":
                    case "estonia":
                        return "Angielski";
                    case "rosja":
                        return "Rosyjski";
                    case "ukraina":
                        return "Ukraiński";
                    case "turcja":
                        return "Turecki";
                    case "egipt":
                        return "Arabski";
                    case "izrael":
                        return "Hebrajski";
                    case "maroko":
                    case "tunezja":
                        return "Francuski";
                    case "portugalia":
                        return "Portugalski";
                    case "holandia":
                        return "Holenderski";
                    case "belgia":
                        return "Francuski";
                    case "szwajcaria":
                        return "Niemiecki";
                    case "dania":
                    case "szwecja":
                    case "norwegia":
                        return "Angielski";
                    case "finlandia":
                        return "Fiński";
                    case "islandia":
                        return "Angielski";
                    case "irlandia":
                    case "wielka brytania":
                    case "anglia":
                        return "Angielski";
                    case "malta":
                        return "Angielski";
                    case "cypr":
                        return "Grecki";
                    default:
                        return "Angielski"; // Domyślny język międzynarodowy
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
            String kraj = wycieczka.getOferta().getKrajDocelowy();
            String typ = wycieczka.getOferta().getTypWycieczki();

            if (kraj != null) {
                switch (kraj.toLowerCase()) {
                    case "chorwacja":
                    case "słowenia":
                    case "bośnia i hercegowina":
                    case "serbia":
                    case "czarnogóra":
                    case "macedonia":
                        return "Bałkany";

                    case "grecja":
                    case "cypr":
                    case "turcja":
                        return "Morze Egejskie";

                    case "hiszpania":
                    case "portugalia":
                        return "Półwysep Iberyjski";

                    case "włochy":
                    case "san marino":
                    case "watykan":
                        return "Włochy";

                    case "francja":
                    case "monako":
                        return "Francja";

                    case "niemcy":
                    case "austria":
                    case "szwajcaria":
                        return "Kraje niemieckojęzyczne";

                    case "czechy":
                    case "słowacja":
                    case "węgry":
                        return "Europa Środkowa";

                    case "dania":
                    case "szwecja":
                    case "norwegia":
                    case "finlandia":
                    case "islandia":
                        return "Skandynavia";

                    case "holandia":
                    case "belgia":
                    case "luksemburg":
                        return "Benelux";

                    case "litwa":
                    case "łotwa":
                    case "estonia":
                        return "Kraje Bałtyckie";

                    case "rosja":
                    case "białoruś":
                    case "ukraina":
                        return "Europa Wschodnia";

                    case "egipt":
                    case "maroko":
                    case "tunezja":
                    case "algieria":
                        return "Afryka Północna";

                    case "izrael":
                    case "jordania":
                        return "Bliski Wschód";

                    case "tajlandia":
                    case "wietnam":
                    case "kambodża":
                    case "laos":
                        return "Azja Południowo-Wschodnia";

                    case "indie":
                    case "nepal":
                    case "sri lanka":
                        return "Azja Południowa";

                    case "chiny":
                    case "japonia":
                    case "korea południowa":
                        return "Daleki Wschód";

                    case "usa":
                    case "kanada":
                        return "Ameryka Północna";

                    case "meksyk":
                    case "gwatemala":
                    case "kostaryka":
                        return "Ameryka Środkowa";

                    case "brazylia":
                    case "argentyna":
                    case "peru":
                    case "chile":
                        return "Ameryka Południowa";

                    case "australia":
                    case "nowa zelandia":
                        return "Oceania";

                    default:
                        return kraj;
                }
            }

            if (typ != null) {
                switch (typ.toLowerCase()) {
                    case "pielgrzymka":
                        return "Turystyka religijna";
                    case "objazdowa":
                        return "Wycieczki objazdowe";
                    case "wypoczynkowa":
                        return "Turystyka wypoczynkowa";
                    case "krajoznawcza":
                        return "Turystyka krajoznawcza";
                    case "górska":
                        return "Turystyka górska";
                    case "narciarska":
                        return "Turystyka narciarska";
                    case "rowerowa":
                        return "Turystyka rowerowa";
                    case "biznesowa":
                        return "Turystyka biznesowa";
                    default:
                        return typ;
                }
            }
        }
        return null;
    }

    /**
     * Waliduje czy pilot spełnia wymagania do prowadzenia wycieczki
     */
    public boolean walidujWymaganiaPilota(Pilot pilot, Wycieczka wycieczka) {
        try {
            if (!"AKTYWNY".equals(pilot.getStatusPilota())) {
                System.out.println("Pilot nie jest aktywny");
                return false;
            }

            if (!pilot.sprawdzWaznoscLicencji()) {
                System.out.println("Pilot ma nieważną licencję");
                return false;
            }

            if (!pilot.sprawdzDostepnosc(wycieczka.getDataRozpoczecia(), wycieczka.getDataZakonczenia())) {
                System.out.println("Pilot nie jest dostępny w wymaganym terminie");
                return false;
            }

            String wymaganyJezyk = pobierzWymaganyJezyk(wycieczka);
            if (wymaganyJezyk != null && !pilot.sprawdzZnajomoscJezyka(wymaganyJezyk)) {
                System.out.println("Pilot nie zna wymaganego języka: " + wymaganyJezyk);
                return false;
            }

            String wymaganaSpecjalizacja = pobierzWymaganaSpecjalizacje(wycieczka);
            if (wymaganaSpecjalizacja != null && !pilot.sprawdzSpecjalizacje(wymaganaSpecjalizacja)) {
                System.out.println("Uwaga: Pilot nie ma specjalizacji '" + wymaganaSpecjalizacja +
                        "' - może to wpłynąć na jakość obsługi");
            }

            return true;

        } catch (Exception e) {
            System.err.println("Błąd podczas walidacji wymagań pilota: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generuje listę rekomendowanych pilotów dla wycieczki
     */
    public List<Pilot> generujListeRekomendowanychPilotow(Wycieczka wycieczka, int maxLiczba) {
        try {
            List<Pilot> dostepniPiloci = wyszukajDostepnychPilotow(
                    wycieczka.getDataRozpoczecia(),
                    wycieczka.getDataZakonczenia()
            );

            return dostepniPiloci.stream()
                    .filter(pilot -> walidujWymaganiaPilota(pilot, wycieczka))
                    .sorted((p1, p2) -> {
                        int punkty1 = obliczPunktyPilota(p1, wycieczka);
                        int punkty2 = obliczPunktyPilota(p2, wycieczka);
                        return Integer.compare(punkty2, punkty1); // Sortowanie malejące
                    })
                    .limit(maxLiczba)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Błąd podczas generowania listy rekomendowanych pilotów: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Oblicza punkty pilota dla konkretnej wycieczki (system rekomendacji)
     */
    private int obliczPunktyPilota(Pilot pilot, Wycieczka wycieczka) {
        int punkty = 0;

        if (pilot.sprawdzCzyDoswiadczony()) {
            punkty += 20;
        }

        String wymaganyJezyk = pobierzWymaganyJezyk(wycieczka);
        if (wymaganyJezyk != null && pilot.sprawdzZnajomoscJezyka(wymaganyJezyk)) {
            punkty += 15;
        }

        String wymaganaSpecjalizacja = pobierzWymaganaSpecjalizacje(wycieczka);
        if (wymaganaSpecjalizacja != null && pilot.sprawdzSpecjalizacje(wymaganaSpecjalizacja)) {
            punkty += 10;
        }

        long wycieczkiWTymRoku = pilot.obliczLiczbeWycieczekWTymRoku();
        if (wycieczkiWTymRoku >= 10) {
            punkty += 5;
        } else if (wycieczkiWTymRoku >= 5) {
            punkty += 3;
        }

        if (wycieczkiWTymRoku > 25) {
            punkty -= 10;
        }

        return punkty;
    }

    /**
     * Metoda pomocnicza do escapowania wartości CSV
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}