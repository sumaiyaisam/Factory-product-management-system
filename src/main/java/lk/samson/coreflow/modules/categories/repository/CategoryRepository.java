
package lk.samson.coreflow.modules.categories.repository;

import lk.samson.coreflow.modules.categories.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Category> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            String name, String code
    );
}