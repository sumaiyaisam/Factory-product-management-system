
package lk.samson.coreflow.modules.locations.controller;

import jakarta.validation.Valid;
import lk.samson.coreflow.modules.locations.controller.request.StorageLocationRequest;
import lk.samson.coreflow.modules.locations.controller.response.StorageLocationResponse;
import lk.samson.coreflow.modules.locations.service.StorageLocationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class StorageLocationController {

    private final StorageLocationService locationService;

    public StorageLocationController(
            StorageLocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StorageLocationResponse create(
            @Valid @RequestBody StorageLocationRequest request) {
        return locationService.create(request);
    }

    @GetMapping
    public List<StorageLocationResponse> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long warehouseId) {
        return locationService.getAll(search, warehouseId);
    }

    @GetMapping("/{id}")
    public StorageLocationResponse getById(@PathVariable Long id) {
        return locationService.getById(id);
    }

    @PutMapping("/{id}")
    public StorageLocationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody StorageLocationRequest request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }
}