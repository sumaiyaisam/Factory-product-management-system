
package lk.samson.coreflow.modules.locations.controller.response;

import lombok.Data;
import lk.samson.coreflow.modules.locations.entity.LocationType;

import java.time.LocalDateTime;

@Data
public class StorageLocationResponse {

    private Long id;

    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    private String code;
    private String name;
    private String description;
    private LocationType locationType;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}