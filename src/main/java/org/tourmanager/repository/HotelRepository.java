package org.tourmanager.repository;

import org.tourmanager.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Znajdź hotele według kraju
     */
    List<Hotel> findByKraj(String kraj);

    /**
     * Znajdź hotele według miasta
     */
    List<Hotel> findByMiasto(String miasto);

    /**
     * Znajdź hotele według kategorii
     */
    List<Hotel> findByKategoria(Integer kategoria);

    /**
     * Znajdź hotele według statusu współpracy
     */
    List<Hotel> findByStatusWspolpracy(String status);

    /**
     * Znajdź hotele według nazwy (wyszukiwanie tekstowe)
     */
    @Query("SELECT h FROM Hotel h WHERE LOWER(h.nazwa) LIKE LOWER(CONCAT('%', :nazwa, '%'))")
    List<Hotel> findByNazwaContaining(@Param("nazwa") String nazwa);

    /**
     * Znajdź hotele z wysokimi ocenami
     */
    @Query("SELECT h FROM Hotel h WHERE h.ocenaJakosci >= :minOcena ORDER BY h.ocenaJakosci DESC")
    List<Hotel> findHoteleZWysokimiOcenami(@Param("minOcena") BigDecimal minOcena);

    /**
     * Znajdź hotele według kategorii i kraju
     */
    List<Hotel> findByKategoriaAndKraj(Integer kategoria, String kraj);

    /**
     * Znajdź hotele według miasta i kategorii
     */
    List<Hotel> findByMiastoAndKategoria(String miasto, Integer kategoria);

    /**
     * Znajdź najlepsze hotele (kategoria 4-5 gwiazdek, ocena powyżej 8.0)
     */
    @Query("SELECT h FROM Hotel h WHERE h.kategoria >= 4 AND h.ocenaJakosci >= 8.0 " +
            "AND h.statusWspolpracy = 'AKTYWNA' " +
            "ORDER BY h.ocenaJakosci DESC, h.kategoria DESC")
    List<Hotel> findNajlepszieHotele();

    /**
     * Znajdź stałych partnerów (hotele z wieloma wycieczkami)
     */
    @Query("SELECT h FROM Hotel h WHERE SIZE(h.wycieczki) >= :minLiczbaWycieczek")
    List<Hotel> findStalychPartnerow(@Param("minLiczbaWycieczek") int minLiczbaWycieczek);

    /**
     * Znajdź hotele dostępne w określonym okresie
     */
    @Query("SELECT h FROM Hotel h WHERE h.statusWspolpracy = 'AKTYWNA' AND " +
            "NOT EXISTS (SELECT w FROM Wycieczka w WHERE w.hotel = h AND " +
            "((w.dataRozpoczecia <= :dataOd AND w.dataZakonczenia >= :dataOd) OR " +
            "(w.dataRozpoczecia <= :dataDo AND w.dataZakonczenia >= :dataDo) OR " +
            "(w.dataRozpoczecia >= :dataOd AND w.dataZakonczenia <= :dataDo)) AND " +
            "(w.statusWycieczki = 'PLANOWANA' OR w.statusWycieczki = 'W_TRAKCIE'))")
    List<Hotel> findDostepneHotele(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Statystyki hoteli według krajów
     */
    @Query("SELECT h.kraj, COUNT(h), AVG(h.ocenaJakosci), AVG(h.kategoria) " +
            "FROM Hotel h WHERE h.statusWspolpracy = 'AKTYWNA' " +
            "GROUP BY h.kraj ORDER BY COUNT(h) DESC")
    List<Object[]> findStatystykiWgKrajow();

    /**
     * Statystyki hoteli według kategorii
     */
    @Query("SELECT h.kategoria, COUNT(h), AVG(h.ocenaJakosci) " +
            "FROM Hotel h WHERE h.statusWspolpracy = 'AKTYWNA' " +
            "GROUP BY h.kategoria ORDER BY h.kategoria DESC")
    List<Object[]> findStatystykiWgKategorii();

    /**
     * Znajdź hotele wymagające kontaktu (bez wycieczek w ostatnim roku)
     */
    @Query("SELECT h FROM Hotel h WHERE h.statusWspolpracy = 'AKTYWNA' AND " +
            "NOT EXISTS (SELECT w FROM Wycieczka w WHERE w.hotel = h AND w.dataRozpoczecia >= :dataGraniczna)")
    List<Hotel> findHoteleWymagajaceKontaktu(@Param("dataGraniczna") LocalDate dataGraniczna);

    /**
     * Znajdź hotele z najgorszymi ocenami (poniżej określonej wartości)
     */
    @Query("SELECT h FROM Hotel h WHERE h.ocenaJakosci < :maxOcena " +
            "AND h.statusWspolpracy = 'AKTYWNA' ORDER BY h.ocenaJakosci ASC")
    List<Hotel> findHoteleZNiskimiOcenami(@Param("maxOcena") BigDecimal maxOcena);

    /**
     * Znajdź hotele według osób kontaktowych
     */
    @Query("SELECT h FROM Hotel h WHERE LOWER(h.osobaKontaktowa) LIKE LOWER(CONCAT('%', :osoba, '%'))")
    List<Hotel> findByOsobaKontaktowa(@Param("osoba") String osoba);

    /**
     * Znajdź hotele według numeru telefonu
     */
    Hotel findByTelefon(String telefon);

    /**
     * Znajdź hotele według adresu email
     */
    Hotel findByEmail(String email);

    /**
     * Ranking hoteli według liczby zrealizowanych wycieczek
     */
    @Query("SELECT h, COUNT(w) as liczbaWycieczek FROM Hotel h " +
            "LEFT JOIN h.wycieczki w WHERE w.statusWycieczki = 'ZAKONCZONA' " +
            "GROUP BY h ORDER BY liczbaWycieczek DESC")
    List<Object[]> findRankingHoteliWgWycieczek();

    /**
     * Znajdź hotele z ocenami w określonym przedziale
     */
    @Query("SELECT h FROM Hotel h WHERE h.ocenaJakosci BETWEEN :minOcena AND :maxOcena " +
            "ORDER BY h.ocenaJakosci DESC")
    List<Hotel> findByOcenaBetween(@Param("minOcena") BigDecimal minOcena, @Param("maxOcena") BigDecimal maxOcena);

    /**
     * Znajdź hotele rozpoczynające współpracę w określonym okresie
     */
    @Query("SELECT h FROM Hotel h WHERE h.dataRozpoczeciaWspolpracy BETWEEN :dataOd AND :dataDo " +
            "ORDER BY h.dataRozpoczeciaWspolpracy DESC")
    List<Hotel> findNowePartnerstwa(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Średnia ocena hoteli według kraju
     */
    @Query("SELECT h.kraj, AVG(h.ocenaJakosci) FROM Hotel h " +
            "WHERE h.statusWspolpracy = 'AKTYWNA' " +
            "GROUP BY h.kraj HAVING AVG(h.ocenaJakosci) >= :minSrednia " +
            "ORDER BY AVG(h.ocenaJakosci) DESC")
    List<Object[]> findKrajeZWysokimiOcenami(@Param("minSrednia") BigDecimal minSrednia);

    /**
     * Znajdź hotele bez żadnych wycieczek
     */
    @Query("SELECT h FROM Hotel h WHERE SIZE(h.wycieczki) = 0 AND h.statusWspolpracy = 'AKTYWNA'")
    List<Hotel> findHoteleBezWycieczek();

    /**
     * Znajdź hotele według warunków współpracy (wyszukiwanie w tekście)
     */
    @Query("SELECT h FROM Hotel h WHERE LOWER(h.warunkiWspolpracy) LIKE LOWER(CONCAT('%', :warunki, '%'))")
    List<Hotel> findByWarunkiWspolpracy(@Param("warunki") String warunki);

    /**
     * Znajdź hotele z aktualnymi wycieczkami
     */
    @Query("SELECT DISTINCT h FROM Hotel h JOIN h.wycieczki w " +
            "WHERE w.statusWycieczki = 'W_TRAKCIE' OR " +
            "(w.statusWycieczki = 'PLANOWANA' AND w.dataRozpoczecia <= :dataGraniczna)")
    List<Hotel> findHoteleZAktualnumiWycieczkami(@Param("dataGraniczna") LocalDate dataGraniczna);
}