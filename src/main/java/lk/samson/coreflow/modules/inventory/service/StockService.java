package lk.samson.coreflow.modules.inventory.service;

import lk.samson.coreflow.modules.inventory.controller.request.StockMovementRequest;
import lk.samson.coreflow.modules.inventory.controller.response.StockBalanceResponse;
import lk.samson.coreflow.modules.inventory.controller.response.StockTransactionResponse;

import java.util.List;

public interface StockService {

    StockTransactionResponse createMovement(StockMovementRequest request);

    List<StockBalanceResponse> getBalances(
            Long productId,
            Long locationId
    );

    List<StockTransactionResponse> getTransactions(
            Long productId,
            Long locationId
    );
}