
package lk.samson.coreflow.modules.inventory.repository;

import lk.samson.coreflow.modules.inventory.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransactionRepository
        extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findAllByOrderByCreatedAtDesc();

    List<StockTransaction> findByProduct_IdOrderByCreatedAtDesc(
            Long productId
    );

    List<StockTransaction> findByLocation_IdOrderByCreatedAtDesc(
            Long locationId
    );

    List<StockTransaction>
    findByProduct_IdAndLocation_IdOrderByCreatedAtDesc(
            Long productId,
            Long locationId
    );
}