
package lk.samson.coreflow.modules.inventory.repository;

import lk.samson.coreflow.modules.inventory.entity.StockBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockBalanceRepository
        extends JpaRepository<StockBalance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM StockBalance b
        WHERE b.product.id = :productId
          AND b.location.id = :locationId
        """)
    Optional<StockBalance> findForUpdate(
            @Param("productId") Long productId,
            @Param("locationId") Long locationId
    );

    Optional<StockBalance> findByProduct_IdAndLocation_Id(
            Long productId,
            Long locationId
    );

    List<StockBalance> findByProduct_Id(Long productId);

    List<StockBalance> findByLocation_Id(Long locationId);
}