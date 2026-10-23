package com.example.stock.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p " +
            "set p.stock=p.stock-:quantity, " +
            "p.fencingToken=:token " +
            "where p.id=:id and p.stock>=:quantity and p.fencingToken<:token")
    int decreaseStockIfAvailableAndFenceIsNewer(@Param("id") long id, @Param("quantity") int quantity, @Param("token") long token);
}
