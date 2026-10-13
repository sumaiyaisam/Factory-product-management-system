
package lk.samson.coreflow.modules.categories.service;

import lk.samson.coreflow.modules.categories.controller.request.CategoryRequest;
import lk.samson.coreflow.modules.categories.controller.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    List<CategoryResponse> getAll(String search);

    CategoryResponse getById(Long id);

    CategoryResponse update(Long id, CategoryRequest request);

    void deactivate(Long id);
}