package antifraud.repository;

import antifraud.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT COUNT(DISTINCT t.region) " +
            "FROM Transaction t " +
            "WHERE t.date BETWEEN :startDateTime AND :endDateTime " +
            "AND t.region <> :currentRegion")
    Long countDistinctRegionsInPeriodExcludingCurrentRegion(@Param("startDateTime") LocalDateTime startDateTime,
                                                           @Param("endDateTime") LocalDateTime endDateTime,
                                                           @Param("currentRegion") String currentRegion);

    @Query("SELECT COUNT(DISTINCT t.ip) " +
            "FROM Transaction t " +
            "WHERE t.date BETWEEN :startDateTime AND :endDateTime " +
            "AND t.ip <> :currentIp")
    Long countDistinctIpsInPeriodExcludingCurrentIp(@Param("startDateTime") LocalDateTime startDateTime,
                                                      @Param("endDateTime") LocalDateTime endDateTime,
                                                      @Param("currentIp") String currentIp);
    List<Transaction> findAllByOrderByIdAsc();
    List<Transaction> findAllByNumberOrderByIdAsc(String number);
}
