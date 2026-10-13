
package lk.samson.coreflow.modules.products.controller.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;

    private Long categoryId;
    private String categoryCode;
    private String categoryName;

    private Long uomId;
    private String uomCode;
    private String uomName;

    private BigDecimal sellingPrice;
    private BigDecimal costPrice;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}