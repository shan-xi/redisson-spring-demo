package com.example.stock.purchase;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FencingTokenService {
    private final EntityManager entityManager;

    public FencingTokenService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // PostgreSQL sequence values are monotonically increasing; gaps are normal and safe.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long nextToken() {
        Number n = (Number) entityManager.createNativeQuery("select nextval('product_fencing_token_seq')").getSingleResult();
        return n.longValue();
    }
}
