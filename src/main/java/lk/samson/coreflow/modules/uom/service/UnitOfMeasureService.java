
package lk.samson.coreflow.modules.uom.service;

import lk.samson.coreflow.modules.uom.controller.request.UnitOfMeasureRequest;
import lk.samson.coreflow.modules.uom.controller.response.UnitOfMeasureResponse;

import java.util.List;

public interface UnitOfMeasureService {

    UnitOfMeasureResponse create(UnitOfMeasureRequest request);

    List<UnitOfMeasureResponse> getAll(String search);

    UnitOfMeasureResponse getById(Long id);

    UnitOfMeasureResponse update(
            Long id,
            UnitOfMeasureRequest request
    );

    void deactivate(Long id);
}