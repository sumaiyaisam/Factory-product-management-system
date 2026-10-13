
package lk.samson.coreflow.modules.categories.service.impl;

import lk.samson.coreflow.modules.categories.controller.request.CategoryRequest;
import lk.samson.coreflow.modules.categories.controller.response.CategoryResponse;
import lk.samson.coreflow.modules.categories.entity.Category;
import lk.samson.coreflow.modules.categories.repository.CategoryRepository;

import lk.samson.coreflow.modules.categories.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        String code = request.getCode().trim();
        String name = request.getName().trim();

        checkDuplicates(code, name, null);

        Category category = new Category();
        applyRequest(category, request);
        category.setCode(code);
        category.setName(name);

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(String search) {
        List<Category> categories;

        if (search == null || search.isBlank()) {
            categories = categoryRepository.findAll();
        } else {
            String keyword = search.trim();
            categories =
                    categoryRepository
                            .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                                    keyword, keyword
                            );
        }

        return categories.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return toResponse(findCategory(id));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findCategory(id);

        String code = request.getCode().trim();
        String name = request.getName().trim();

        checkDuplicates(code, name, id);

        applyRequest(category, request);
        category.setCode(code);
        category.setName(name);

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public void deactivate(Long id) {
        Category category = findCategory(id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    private void checkDuplicates(String code, String name, Long id) {
        boolean codeExists = id == null
                ? categoryRepository.existsByCodeIgnoreCase(code)
                : categoryRepository.existsByCodeIgnoreCaseAndIdNot(code, id);

        boolean nameExists = id == null
                ? categoryRepository.existsByNameIgnoreCase(name)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id);

        if (codeExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category code already exists"
            );
        }

        if (nameExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category name already exists"
            );
        }
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found"
                ));
    }

    private void applyRequest(Category category, CategoryRequest request) {
        category.setDescription(request.getDescription());

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setCode(category.getCode());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setActive(category.isActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        return response;
    }
}