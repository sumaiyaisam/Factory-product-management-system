
package lk.samson.coreflow.modules.warehouses.service;

import lk.samson.coreflow.modules.warehouses.controller.request.WarehouseRequest;
import lk.samson.coreflow.modules.warehouses.controller.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {

    WarehouseResponse create(WarehouseRequest request);

    List<WarehouseResponse> getAll(String search);

    WarehouseResponse getById(Long id);

    WarehouseResponse update(Long id, WarehouseRequest request);

    void delete(Long id);
}