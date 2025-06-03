package org.tourmanager.service;

import org.tourmanager.model.*;
import org.tourmanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode; // Dodaj ten import
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RaportService {

    @Autowired
    private UmowaRepository umowaRepository;

    @Autowired
    private OfertaTurystycznaRepository ofertaRepository;

    @Autowired
    private KlientRepository klientRepository;

    public String generujMiesieznyRaportSprzedazy(int rok, int miesiac) {
        try {
            YearMonth miesiacRoku = YearMonth.of(rok, miesiac);
            LocalDate pierwszyDzien = miesiacRoku.atDay(1);
            LocalDate ostatniDzien = miesiacRoku.atEndOfMonth();

            List<Umowa> umowyWMiesiacu = umowaRepository.findByDataZawarciaOkresu(pierwszyDzien, ostatniDzien);

            if (umowyWMiesiacu == null) {
                umowyWMiesiacu = new ArrayList<>();
            }

            StringBuilder raport = new StringBuilder();
            raport.append("═══════════════════════════════════════════════════════════════════════\n");
            raport.append("                        BIURO PODRÓŻY \"TRAVELER\"\n");
            raport.append("                      MIESIĘCZNY RAPORT SPRZEDAŻY\n");
            raport.append("                           ").append(miesiacRoku.toString().toUpperCase()).append("\n");
            raport.append("═══════════════════════════════════════════════════════════════════════\n\n");

            raport.append("Data wygenerowania: ").append(LocalDate.now()).append("\n");
            raport.append("Wygenerował: System TourManager\n\n");

            raport.append("───────────────────────────────────────────────────────────────────────\n");
            raport.append("                           PODSUMOWANIE OGÓLNE\n");
            raport.append("───────────────────────────────────────────────────────────────────────\n\n");

            int liczbaUmow = umowyWMiesiacu.size();
            BigDecimal calkowitaWartosc = obliczCalkowitaWartoscSprzedazy(umowyWMiesiacu);

            BigDecimal sredniaWartosc = liczbaUmow > 0 ?
                    calkowitaWartosc.divide(new BigDecimal(liczbaUmow), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            BigDecimal otrzymaneZaliczki = obliczOtrzymaneZaliczki(umowyWMiesiacu);
            BigDecimal pozostaleDoplaty = calkowitaWartosc.subtract(otrzymaneZaliczki);

            raport.append(String.format("Liczba zawartych umów: %d\n", liczbaUmow));
            raport.append(String.format("Całkowita wartość sprzedaży: %,.0f PLN\n", calkowitaWartosc));
            raport.append(String.format("Średnia wartość umowy: %,.0f PLN\n", sredniaWartosc));
            raport.append(String.format("Otrzymane zaliczki: %,.0f PLN\n", otrzymaneZaliczki));
            raport.append(String.format("Pozostałe dopłaty: %,.0f PLN\n\n", pozostaleDoplaty));

            return raport.toString();

        } catch (Exception e) {
            System.err.println("Błąd podczas generowania raportu: " + e.getMessage());
            return "Błąd podczas generowania raportu: " + e.getMessage() +
                    "\nSpróbuj ponownie lub skontaktuj się z administratorem.";
        }
    }

    public String generujRaportTopKlientow(int limit) {
        try {
            List<Klient> wszyscyKlienci = klientRepository.findAll();

            if (wszyscyKlienci == null) {
                wszyscyKlienci = new ArrayList<>();
            }

            List<TopKlient> topKlienci = wszyscyKlienci.stream()
                    .map(klient -> {
                        BigDecimal lacznaWartosc = klient.obliczLacznaWartoscUmow();
                        int liczbaWyjazdow = klient.getUmowy() != null ? klient.getUmowy().size() : 0;
                        return new TopKlient(klient, lacznaWartosc, liczbaWyjazdow);
                    })
                    .filter(tk -> tk.lacznaWartosc != null && tk.lacznaWartosc.compareTo(BigDecimal.ZERO) > 0)
                    .sorted((a, b) -> b.lacznaWartosc.compareTo(a.lacznaWartosc))
                    .limit(limit)
                    .collect(Collectors.toList());

            StringBuilder raport = new StringBuilder();
            raport.append("═══════════════════════════════════════════════════════════════════════\n");
            raport.append("                        TOP ").append(limit).append(" KLIENTÓW\n");
            raport.append("═══════════════════════════════════════════════════════════════════════\n\n");

            for (int i = 0; i < topKlienci.size(); i++) {
                TopKlient tk = topKlienci.get(i);
                raport.append(String.format("%d. %s %s - %d wyjazdów, wartość: %,.0f PLN\n",
                        i + 1,
                        tk.klient.getImie() != null ? tk.klient.getImie() : "BRAK",
                        tk.klient.getNazwisko() != null ? tk.klient.getNazwisko() : "BRAK",
                        tk.liczbaWyjazdow,
                        tk.lacznaWartosc));
            }

            return raport.toString();

        } catch (Exception e) {
            System.err.println("Błąd podczas generowania raportu TOP klientów: " + e.getMessage());
            return "Błąd podczas generowania raportu: " + e.getMessage();
        }
    }

    private BigDecimal obliczCalkowitaWartoscSprzedazy(List<Umowa> umowy) {
        if (umowy == null || umowy.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return umowy.stream()
                .filter(u -> u != null && !"ANULOWANA".equals(u.getStatusUmowy()))
                .map(Umowa::getCenaCalkowita)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal obliczOtrzymaneZaliczki(List<Umowa> umowy) {
        if (umowy == null || umowy.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return umowy.stream()
                .filter(u -> u != null && u.getDataWplatyZaliczki() != null)
                .map(Umowa::getZaliczka)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static class TopKlient {
        Klient klient;
        BigDecimal lacznaWartosc;
        int liczbaWyjazdow;

        TopKlient(Klient klient, BigDecimal lacznaWartosc, int liczbaWyjazdow) {
            this.klient = klient;
            this.lacznaWartosc = lacznaWartosc != null ? lacznaWartosc : BigDecimal.ZERO;
            this.liczbaWyjazdow = liczbaWyjazdow;
        }
    }
}