
package lk.samson.coreflow.modules.products.controller;

import jakarta.validation.Valid;
import lk.samson.coreflow.modules.products.controller.request.ProductRequest;
import lk.samson.coreflow.modules.products.controller.response.ProductResponse;
import lk.samson.coreflow.modules.products.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @GetMapping
    public List<ProductResponse> getAll(
            @RequestParam(required = false) String search) {
        return productService.getAll(search);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}