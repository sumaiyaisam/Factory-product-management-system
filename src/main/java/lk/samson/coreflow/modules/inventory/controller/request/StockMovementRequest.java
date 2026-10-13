
package lk.samson.coreflow.modules.inventory.controller.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lk.samson.coreflow.modules.inventory.entity.StockMovementType;

import java.math.BigDecimal;

@Data
public class StockMovementRequest {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Storage location ID is required")
    @Positive(message = "Storage location ID must be positive")
    private Long locationId;

    @NotNull(message = "Movement type is required")
    private StockMovementType movementType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(
            value = "0.001",
            message = "Quantity must be greater than zero"
    )
    private BigDecimal quantity;

    @Size(max = 100, message = "Reference number cannot exceed 100 characters")
    private String referenceNumber;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}