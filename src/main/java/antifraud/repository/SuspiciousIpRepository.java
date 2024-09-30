package antifraud.repository;

import antifraud.domain.SuspiciousIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspiciousIpRepository extends JpaRepository<SuspiciousIp,Long> {
    Boolean existsByIp (String ip);
    Optional<SuspiciousIp> findByIp (String ip);
    List<SuspiciousIp> findAllByOrderByIdAsc();
}
