
package lk.samson.coreflow.modules.products.service.impl;

import lk.samson.coreflow.modules.categories.entity.Category;
import lk.samson.coreflow.modules.categories.repository.CategoryRepository;
import lk.samson.coreflow.modules.products.controller.request.ProductRequest;
import lk.samson.coreflow.modules.products.controller.response.ProductResponse;
import lk.samson.coreflow.modules.products.entity.Product;
import lk.samson.coreflow.modules.products.repository.ProductRepository;
import lk.samson.coreflow.modules.products.service.ProductService;
import lk.samson.coreflow.modules.uom.entity.UnitOfMeasure;
import lk.samson.coreflow.modules.uom.repository.UnitOfMeasureRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository uomRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UnitOfMeasureRepository uomRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.uomRepository = uomRepository;
    }

    @Override
    public ProductResponse create(ProductRequest request) {

        validateRequest(request);

        String sku = request.getSku().trim();

        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product SKU already exists: " + sku
            );
        }

        Category category = getActiveCategory(request.getCategoryId());
        UnitOfMeasure uom = getActiveUom(request.getUomId());

        Product product = new Product();
        mapRequestToEntity(request, product, category, uom);

        return mapEntityToResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(String search) {

        String keyword = search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);

        return productRepository.findAll().stream()
                .filter(product -> keyword.isEmpty()
                        || contains(product.getSku(), keyword)
                        || contains(product.getName(), keyword)
                        || contains(product.getDescription(), keyword)
                        || (product.getCategory() != null
                        && (contains(product.getCategory().getName(), keyword)
                        || contains(product.getCategory().getCode(), keyword)))
                        || (product.getUnitOfMeasure() != null
                        && (contains(product.getUnitOfMeasure().getName(), keyword)
                        || contains(product.getUnitOfMeasure().getCode(), keyword))))
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return mapEntityToResponse(findProductById(id));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {

        validateRequest(request);

        Product product = findProductById(id);
        String sku = request.getSku().trim();

        if (productRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product SKU already exists: " + sku
            );
        }

        Category category = getActiveCategory(request.getCategoryId());
        UnitOfMeasure uom = getActiveUom(request.getUomId());

        mapRequestToEntity(request, product, category, uom);

        return mapEntityToResponse(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {

        Product product = findProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private void validateRequest(ProductRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Product SKU is required"
            );
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Product name is required"
            );
        }

        if (request.getSellingPrice() == null
                || request.getSellingPrice().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selling price is required and cannot be negative"
            );
        }

        if (request.getCostPrice() == null
                || request.getCostPrice().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cost price is required and cannot be negative"
            );
        }
    }

    private Product findProductById(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID must be positive"
            );
        }

        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + id
                ));
    }

    private Category getActiveCategory(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Valid category ID is required"
            );
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found with ID: " + id
                ));

        if (!category.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign an inactive category"
            );
        }

        return category;
    }

    private UnitOfMeasure getActiveUom(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Valid UOM ID is required"
            );
        }

        UnitOfMeasure uom = uomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unit of measure not found with ID: " + id
                ));

        if (!uom.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign an inactive unit of measure"
            );
        }

        return uom;
    }

    private void mapRequestToEntity(
            ProductRequest request,
            Product product,
            Category category,
            UnitOfMeasure uom
    ) {
        product.setSku(request.getSku().trim());
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setUnitOfMeasure(uom);
        product.setSellingPrice(request.getSellingPrice());
        product.setCostPrice(request.getCostPrice());
        product.setActive(request.getActive() == null || request.getActive());
    }

    private ProductResponse mapEntityToResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSellingPrice(product.getSellingPrice());
        response.setCostPrice(product.getCostPrice());
        response.setActive(product.isActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryCode(product.getCategory().getCode());
            response.setCategoryName(product.getCategory().getName());
        }

        if (product.getUnitOfMeasure() != null) {
            response.setUomId(product.getUnitOfMeasure().getId());
            response.setUomCode(product.getUnitOfMeasure().getCode());
            response.setUomName(product.getUnitOfMeasure().getName());
        }

        return response;
    }

    private boolean contains(String value, String keyword) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}