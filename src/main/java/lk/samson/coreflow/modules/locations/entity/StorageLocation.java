
package lk.samson.coreflow.modules.locations.entity;

import jakarta.persistence.*;
import lk.samson.coreflow.modules.warehouses.entity.Warehouse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "storage_locations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_location_warehouse_code",
                        columnNames = {"warehouse_id", "code"}
                )
        },
        indexes = {
                @Index(name = "idx_location_warehouse", columnList = "warehouse_id")
        }
)
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private LocationType locationType = LocationType.GENERAL;

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