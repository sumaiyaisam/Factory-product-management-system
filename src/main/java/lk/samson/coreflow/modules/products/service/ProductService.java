
package lk.samson.coreflow.modules.products.service;

import lk.samson.coreflow.modules.products.controller.request.ProductRequest;
import lk.samson.coreflow.modules.products.controller.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    List<ProductResponse> getAll(String search);

    ProductResponse getById(Long id);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}