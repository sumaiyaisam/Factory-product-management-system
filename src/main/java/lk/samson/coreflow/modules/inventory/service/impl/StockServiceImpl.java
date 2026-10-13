
package lk.samson.coreflow.modules.inventory.service.impl;

import lk.samson.coreflow.modules.inventory.controller.request.StockMovementRequest;
import lk.samson.coreflow.modules.inventory.controller.response.StockBalanceResponse;
import lk.samson.coreflow.modules.inventory.controller.response.StockTransactionResponse;
import lk.samson.coreflow.modules.inventory.entity.StockBalance;
import lk.samson.coreflow.modules.inventory.entity.StockMovementType;
import lk.samson.coreflow.modules.inventory.entity.StockTransaction;
import lk.samson.coreflow.modules.inventory.repository.StockBalanceRepository;
import lk.samson.coreflow.modules.inventory.repository.StockTransactionRepository;
import lk.samson.coreflow.modules.inventory.service.StockService;
import lk.samson.coreflow.modules.locations.entity.StorageLocation;
import lk.samson.coreflow.modules.locations.repository.StorageLocationRepository;
import lk.samson.coreflow.modules.products.entity.Product;
import lk.samson.coreflow.modules.products.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class StockServiceImpl implements StockService {

    private final StockBalanceRepository balanceRepository;
    private final StockTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final StorageLocationRepository locationRepository;

    public StockServiceImpl(
            StockBalanceRepository balanceRepository,
            StockTransactionRepository transactionRepository,
            ProductRepository productRepository,
            StorageLocationRepository locationRepository) {
        this.balanceRepository = balanceRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public StockTransactionResponse createMovement(
            StockMovementRequest request) {

        validateRequest(request);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with ID: " + request.getProductId()
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot move stock for an inactive product"
            );
        }

        StorageLocation location =
                locationRepository.findById(request.getLocationId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Storage location not found with ID: "
                                        + request.getLocationId()
                        ));

        if (!location.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot move stock through an inactive location"
            );
        }

        if (!location.getWarehouse().isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The location's warehouse is inactive"
            );
        }

        StockBalance balance = balanceRepository
                .findForUpdate(product.getId(), location.getId())
                .orElseGet(() -> createInitialBalance(product, location));

        BigDecimal current = balance.getQuantity();
        BigDecimal movement = request.getQuantity();
        BigDecimal updated;

        switch (request.getMovementType()) {
            case RECEIPT, ADJUSTMENT_IN ->
                    updated = current.add(movement);

            case ISSUE, ADJUSTMENT_OUT -> {
                if (current.compareTo(movement) < 0) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Insufficient stock. Available: " + current
                                    + ", requested: " + movement
                    );
                }
                updated = current.subtract(movement);
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported stock movement type"
            );
        }

        balance.setQuantity(updated);
        balanceRepository.save(balance);

        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setLocation(location);
        transaction.setMovementType(request.getMovementType());
        transaction.setQuantity(movement);
        transaction.setBalanceAfter(updated);
        transaction.setReferenceNumber(trimToNull(request.getReferenceNumber()));
        transaction.setReason(request.getReason().trim());

        StockTransaction saved = transactionRepository.save(transaction);

        return mapTransactionToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockBalanceResponse> getBalances(
            Long productId,
            Long locationId) {

        validateOptionalIds(productId, locationId);

        return balanceRepository.findAll()
                .stream()
                .filter(balance ->
                        (productId == null
                                || balance.getProduct().getId().equals(productId))
                                && (locationId == null
                                || balance.getLocation().getId().equals(locationId))
                )
                .map(this::mapBalanceToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getTransactions(
            Long productId,
            Long locationId) {

        validateOptionalIds(productId, locationId);

        List<StockTransaction> transactions;

        if (productId != null && locationId != null) {
            transactions = transactionRepository
                    .findByProduct_IdAndLocation_IdOrderByCreatedAtDesc(
                            productId, locationId
                    );
        } else if (productId != null) {
            transactions = transactionRepository
                    .findByProduct_IdOrderByCreatedAtDesc(productId);
        } else if (locationId != null) {
            transactions = transactionRepository
                    .findByLocation_IdOrderByCreatedAtDesc(locationId);
        } else {
            transactions = transactionRepository
                    .findAllByOrderByCreatedAtDesc();
        }

        return transactions.stream()
                .map(this::mapTransactionToResponse)
                .toList();
    }

    private StockBalance createInitialBalance(
            Product product,
            StorageLocation location) {

        StockBalance balance = new StockBalance();
        balance.setProduct(product);
        balance.setLocation(location);
        balance.setQuantity(BigDecimal.ZERO);

        return balanceRepository.saveAndFlush(balance);
    }

    private void validateRequest(StockMovementRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body is required"
            );
        }

        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid product ID is required"
            );
        }

        if (request.getLocationId() == null || request.getLocationId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid location ID is required"
            );
        }

        if (request.getMovementType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Movement type is required"
            );
        }

        BigDecimal quantity = request.getQuantity();

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        if (quantity.stripTrailingZeros().scale() > 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity cannot have more than 3 decimal places"
            );
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reason is required"
            );
        }
    }

    private void validateOptionalIds(Long productId, Long locationId) {
        if (productId != null && productId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID must be positive"
            );
        }

        if (locationId != null && locationId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Location ID must be positive"
            );
        }
    }

    private StockBalanceResponse mapBalanceToResponse(
            StockBalance balance) {

        StockBalanceResponse response = new StockBalanceResponse();
        Product product = balance.getProduct();
        StorageLocation location = balance.getLocation();

        response.setId(balance.getId());
        response.setProductId(product.getId());
        response.setProductSku(product.getSku());
        response.setProductName(product.getName());

        if (product.getUnitOfMeasure() != null) {
            response.setUomCode(product.getUnitOfMeasure().getCode());
        }

        response.setWarehouseId(location.getWarehouse().getId());
        response.setWarehouseCode(location.getWarehouse().getCode());
        response.setWarehouseName(location.getWarehouse().getName());

        response.setLocationId(location.getId());
        response.setLocationCode(location.getCode());
        response.setLocationName(location.getName());

        response.setQuantity(balance.getQuantity());

        return response;
    }

    private StockTransactionResponse mapTransactionToResponse(
            StockTransaction transaction) {

        StockTransactionResponse response =
                new StockTransactionResponse();

        Product product = transaction.getProduct();
        StorageLocation location = transaction.getLocation();

        response.setId(transaction.getId());

        response.setProductId(product.getId());
        response.setProductSku(product.getSku());
        response.setProductName(product.getName());

        response.setWarehouseId(location.getWarehouse().getId());
        response.setWarehouseCode(location.getWarehouse().getCode());
        response.setWarehouseName(location.getWarehouse().getName());

        response.setLocationId(location.getId());
        response.setLocationCode(location.getCode());
        response.setLocationName(location.getName());

        response.setMovementType(transaction.getMovementType());
        response.setQuantity(transaction.getQuantity());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setReason(transaction.getReason());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}