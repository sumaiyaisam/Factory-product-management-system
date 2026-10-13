
package lk.samson.coreflow.modules.inventory.controller;

import jakarta.validation.Valid;
import lk.samson.coreflow.modules.inventory.controller.request.StockMovementRequest;
import lk.samson.coreflow.modules.inventory.controller.response.StockBalanceResponse;
import lk.samson.coreflow.modules.inventory.controller.response.StockTransactionResponse;
import lk.samson.coreflow.modules.inventory.service.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransactionResponse createMovement(
            @Valid @RequestBody StockMovementRequest request) {
        return stockService.createMovement(request);
    }

    @GetMapping("/balances")
    public List<StockBalanceResponse> getBalances(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long locationId) {
        return stockService.getBalances(productId, locationId);
    }

    @GetMapping("/transactions")
    public List<StockTransactionResponse> getTransactions(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long locationId) {
        return stockService.getTransactions(productId, locationId);
    }
}