
package lk.samson.coreflow.modules.locations.service.impl;

import lk.samson.coreflow.modules.locations.controller.request.StorageLocationRequest;
import lk.samson.coreflow.modules.locations.controller.response.StorageLocationResponse;
import lk.samson.coreflow.modules.locations.entity.StorageLocation;
import lk.samson.coreflow.modules.locations.entity.LocationType;
import lk.samson.coreflow.modules.locations.repository.StorageLocationRepository;
import lk.samson.coreflow.modules.locations.service.StorageLocationService;
import lk.samson.coreflow.modules.warehouses.entity.Warehouse;
import lk.samson.coreflow.modules.warehouses.repository.WarehouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class StorageLocationServiceImpl implements StorageLocationService {

    private final StorageLocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;

    public StorageLocationServiceImpl(
            StorageLocationRepository locationRepository,
            WarehouseRepository warehouseRepository) {
        this.locationRepository = locationRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public StorageLocationResponse create(StorageLocationRequest request) {
        validateRequest(request);

        Warehouse warehouse = findActiveWarehouse(request.getWarehouseId());
        String code = normalizeCode(request.getCode());

        if (locationRepository.existsByWarehouse_IdAndCodeIgnoreCase(
                warehouse.getId(), code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Location code already exists in this warehouse: " + code
            );
        }

        StorageLocation location = new StorageLocation();
        mapRequestToEntity(request, location, warehouse, true);

        return mapEntityToResponse(locationRepository.save(location));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageLocationResponse> getAll(
            String search,
            Long warehouseId) {

        if (warehouseId != null && warehouseId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Warehouse ID must be positive"
            );
        }

        String keyword = search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);

        return locationRepository.findAll()
                .stream()
                .filter(location ->
                        (warehouseId == null
                                || location.getWarehouse().getId().equals(warehouseId))
                                && (keyword.isEmpty()
                                || contains(location.getCode(), keyword)
                                || contains(location.getName(), keyword)
                                || contains(location.getDescription(), keyword)
                                || contains(location.getLocationType().name(), keyword)
                                || contains(location.getWarehouse().getCode(), keyword)
                                || contains(location.getWarehouse().getName(), keyword))
                )
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StorageLocationResponse getById(Long id) {
        return mapEntityToResponse(findLocationById(id));
    }

    @Override
    public StorageLocationResponse update(
            Long id,
            StorageLocationRequest request) {

        validateRequest(request);

        StorageLocation location = findLocationById(id);
        Warehouse warehouse = findActiveWarehouse(request.getWarehouseId());
        String code = normalizeCode(request.getCode());

        if (locationRepository.existsByWarehouse_IdAndCodeIgnoreCaseAndIdNot(
                warehouse.getId(), code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Location code already exists in this warehouse: " + code
            );
        }

        mapRequestToEntity(request, location, warehouse, false);

        return mapEntityToResponse(locationRepository.save(location));
    }

    @Override
    public void delete(Long id) {
        StorageLocation location = findLocationById(id);
        location.setActive(false);
        locationRepository.save(location);
    }

    private void validateRequest(StorageLocationRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        if (request.getWarehouseId() == null
                || request.getWarehouseId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid warehouse ID is required"
            );
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Location code is required"
            );
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Location name is required"
            );
        }

        if (request.getLocationType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Location type is required"
            );
        }
    }

    private Warehouse findActiveWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Warehouse not found with ID: " + warehouseId
                ));

        if (!warehouse.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign a location to an inactive warehouse"
            );
        }

        return warehouse;
    }

    private StorageLocation findLocationById(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Location ID must be positive"
            );
        }

        return locationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Storage location not found with ID: " + id
                ));
    }

    private void mapRequestToEntity(
            StorageLocationRequest request,
            StorageLocation location,
            Warehouse warehouse,
            boolean creating) {

        location.setWarehouse(warehouse);
        location.setCode(normalizeCode(request.getCode()));
        location.setName(request.getName().trim());
        location.setDescription(trimToNull(request.getDescription()));
        location.setLocationType(request.getLocationType());

        if (request.getActive() != null) {
            location.setActive(request.getActive());
        } else if (creating) {
            location.setActive(true);
        }
    }

    private StorageLocationResponse mapEntityToResponse(
            StorageLocation location) {

        StorageLocationResponse response = new StorageLocationResponse();

        response.setId(location.getId());

        if (location.getWarehouse() != null) {
            response.setWarehouseId(location.getWarehouse().getId());
            response.setWarehouseCode(location.getWarehouse().getCode());
            response.setWarehouseName(location.getWarehouse().getName());
        }

        response.setCode(location.getCode());
        response.setName(location.getName());
        response.setDescription(location.getDescription());
        response.setLocationType(location.getLocationType());
        response.setActive(location.isActive());
        response.setCreatedAt(location.getCreatedAt());
        response.setUpdatedAt(location.getUpdatedAt());

        return response;
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
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