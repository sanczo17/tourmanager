package org.tourmanager.repository;

import org.tourmanager.model.Wycieczka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WycieczkaRepository extends JpaRepository<Wycieczka, Long> {

    /**
     * Znajdź wycieczki według statusu
     */
    List<Wycieczka> findByStatusWycieczki(String status);

    /**
     * Znajdź wycieczki według pilota
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.pilot.id = :pilotId")
    List<Wycieczka> findByPilotId(@Param("pilotId") Long pilotId);

    /**
     * Znajdź wycieczki według hotelu
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.hotel.id = :hotelId")
    List<Wycieczka> findByHotelId(@Param("hotelId") Long hotelId);

    /**
     * Znajdź wycieczki według oferty
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.oferta.id = :ofertaId")
    List<Wycieczka> findByOfertaId(@Param("ofertaId") Long ofertaId);

    /**
     * Znajdź wycieczki w określonym okresie
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.dataRozpoczecia BETWEEN :dataOd AND :dataDo")
    List<Wycieczka> findByDataRozpoczeciaBetween(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Znajdź aktualnie trwające wycieczki
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.statusWycieczki = 'W_TRAKCIE'")
    List<Wycieczka> findAktualneTrwajaceWycieczki();

    /**
     * Znajdź planowane wycieczki
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.statusWycieczki = 'PLANOWANA' ORDER BY w.dataRozpoczecia ASC")
    List<Wycieczka> findPlanowaneWycieczki();

    /**
     * Znajdź wycieczki zakończone w określonym okresie
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.statusWycieczki = 'ZAKONCZONA' AND w.dataZakonczenia BETWEEN :dataOd AND :dataDo")
    List<Wycieczka> findZakonczone(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Znajdź wycieczki bez przypisanego pilota
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.pilot IS NULL AND w.statusWycieczki = 'PLANOWANA'")
    List<Wycieczka> findWycieczkaiBezPilota();

    /**
     * Znajdź wycieczki bez przypisanego hotelu
     */
    @Query("SELECT w FROM Wycieczka w WHERE w.hotel IS NULL AND w.statusWycieczki = 'PLANOWANA'")
    List<Wycieczka> findWycieczkaiBezHotelu();

    /**
     * Statystyki wycieczek według miesięcy
     */
    @Query("SELECT YEAR(w.dataRozpoczecia), MONTH(w.dataRozpoczecia), COUNT(w), SUM(w.liczbaUczestnikow) " +
            "FROM Wycieczka w WHERE w.statusWycieczki = 'ZAKONCZONA' " +
            "GROUP BY YEAR(w.dataRozpoczecia), MONTH(w.dataRozpoczecia) " +
            "ORDER BY YEAR(w.dataRozpoczecia) DESC, MONTH(w.dataRozpoczecia) DESC")
    List<Object[]> findStatystykiMiesieczne();
}