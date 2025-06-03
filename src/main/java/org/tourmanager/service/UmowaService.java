package org.tourmanager.service;

import org.tourmanager.model.*;
import org.tourmanager.repository.UmowaRepository;
import org.tourmanager.repository.KlientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class UmowaService {

    @Autowired
    private UmowaRepository umowaRepository;

    @Autowired
    private KlientRepository klientRepository;

    /**
     * Zawiera nową umowę z klientem
     */
    public boolean zawrzyjUmowe(Umowa umowa) {
        try {
            if (umowa == null) {
                throw new IllegalArgumentException("Umowa nie może być null");
            }

            if (umowa.utworzUmowe()) {
                umowaRepository.save(umowa);
                System.out.println("Umowa zapisana w bazie danych: " + umowa.getNrUmowy());
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Błąd walidacji umowy: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas zapisywania umowy: " + e.getMessage());
            return false;
        }
    }

    public boolean zarejestrujZaliczke(String nrUmowy, BigDecimal kwota, String metodaPlatnosci) {
        try {
            if (nrUmowy == null || nrUmowy.trim().isEmpty()) {
                throw new IllegalArgumentException("Numer umowy nie może być pusty");
            }

            if (kwota == null || kwota.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Kwota musi być większa od zera");
            }

            if (metodaPlatnosci == null || metodaPlatnosci.trim().isEmpty()) {
                throw new IllegalArgumentException("Metoda płatności nie może być pusta");
            }

            Umowa umowa = umowaRepository.findByNrUmowy(nrUmowy);
            if (umowa != null) {
                if (umowa.zarejestrujZaliczke(kwota, metodaPlatnosci)) {
                    umowaRepository.save(umowa);
                    return true;
                }
            } else {
                System.err.println("Nie znaleziono umowy o numerze: " + nrUmowy);
            }
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Błąd walidacji: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas rejestracji zaliczki: " + e.getMessage());
            return false;
        }
    }

    /**
     * Anuluje umowę
     */
    public boolean anulujUmowe(String nrUmowy, String powodRezygnacji) {
        try {
            Umowa umowa = umowaRepository.findByNrUmowy(nrUmowy);
            if (umowa != null) {
                if (umowa.anulujUmowe(powodRezygnacji)) {
                    umowaRepository.save(umowa);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas anulowania umowy: " + e.getMessage());
            return false;
        }
    }

    /**
     * Wyszukuje umowy klienta
     */
    public List<Umowa> wyszukajUmowyKlienta(String pesel) {
        try {
            Klient klient = klientRepository.findByPesel(pesel);
            if (klient != null) {
                return klient.sprawdzHistorieWyjazdow();
            }
            return List.of();
        } catch (Exception e) {
            System.err.println("Błąd podczas wyszukiwania umów: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Pobiera umowy wymagające dopłaty
     */
    public List<Umowa> pobierzUmowyDoDoplaty() {
        return umowaRepository.findByStatusUmowy("ZALICZKA_WPLACONA");
    }
}