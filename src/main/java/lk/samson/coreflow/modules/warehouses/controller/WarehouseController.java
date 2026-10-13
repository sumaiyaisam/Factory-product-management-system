
package lk.samson.coreflow.modules.warehouses.controller;

import jakarta.validation.Valid;
import lk.samson.coreflow.modules.warehouses.controller.request.WarehouseRequest;
import lk.samson.coreflow.modules.warehouses.controller.response.WarehouseResponse;
import lk.samson.coreflow.modules.warehouses.service.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse create(
            @Valid @RequestBody WarehouseRequest request) {
        return warehouseService.create(request);
    }

    @GetMapping
    public List<WarehouseResponse> getAll(
            @RequestParam(required = false) String search) {
        return warehouseService.getAll(search);
    }

    @GetMapping("/{id}")
    public WarehouseResponse getById(@PathVariable Long id) {
        return warehouseService.getById(id);
    }

    @PutMapping("/{id}")
    public WarehouseResponse update(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request) {
        return warehouseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        warehouseService.delete(id);
    }
}