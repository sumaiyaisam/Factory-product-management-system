
package lk.samson.coreflow.modules.uom.repository;

import lk.samson.coreflow.modules.uom.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitOfMeasureRepository
        extends JpaRepository<UnitOfMeasure, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCaseAndIdNot(
            String code, Long id
    );

    boolean existsByNameIgnoreCaseAndIdNot(
            String name, Long id
    );
}