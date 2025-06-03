package org.tourmanager.repository;

import org.tourmanager.model.Klient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface KlientRepository extends JpaRepository<Klient, Long> {

    /**
     * Znajdź klienta według numeru PESEL
     */
    Klient findByPesel(String pesel);

    /**
     * Znajdź klientów według imienia i nazwiska
     */
    List<Klient> findByImieAndNazwisko(String imie, String nazwisko);

    /**
     * Znajdź klientów według numeru telefonu
     */
    Klient findByTelefon(String telefon);

    /**
     * Znajdź klientów według adresu email
     */
    Klient findByEmail(String email);

    /**
     * Znajdź klientów według statusu
     */
    List<Klient> findByStatusKlienta(String status);

    /**
     * Wyszukiwanie klientów według fragmentu nazwiska
     */
    @Query("SELECT k FROM Klient k WHERE LOWER(k.nazwisko) LIKE LOWER(CONCAT('%', :nazwisko, '%'))")
    List<Klient> findByNazwiskoContaining(@Param("nazwisko") String nazwisko);

    /**
     * Znajdź klientów zarejestrowanych w określonym okresie
     */
    List<Klient> findByDataRejestracijaBetween(LocalDate dataOd, LocalDate dataDo);

    /**
     * Znajdź klientów VIP (z wieloma wyjazdami)
     */
    @Query("SELECT k FROM Klient k WHERE SIZE(k.umowy) >= :minLiczbaUmow")
    List<Klient> findKlientowVIP(@Param("minLiczbaUmow") int minLiczbaUmow);

    /**
     * Znajdź klientów bez żadnych umów
     */
    @Query("SELECT k FROM Klient k WHERE SIZE(k.umowy) = 0")
    List<Klient> findKlientowBezUmow();

    /**
     * Statystyki klientów według miasta
     */
    @Query("SELECT SUBSTRING(k.adres, LOCATE(',', k.adres) + 1), COUNT(k) " +
            "FROM Klient k WHERE k.adres LIKE '%,%' " +
            "GROUP BY SUBSTRING(k.adres, LOCATE(',', k.adres) + 1)")
    List<Object[]> findStatystykiWgMiast();
}