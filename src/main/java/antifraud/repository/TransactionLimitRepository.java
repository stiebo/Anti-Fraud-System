package antifraud.repository;

import antifraud.domain.TransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLimitRepository extends JpaRepository<TransactionLimit,Long> {
}
