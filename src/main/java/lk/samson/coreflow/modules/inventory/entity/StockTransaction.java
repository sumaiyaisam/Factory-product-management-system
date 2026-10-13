
package lk.samson.coreflow.modules.inventory.entity;

import jakarta.persistence.*;
import lk.samson.coreflow.modules.locations.entity.StorageLocation;
import lk.samson.coreflow.modules.products.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "stock_transactions",
        indexes = {
                @Index(name = "idx_stock_tx_product", columnList = "product_id"),
                @Index(name = "idx_stock_tx_location", columnList = "location_id"),
                @Index(name = "idx_stock_tx_created", columnList = "created_at"),
                @Index(name = "idx_stock_tx_reference", columnList = "reference_number")
        }
)
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private StorageLocation location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockMovementType movementType;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal balanceAfter;

    @Column(length = 100)
    private String referenceNumber;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}