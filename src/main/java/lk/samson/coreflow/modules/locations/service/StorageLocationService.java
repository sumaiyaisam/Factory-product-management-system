
package lk.samson.coreflow.modules.locations.service;

import lk.samson.coreflow.modules.locations.controller.request.StorageLocationRequest;
import lk.samson.coreflow.modules.locations.controller.response.StorageLocationResponse;

import java.util.List;

public interface StorageLocationService {

    StorageLocationResponse create(StorageLocationRequest request);

    List<StorageLocationResponse> getAll(
            String search,
            Long warehouseId
    );

    StorageLocationResponse getById(Long id);

    StorageLocationResponse update(
            Long id,
            StorageLocationRequest request
    );

    void delete(Long id);
}