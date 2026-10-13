
package lk.samson.coreflow.modules.warehouses.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lk.samson.coreflow.modules.warehouses.entity.WarehouseType;

@Data
public class WarehouseRequest {

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 30, message = "Warehouse code cannot exceed 30 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Warehouse code can contain letters, numbers, hyphens and underscores only"
    )
    private String code;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 150, message = "Warehouse name cannot exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotNull(message = "Warehouse type is required")
    private WarehouseType warehouseType = WarehouseType.GENERAL;

    private Boolean active = true;
}