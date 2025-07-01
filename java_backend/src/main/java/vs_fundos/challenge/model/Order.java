package vs_fundos.challenge.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import vs_fundos.challenge.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private BigDecimal totalValue;

    @Column(nullable = false)
    private LocalDateTime orderDateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime orderDateUpdated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private OrderStatus status;
}


