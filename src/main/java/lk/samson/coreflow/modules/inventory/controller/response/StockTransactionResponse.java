package lk.samson.coreflow.modules.inventory.controller.response;

import lk.samson.coreflow.modules.inventory.entity.StockMovementType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockTransactionResponse {

    private Long id;

    private Long productId;
    private String productSku;
    private String productName;

    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    private Long locationId;
    private String locationCode;
    private String locationName;

    private StockMovementType movementType;
    private BigDecimal quantity;
    private BigDecimal balanceAfter;
    private String referenceNumber;
    private String reason;
    private LocalDateTime createdAt;
}