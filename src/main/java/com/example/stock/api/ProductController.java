package com.example.stock.api;

import com.example.stock.product.Product;
import com.example.stock.purchase.PurchaseRequest;
import com.example.stock.purchase.PurchaseResponse;
import com.example.stock.purchase.StockPurchaseService;
import com.example.stock.purchase.StockPurchaseTransaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final StockPurchaseService purchases;
    private final StockPurchaseTransaction transaction;

    public ProductController(StockPurchaseService purchases, StockPurchaseTransaction transaction) {
        this.purchases = purchases;
        this.transaction = transaction;
    }

    public record CreateProductRequest(@NotBlank String name, @Min(0) int stock) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductView create(@Valid @RequestBody CreateProductRequest request) {
        Product p = transaction.createProduct(request.name(), request.stock());
        return new ProductView(p.getId(), p.getName(), p.getStock());
    }

    @GetMapping("/{id}")
    public ProductView get(@PathVariable long id) {
        Product p = transaction.getProduct(id);
        return new ProductView(p.getId(), p.getName(), p.getStock());
    }

    @PostMapping("/{id}/purchase")
    public PurchaseResponse purchase(@PathVariable long id,
                                     @RequestHeader("Idempotency-Key") String idempotencyKey,
                                     @Valid @RequestBody PurchaseRequest request) {
        return purchases.purchase(id, request.quantity(), idempotencyKey);
    }

    public record ProductView(long id, String name, int stock) {
    }
}
