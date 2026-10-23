package com.example.stock.purchase;

public record PurchaseResponse(long productId, int purchasedQuantity, String idempotencyKey, String message) {
}
