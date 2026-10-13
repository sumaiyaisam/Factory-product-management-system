
package lk.samson.coreflow.modules.locations.repository;

import lk.samson.coreflow.modules.locations.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorageLocationRepository
        extends JpaRepository<StorageLocation, Long> {

    boolean existsByWarehouse_IdAndCodeIgnoreCase(
            Long warehouseId,
            String code
    );

    boolean existsByWarehouse_IdAndCodeIgnoreCaseAndIdNot(
            Long warehouseId,
            String code,
            Long id
    );

    List<StorageLocation> findByWarehouse_Id(Long warehouseId);
}