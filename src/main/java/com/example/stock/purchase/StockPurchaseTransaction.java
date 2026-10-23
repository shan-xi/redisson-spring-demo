package com.example.stock.purchase;

import com.example.stock.product.Product;
import com.example.stock.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockPurchaseTransaction {
    private final ProductRepository products;
    private final PurchaseOrderRepository orders;

    public StockPurchaseTransaction(ProductRepository products, PurchaseOrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    @Transactional
    public PurchaseResponse purchase(long productId, int quantity, String idempotencyKey, long fencingToken) {
        validateKey(idempotencyKey);
        if (quantity < 1) throw new IllegalArgumentException("quantity must be at least 1");

        // Atomically claim the key. If it already exists, PostgreSQL inserts nothing.
        int inserted = orders.insertIfKeyAvailable(idempotencyKey, productId, quantity);
        if (inserted == 0) {
            PurchaseOrder old = orders.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotency key conflict, but purchase was not found"));
            return replayOrReject(old, productId, quantity);
        }

        // This update and the purchase_orders insert commit or roll back together.
        int updated = products.decreaseStockIfAvailableAndFenceIsNewer(productId, quantity, fencingToken);
        if (updated == 0) {
            if (!products.existsById(productId)) throw new ProductNotFoundException(productId);
            Product current = products.findById(productId).orElseThrow();
            if (current.getFencingToken() >= fencingToken) {
                throw new StaleFencingTokenException("Stale fencing token rejected; retry with the same Idempotency-Key");
            }
            throw new InsufficientStockException(productId, quantity);
        }
        return new PurchaseResponse(productId, quantity, idempotencyKey,
                "Purchase successful; fencing token accepted: " + fencingToken);
    }

    @Transactional(readOnly = true)
    public PurchaseResponse findExistingPurchase(String key, long productId, int quantity) {
        validateKey(key);
        return orders.findByIdempotencyKey(key).map(old -> replayOrReject(old, productId, quantity)).orElse(null);
    }

    private PurchaseResponse replayOrReject(PurchaseOrder old, long productId, int quantity) {
        if (old.getProductId() != productId || old.getQuantity() != quantity) {
            throw new IdempotencyKeyConflictException("This Idempotency-Key was already used with a different product or quantity");
        }
        return new PurchaseResponse(old.getProductId(), old.getQuantity(), old.getIdempotencyKey(),
                "Purchase already processed; returning the original result");
    }

    private static void validateKey(String key) {
        if (key == null || key.isBlank())
            throw new IllegalArgumentException("Idempotency-Key header must not be blank");
        if (key.length() > 100) throw new IllegalArgumentException("Idempotency-Key must be at most 100 characters");
    }

    @Transactional
    public Product createProduct(String name, int initialStock) {
        if (initialStock < 0) throw new IllegalArgumentException("initialStock must be >= 0");
        return products.save(new Product(name, initialStock));
    }

    @Transactional(readOnly = true)
    public Product getProduct(long productId) {
        return products.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
