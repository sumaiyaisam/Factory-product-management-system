
package lk.samson.coreflow.modules.locations.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lk.samson.coreflow.modules.locations.entity.LocationType;

@Data
public class StorageLocationRequest {

    @NotNull(message = "Warehouse ID is required")
    @Positive(message = "Warehouse ID must be positive")
    private Long warehouseId;

    @NotBlank(message = "Location code is required")
    @Size(max = 30, message = "Location code cannot exceed 30 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Location code can contain letters, numbers, hyphens and underscores only"
    )
    private String code;

    @NotBlank(message = "Location name is required")
    @Size(max = 150, message = "Location name cannot exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Location type is required")
    private LocationType locationType = LocationType.GENERAL;

    private Boolean active = true;
}