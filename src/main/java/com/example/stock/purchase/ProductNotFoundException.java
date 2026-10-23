package com.example.stock.purchase;
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(long id) { super("Product " + id + " was not found"); }
}
