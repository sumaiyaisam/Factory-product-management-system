
package lk.samson.coreflow.modules.products.entity;

import jakarta.persistence.*;
import lombok.Data;

import lk.samson.coreflow.modules.categories.entity.Category;
import lk.samson.coreflow.modules.uom.entity.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_sku",
                        columnNames = "sku"
                )
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uom_id", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal sellingPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}