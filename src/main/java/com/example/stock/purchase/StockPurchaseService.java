package com.example.stock.purchase;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class StockPurchaseService {
    private final RedissonClient redisson;
    private final StockPurchaseTransaction transaction;
    private final FencingTokenService tokens;

    public StockPurchaseService(RedissonClient redisson, StockPurchaseTransaction transaction, FencingTokenService tokens) {
        this.redisson = redisson;
        this.transaction = transaction;
        this.tokens = tokens;
    }

    public PurchaseResponse purchase(long productId, int quantity, String idempotencyKey) {
        PurchaseResponse previous = transaction.findExistingPurchase(idempotencyKey, productId, quantity);
        if (previous != null) return previous;
        RLock lock = redisson.getLock("lock:product-stock:" + productId);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(0, TimeUnit.SECONDS);
            if (!acquired) throw new ProductBusyException(productId);
            // Allocate only after acquiring the lock. A later lock holder receives a larger token.
            long token = tokens.nextToken();
            // Separate proxied bean: transaction commits/rolls back before this call returns.
            return transaction.purchase(productId, quantity, idempotencyKey, token);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PurchaseInterruptedException();
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
