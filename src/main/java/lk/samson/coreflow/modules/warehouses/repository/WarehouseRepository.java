
package lk.samson.coreflow.modules.warehouses.repository;

import lk.samson.coreflow.modules.warehouses.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository
        extends JpaRepository<Warehouse, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}