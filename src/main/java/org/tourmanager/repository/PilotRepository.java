package org.tourmanager.repository;

import org.tourmanager.model.Pilot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PilotRepository extends JpaRepository<Pilot, Long> {

    /**
     * Znajdź pilotów według imienia i nazwiska
     */
    List<Pilot> findByImieAndNazwisko(String imie, String nazwisko);

    /**
     * Znajdź pilota według numeru licencji
     */
    Pilot findByNumerLicencji(String numerLicencji);

    /**
     * Znajdź pilotów z wygasającymi licencjami
     */
    @Query("SELECT p FROM Pilot p WHERE p.dataWaznosciLicencji BETWEEN CURRENT_DATE AND :dataGraniczna")
    List<Pilot> findPilotowZWygasajacymiLicencjami(@Param("dataGraniczna") LocalDate dataGraniczna);

    /**
     * Znajdź pilotów znających określony język
     */
    @Query("SELECT p FROM Pilot p WHERE LOWER(p.znajomoscJezykow) LIKE LOWER(CONCAT('%', :jezyk, '%'))")
    List<Pilot> findByZnajomoscJezyka(@Param("jezyk") String jezyk);

    /**
     * Znajdź pilotów według specjalizacji
     */
    @Query("SELECT p FROM Pilot p WHERE LOWER(p.specjalizacje) LIKE LOWER(CONCAT('%', :specjalizacja, '%'))")
    List<Pilot> findBySpecjalizacja(@Param("specjalizacja") String specjalizacja);

    /**
     * Znajdź dostępnych pilotów w określonym okresie
     */
    @Query("SELECT p FROM Pilot p WHERE p.statusPilota = 'AKTYWNY' AND " +
            "(p.dostepnoscOd IS NULL OR p.dostepnoscOd <= :dataOd) AND " +
            "(p.dostepnoscDo IS NULL OR p.dostepnoscDo >= :dataDo)")
    List<Pilot> findDostepnychPilotow(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Znajdź pilotów według stażu pracy
     */
    @Query("SELECT p FROM Pilot p WHERE p.dataZatrudnienia <= :dataGraniczna")
    List<Pilot> findDoswiadczonychPilotow(@Param("dataGraniczna") LocalDate dataGraniczna);

    /**
     * Statystyki pilotów według liczby wycieczek
     */
    @Query("SELECT p.imie, p.nazwisko, COUNT(w) as liczbaWycieczek " +
            "FROM Pilot p LEFT JOIN p.wycieczki w " +
            "WHERE w.statusWycieczki = 'ZAKONCZONA' " +
            "GROUP BY p.id ORDER BY liczbaWycieczek DESC")
    List<Object[]> findStatystykiPilotow();
}