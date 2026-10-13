
package lk.samson.coreflow.modules.uom.service.impl;

import lk.samson.coreflow.modules.uom.controller.request.UnitOfMeasureRequest;
import lk.samson.coreflow.modules.uom.controller.response.UnitOfMeasureResponse;
import lk.samson.coreflow.modules.uom.entity.UnitOfMeasure;
import lk.samson.coreflow.modules.uom.repository.UnitOfMeasureRepository;
import lk.samson.coreflow.modules.uom.service.UnitOfMeasureService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private final UnitOfMeasureRepository repository;

    public UnitOfMeasureServiceImpl(UnitOfMeasureRepository repository) {
        this.repository = repository;
    }

    @Override
    public UnitOfMeasureResponse create(UnitOfMeasureRequest request) {

        validateRequest(request);

        String code = normalizeCode(request.getCode());
        String name = request.getName().trim();

        if (repository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unit code already exists: " + code
            );
        }

        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unit name already exists: " + name
            );
        }

        UnitOfMeasure unit = new UnitOfMeasure();
        mapRequestToEntity(request, unit);

        return mapEntityToResponse(repository.save(unit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasureResponse> getAll(String search) {

        String keyword = search == null ? "" : search.trim()
                .toLowerCase(Locale.ROOT);

        return repository.findAll().stream()
                .filter(unit -> keyword.isEmpty()
                        || contains(unit.getCode(), keyword)
                        || contains(unit.getName(), keyword)
                        || contains(unit.getDescription(), keyword))
                .map(this::mapEntityToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasureResponse getById(Long id) {
        return mapEntityToResponse(findById(id));
    }

    @Override
    public UnitOfMeasureResponse update(
            Long id,
            UnitOfMeasureRequest request
    ) {
        validateRequest(request);

        UnitOfMeasure unit = findById(id);

        String code = normalizeCode(request.getCode());
        String name = request.getName().trim();

        if (repository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unit code already exists: " + code
            );
        }

        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unit name already exists: " + name
            );
        }

        mapRequestToEntity(request, unit);

        return mapEntityToResponse(repository.save(unit));
    }

    @Override
    public void deactivate(Long id) {

        UnitOfMeasure unit = findById(id);
        unit.setActive(false);
        repository.save(unit);
    }

    private UnitOfMeasure findById(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unit ID must be a positive number"
            );
        }

        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unit of measure not found with ID: " + id
                ));
    }

    private void validateRequest(UnitOfMeasureRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unit code is required"
            );
        }

        if (request.getCode().trim().length() > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unit code cannot exceed 20 characters"
            );
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unit name is required"
            );
        }

        if (request.getName().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unit name cannot exceed 100 characters"
            );
        }

        if (request.getDescription() != null
                && request.getDescription().length() > 255) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Description cannot exceed 255 characters"
            );
        }
    }

    private void mapRequestToEntity(
            UnitOfMeasureRequest request,
            UnitOfMeasure unit
    ) {
        unit.setCode(normalizeCode(request.getCode()));
        unit.setName(request.getName().trim());

        String description = request.getDescription();
        unit.setDescription(
                description == null || description.isBlank()
                        ? null
                        : description.trim()
        );
    }

    private UnitOfMeasureResponse mapEntityToResponse(
            UnitOfMeasure unit
    ) {
        UnitOfMeasureResponse response = new UnitOfMeasureResponse();

        response.setId(unit.getId());
        response.setCode(unit.getCode());
        response.setName(unit.getName());
        response.setDescription(unit.getDescription());
        response.setActive(unit.isActive());
        response.setCreatedAt(unit.getCreatedAt());
        response.setUpdatedAt(unit.getUpdatedAt());

        return response;
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private boolean contains(String value, String keyword) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}