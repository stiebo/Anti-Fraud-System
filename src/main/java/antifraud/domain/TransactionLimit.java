package antifraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "\"TRANSACTIONLIMIT\"")
@Entity
public class TransactionLimit {
    @Id
    Long id;
    @Column(nullable = false)
    Long maxAllowed;
    @Column(nullable = false)
    Long maxManual;

    public TransactionLimit(Long maxAllowed, Long maxManual) {
        this.id = 1L;
        this.maxAllowed = maxAllowed;
        this.maxManual = maxManual;
    }
}
