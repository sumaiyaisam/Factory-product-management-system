
package lk.samson.coreflow.modules.warehouses.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "warehouses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_warehouse_code",
                        columnNames = "code"
                )
        }
)
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WarehouseType warehouseType = WarehouseType.GENERAL;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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