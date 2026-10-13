
package lk.samson.coreflow.modules.uom.controller;

import jakarta.validation.Valid;

import lk.samson.coreflow.modules.uom.controller.request.UnitOfMeasureRequest;
import lk.samson.coreflow.modules.uom.controller.response.UnitOfMeasureResponse;
import lk.samson.coreflow.modules.uom.service.UnitOfMeasureService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/uom")
public class UnitOfMeasureController {

    private final UnitOfMeasureService service;

    public UnitOfMeasureController(UnitOfMeasureService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnitOfMeasureResponse create(
            @Valid @RequestBody UnitOfMeasureRequest request
    ) {
        return service.create(request);
    }

    @GetMapping
    public List<UnitOfMeasureResponse> getAll(
            @RequestParam(required = false) String search
    ) {
        return service.getAll(search);
    }

    @GetMapping("/{id}")
    public UnitOfMeasureResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public UnitOfMeasureResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UnitOfMeasureRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        service.deactivate(id);
    }
}