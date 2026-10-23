package com.example.stock.purchase;
public class IdempotencyKeyConflictException extends RuntimeException {
    public IdempotencyKeyConflictException(String message) { super(message); }
}
