package lk.samson.coreflow.modules.inventory.controller.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockBalanceResponse {

    private Long id;

    private Long productId;
    private String productSku;
    private String productName;
    private String uomCode;

    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    private Long locationId;
    private String locationCode;
    private String locationName;

    private BigDecimal quantity;
}