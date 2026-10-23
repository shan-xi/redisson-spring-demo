package com.example.stock.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query(value = "INSERT INTO purchase_orders (idempotency_key, product_id, quantity, created_at) " +
            "VALUES (:key, :productId, :quantity, CURRENT_TIMESTAMP) ON CONFLICT (idempotency_key) DO NOTHING", nativeQuery = true)
    int insertIfKeyAvailable(@Param("key") String key, @Param("productId") long productId, @Param("quantity") int quantity);
}
