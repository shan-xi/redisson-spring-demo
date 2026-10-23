package com.example.stock.purchase;
public class ProductBusyException extends RuntimeException {
    public ProductBusyException(long id) { super("Product " + id + " is being processed; retry later"); }
}
