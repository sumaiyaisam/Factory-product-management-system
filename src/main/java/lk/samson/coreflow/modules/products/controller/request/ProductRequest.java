
package lk.samson.coreflow.modules.products.controller.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product SKU is required")
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @NotNull(message = "Unit of measure ID is required")
    @Positive(message = "Unit of measure ID must be positive")
    private Long uomId;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.00", message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    @NotNull(message = "Cost price is required")
    @DecimalMin(value = "0.00", message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    private Boolean active = true;
}