package org.tourmanager.service;

import org.tourmanager.model.*;
import org.tourmanager.repository.OfertaTurystycznaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OfertaService {

    @Autowired
    private OfertaTurystycznaRepository ofertaRepository;

    /**
     * Dodaje nową ofertę turystyczną
     */
    public boolean dodajOferte(OfertaTurystyczna oferta) {
        try {
            if (oferta.dodajOferte()) {
                ofertaRepository.save(oferta);
                System.out.println("Oferta zapisana w bazie danych: " + oferta.getNazwa());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Błąd podczas zapisywania oferty: " + e.getMessage());
            return false;
        }
    }

    /**
     * Wyszukuje oferty według kryteriów
     */
    public List<OfertaTurystyczna> wyszukajOferty(String kraj, LocalDate dataOd, LocalDate dataDo, String typ) {
        try {
            List<OfertaTurystyczna> wyniki = ofertaRepository.findByKryteria(kraj, dataOd, dataDo, typ);
            return wyniki != null ? wyniki : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Błąd podczas wyszukiwania ofert: " + e.getMessage());
            return new ArrayList<>(); // Zwróć pustą listę zamiast null
        }
    }

    public List<OfertaTurystyczna> pobierzAktywneOferty() {
        try {
            List<OfertaTurystyczna> oferty = ofertaRepository.findByStatusOferty("AKTYWNA");
            return oferty != null ? oferty : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Błąd podczas pobierania aktywnych ofert: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Aktualizuje dostępność wszystkich ofert
     */
    public void aktualizujDostepnoscOfert() {
        List<OfertaTurystyczna> oferty = pobierzAktywneOferty();
        for (OfertaTurystyczna oferta : oferty) {
            oferta.sprawdzDostepnosc();
            ofertaRepository.save(oferta);
        }
        System.out.println("Zaktualizowano dostępność " + oferty.size() + " ofert");
    }

    /**
     * Pobiera ofertę według ID
     */
    public OfertaTurystyczna pobierzOferte(Long id) {
        Optional<OfertaTurystyczna> oferta = ofertaRepository.findById(id);
        return oferta.orElse(null);
    }

    /**
     * Pobiera wymagany język na podstawie destynacji
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
                    default: return "Angielski";
                }
            }
        }
        return null;
    }
    public List<OfertaTurystyczna> getAllOferty() {
        return ofertaRepository.findAll();
    }
}