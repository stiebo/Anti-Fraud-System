package antifraud.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "\"TRANSACTION\"")
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private Long amount;
    @Column(nullable = false)
    String ip;
    @Column(nullable = false)
    String number;
    @Column(nullable = false)
    String region;
    @Column(nullable = false)
    LocalDateTime date;
    @Column(nullable = false)
    String result;
    @Column(nullable = false)
    String feedback;
}
