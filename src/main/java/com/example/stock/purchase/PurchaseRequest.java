package com.example.stock.purchase;

import jakarta.validation.constraints.Min;

public record PurchaseRequest(@Min(1) int quantity) {}
