package com.example.stock.purchase;
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(long id, int quantity) {
        super("Insufficient stock for product " + id + "; requested quantity=" + quantity);
    }
}
