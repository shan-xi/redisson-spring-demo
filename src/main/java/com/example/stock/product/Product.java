package com.example.stock.product;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false)
    private int stock;
    @Column(name = "fencing_token", nullable = false, columnDefinition = "bigint not null default 0")
    private long fencingToken = 0L;

    protected Product() {
    }

    public Product(String name, int stock) {
        if (stock < 0) throw new IllegalArgumentException("stock must be >= 0");
        this.name = name;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    public long getFencingToken() {
        return fencingToken;
    }
}
