package com.example.stock.purchase;
public class PurchaseInterruptedException extends RuntimeException {
    public PurchaseInterruptedException() { super("Interrupted while acquiring product lock"); }
}
