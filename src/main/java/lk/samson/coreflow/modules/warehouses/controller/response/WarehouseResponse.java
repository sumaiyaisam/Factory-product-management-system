
package lk.samson.coreflow.modules.warehouses.controller.response;

import lombok.Data;
import lk.samson.coreflow.modules.warehouses.entity.WarehouseType;

import java.time.LocalDateTime;

@Data
public class WarehouseResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String address;
    private String city;
    private WarehouseType warehouseType;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}