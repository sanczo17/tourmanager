package org.tourmanager.repository;

import org.tourmanager.model.OfertaTurystyczna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OfertaTurystycznaRepository extends JpaRepository<OfertaTurystyczna, Long> {

    /**
     * Znajdź oferty według statusu
     */
    List<OfertaTurystyczna> findByStatusOferty(String status);

    /**
     * Znajdź oferty według kraju docelowego
     */
    List<OfertaTurystyczna> findByKrajDocelowy(String krajDocelowy);

    /**
     * Znajdź oferty według typu wycieczki
     */
    List<OfertaTurystyczna> findByTypWycieczki(String typWycieczki);

    /**
     * Znajdź oferty w określonym przedziale dat
     */
    @Query("SELECT o FROM OfertaTurystyczna o WHERE o.dataWyjazdu >= :dataOd AND o.dataPowrotu <= :dataDo")
    List<OfertaTurystyczna> findByDataWyjazdoBetween(@Param("dataOd") LocalDate dataOd, @Param("dataDo") LocalDate dataDo);

    /**
     * Znajdź oferty według kompleksowych kryteriów
     */
    @Query("SELECT o FROM OfertaTurystyczna o WHERE " +
            "(:kraj IS NULL OR o.krajDocelowy = :kraj) AND " +
            "(:dataOd IS NULL OR o.dataWyjazdu >= :dataOd) AND " +
            "(:dataDo IS NULL OR o.dataPowrotu <= :dataDo) AND " +
            "(:typ IS NULL OR o.typWycieczki = :typ) AND " +
            "o.statusOferty = 'AKTYWNA' AND " +
            "o.dostepneMiejsca > 0")
    List<OfertaTurystyczna> findByKryteria(
            @Param("kraj") String kraj,
            @Param("dataOd") LocalDate dataOd,
            @Param("dataDo") LocalDate dataDo,
            @Param("typ") String typ
    );

    /**
     * Znajdź oferty z dostępnymi miejscami
     */
    @Query("SELECT o FROM OfertaTurystyczna o WHERE o.dostepneMiejsca > 0 AND o.statusOferty = 'AKTYWNA'")
    List<OfertaTurystyczna> findDostepneOferty();

    /**
     * Znajdź oferty według nazwy (wyszukiwanie tekstowe)
     */
    @Query("SELECT o FROM OfertaTurystyczna o WHERE LOWER(o.nazwa) LIKE LOWER(CONCAT('%', :nazwa, '%'))")
    List<OfertaTurystyczna> findByNazwaContaining(@Param("nazwa") String nazwa);

    /**
     * Znajdź oferty w określonym przedziale cenowym
     */
    @Query("SELECT o FROM OfertaTurystyczna o WHERE o.cenaZaOsobe BETWEEN :cenaMin AND :cenaMax")
    List<OfertaTurystyczna> findByCenaBetween(@Param("cenaMin") java.math.BigDecimal cenaMin,
                                              @Param("cenaMax") java.math.BigDecimal cenaMax);

    /**
     * Znajdź najbardziej popularne kierunki (według liczby umów)
     */
    @Query("SELECT o.krajDocelowy, COUNT(u) as liczbaUmow FROM OfertaTurystyczna o " +
            "LEFT JOIN o.umowy u WHERE u.statusUmowy != 'ANULOWANA' " +
            "GROUP BY o.krajDocelowy ORDER BY liczbaUmow DESC")
    List<Object[]> findPopularneKierunki();
}