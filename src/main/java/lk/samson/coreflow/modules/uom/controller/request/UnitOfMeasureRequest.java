
package lk.samson.coreflow.modules.uom.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitOfMeasureRequest {

    @NotBlank(message = "Unit code is required")
    @Size(max = 20, message = "Unit code cannot exceed 20 characters")
    private String code;

    @NotBlank(message = "Unit name is required")
    @Size(max = 100, message = "Unit name cannot exceed 100 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
}