package com.example.stock.purchase;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "purchase_orders", uniqueConstraints = @UniqueConstraint(name = "uk_purchase_orders_idempotency_key", columnNames = "idempotency_key"))
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;
    @Column(name = "product_id", nullable = false)
    private long productId;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PurchaseOrder() {
    }

    public PurchaseOrder(String key, long productId, int quantity) {
        this.idempotencyKey = key;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
