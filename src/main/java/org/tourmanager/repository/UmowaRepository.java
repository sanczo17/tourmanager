package org.tourmanager.repository;

import org.tourmanager.model.Umowa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface UmowaRepository extends JpaRepository<Umowa, String> {

    /**
     * Znajdź umowę według numeru
     */
    Umowa findByNrUmowy(String nrUmowy);

    /**
     * Znajdź umowy według statusu
     */
    List<Umowa> findByStatusUmowy(String status);

    /**
     * Znajdź umowy zawarte w określonym okresie
     */
    @Query("SELECT u FROM Umowa u WHERE u.dataZawarcia BETWEEN :dataOd AND :dataDo ORDER BY u.dataZawarcia DESC")
    List<Umowa> findByDataZawarciaOkresu(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Znajdź umowy według klienta
     */
    @Query("SELECT u FROM Umowa u WHERE u.klient.id = :klientId ORDER BY u.dataZawarcia DESC")
    List<Umowa> findByKlientId(@Param("klientId") Long klientId);

    /**
     * Znajdź umowy według oferty
     */
    @Query("SELECT u FROM Umowa u WHERE u.oferta.id = :ofertaId ORDER BY u.dataZawarcia DESC")
    List<Umowa> findByOfertaId(@Param("ofertaId") Long ofertaId);

    /**
     * Znajdź umowy z nieopłaconymi zaliczkami
     */
    @Query("SELECT u FROM Umowa u WHERE u.dataWplatyZaliczki IS NULL AND u.statusUmowy = 'PODPISANA'")
    List<Umowa> findUmowyZNieoplaconymiZaliczkami();

    /**
     * Znajdź umowy z przekroczonym terminem dopłaty
     */
    @Query("SELECT u FROM Umowa u WHERE u.terminDoplaty < CURRENT_DATE AND u.statusUmowy = 'ZALICZKA_WPLACONA'")
    List<Umowa> findUmowyZPrzekroczonymTerminemDoplaty();

    /**
     * Znajdź umowy w określonym przedziale wartości
     */
    @Query("SELECT u FROM Umowa u WHERE u.cenaCalkowita BETWEEN :min AND :max ORDER BY u.cenaCalkowita DESC")
    List<Umowa> findByWartoscBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    /**
     * Statystyki umów według miesięcy
     */
    @Query("SELECT YEAR(u.dataZawarcia), MONTH(u.dataZawarcia), COUNT(u), SUM(u.cenaCalkowita) " +
            "FROM Umowa u WHERE u.statusUmowy != 'ANULOWANA' " +
            "GROUP BY YEAR(u.dataZawarcia), MONTH(u.dataZawarcia) " +
            "ORDER BY YEAR(u.dataZawarcia) DESC, MONTH(u.dataZawarcia) DESC")
    List<Object[]> findStatystykiMiesieczne();

    /**
     * TOP umowy według wartości
     */
    @Query("SELECT u FROM Umowa u WHERE u.statusUmowy != 'ANULOWANA' ORDER BY u.cenaCalkowita DESC")
    List<Umowa> findTopUmowyWgWartosci();

    /**
     * Znajdź umowy z rezygnacjami w określonym okresie
     */
    @Query("SELECT u FROM Umowa u WHERE u.rezygnacja IS NOT NULL AND " +
            "u.rezygnacja.dataRezygnacji BETWEEN :dataOd AND :dataDo")
    List<Umowa> findUmowyZRezygnacjami(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Oblicz łączną wartość umów w okresie
     */
    @Query("SELECT COALESCE(SUM(u.cenaCalkowita), 0) FROM Umowa u WHERE " +
            "u.dataZawarcia BETWEEN :dataOd AND :dataDo AND u.statusUmowy != 'ANULOWANA'")
    BigDecimal obliczLacznaWartoscWOkresie(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Znajdź umowy wymagające uwagi (zbliżający się termin dopłaty)
     */
    @Query("SELECT u FROM Umowa u WHERE u.terminDoplaty BETWEEN CURRENT_DATE AND :dataGraniczna " +
            "AND u.statusUmowy = 'ZALICZKA_WPLACONA'")
    List<Umowa> findUmowyWymagajaceUwagi(@Param("dataGraniczna") LocalDate dataGraniczna);

    /**
     * Znajdź umowy według klienta (PESEL)
     */
    @Query("SELECT u FROM Umowa u WHERE u.klient.pesel = :pesel ORDER BY u.dataZawarcia DESC")
    List<Umowa> findByKlientPesel(@Param("pesel") String pesel);

    /**
     * Znajdź umowy z płatnościami w określonym okresie
     */
    @Query("SELECT DISTINCT u FROM Umowa u JOIN u.platnosci p WHERE " +
            "p.dataWplaty BETWEEN :dataOd AND :dataDo")
    List<Umowa> findUmowyZPlatnosciami(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Statystyki umów według krajów
     */
    @Query("SELECT u.oferta.krajDocelowy, COUNT(u), SUM(u.cenaCalkowita) FROM Umowa u " +
            "WHERE u.statusUmowy != 'ANULOWANA' AND u.oferta.krajDocelowy IS NOT NULL " +
            "GROUP BY u.oferta.krajDocelowy ORDER BY COUNT(u) DESC")
    List<Object[]> findStatystykiWgKrajow();

    /**
     * Znajdź umowy według typu wycieczki
     */
    @Query("SELECT u FROM Umowa u WHERE u.oferta.typWycieczki = :typ ORDER BY u.dataZawarcia DESC")
    List<Umowa> findByTypWycieczki(@Param("typ") String typ);
}