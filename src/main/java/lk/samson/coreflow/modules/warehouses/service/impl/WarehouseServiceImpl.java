
package lk.samson.coreflow.modules.warehouses.service.impl;

import lk.samson.coreflow.modules.warehouses.controller.request.WarehouseRequest;
import lk.samson.coreflow.modules.warehouses.controller.response.WarehouseResponse;
import lk.samson.coreflow.modules.warehouses.entity.Warehouse;
import lk.samson.coreflow.modules.warehouses.entity.WarehouseType;
import lk.samson.coreflow.modules.warehouses.repository.WarehouseRepository;
import lk.samson.coreflow.modules.warehouses.service.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public WarehouseResponse create(WarehouseRequest request) {
        validateRequest(request);

        String code = request.getCode().trim();

        if (warehouseRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Warehouse code already exists: " + code
            );
        }

        Warehouse warehouse = new Warehouse();
        mapRequestToEntity(request, warehouse, true);

        return mapEntityToResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAll(String search) {
        String keyword = search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);

        return warehouseRepository.findAll()
                .stream()
                .filter(warehouse ->
                        keyword.isEmpty()
                                || contains(warehouse.getCode(), keyword)
                                || contains(warehouse.getName(), keyword)
                                || contains(warehouse.getDescription(), keyword)
                                || contains(warehouse.getAddress(), keyword)
                                || contains(warehouse.getCity(), keyword)
                                || (warehouse.getWarehouseType() != null
                                && contains(
                                warehouse.getWarehouseType().name(),
                                keyword
                        ))
                )
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getById(Long id) {
        return mapEntityToResponse(findWarehouseById(id));
    }

    @Override
    public WarehouseResponse update(Long id, WarehouseRequest request) {
        validateRequest(request);

        Warehouse warehouse = findWarehouseById(id);
        String code = request.getCode().trim();

        if (warehouseRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Warehouse code already exists: " + code
            );
        }

        mapRequestToEntity(request, warehouse, false);

        return mapEntityToResponse(warehouseRepository.save(warehouse));
    }

    @Override
    public void delete(Long id) {
        Warehouse warehouse = findWarehouseById(id);
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    private void validateRequest(WarehouseRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Warehouse code is required"
            );
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Warehouse name is required"
            );
        }

        if (request.getWarehouseType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Warehouse type is required"
            );
        }
    }

    private Warehouse findWarehouseById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Warehouse ID must be positive"
            );
        }

        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Warehouse not found with ID: " + id
                ));
    }

    private void mapRequestToEntity(
            WarehouseRequest request,
            Warehouse warehouse,
            boolean creating) {

        warehouse.setCode(request.getCode().trim().toUpperCase(Locale.ROOT));
        warehouse.setName(request.getName().trim());
        warehouse.setDescription(trimToNull(request.getDescription()));
        warehouse.setAddress(trimToNull(request.getAddress()));
        warehouse.setCity(trimToNull(request.getCity()));
        warehouse.setWarehouseType(request.getWarehouseType());

        if (request.getActive() != null) {
            warehouse.setActive(request.getActive());
        } else if (creating) {
            warehouse.setActive(true);
        }
    }

    private WarehouseResponse mapEntityToResponse(Warehouse warehouse) {
        WarehouseResponse response = new WarehouseResponse();

        response.setId(warehouse.getId());
        response.setCode(warehouse.getCode());
        response.setName(warehouse.getName());
        response.setDescription(warehouse.getDescription());
        response.setAddress(warehouse.getAddress());
        response.setCity(warehouse.getCity());
        response.setWarehouseType(warehouse.getWarehouseType());
        response.setActive(warehouse.isActive());
        response.setCreatedAt(warehouse.getCreatedAt());
        response.setUpdatedAt(warehouse.getUpdatedAt());

        return response;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean contains(String value, String keyword) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}