
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
        name = "stock_balances",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_product_location",
                        columnNames = {"product_id", "location_id"}
                )
        },
        indexes = {
                @Index(name = "idx_stock_product", columnList = "product_id"),
                @Index(name = "idx_stock_location", columnList = "location_id")
        }
)
public class StockBalance {

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

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (quantity == null) {
            quantity = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}