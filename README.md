# Redisson + Spring Transaction + PostgreSQL + Idempotency

Stack: Java 21, Spring Boot 4.1.1, Redisson 4.8.0, Spring Data JPA, PostgreSQL, Redis.

## Start

```bash
docker compose up -d
mvn spring-boot:run
```

## Create a product

```bash
curl -i -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Keyboard","stock":10}'
```

## Purchase with an idempotency key

Use one unique key per logical purchase (a UUID is recommended):

```bash
curl -i -X POST http://localhost:8080/api/products/1/purchase \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: 2f04c2c5-7d7f-4c7b-8c55-123456789abc' \
  -d '{"quantity":3}'
```

Repeat the exact same request with the SAME key. The server returns the prior result and does not decrement stock again. Check stock with `curl http://localhost:8080/api/products/1`; it should remain 7 after replay. Use a NEW key for a different purchase. Reusing a key for a different product or quantity returns HTTP 409.

## How it works

- `purchase_orders.idempotency_key` is `VARCHAR(100) NOT NULL UNIQUE`.
- `StockPurchaseService` checks for an already completed request before locking.
- PostgreSQL `INSERT ... ON CONFLICT (idempotency_key) DO NOTHING` atomically claims a key.
- The order insert and conditional stock decrement run in the same `@Transactional` transaction. If stock is insufficient or the product is missing, both changes roll back.
- A repeated key with the same product and quantity returns the previous result without decrementing stock again.
- If a duplicate arrives while the first request is still holding the product lock, it may receive HTTP 429. Retry with the SAME key; after the first transaction commits, the retry returns the stored result.

## Endpoints and status codes

- `POST /api/products`: create product.
- `GET /api/products/{id}`: read stock.
- `POST /api/products/{id}/purchase`: purchase; requires `Idempotency-Key`.
- `201`: product created; `200`: purchase success or replay.
- `400`: missing/invalid input; `404`: product not found; `409`: insufficient stock or key reused with different parameters; `429`: product is busy, retry with the same key.

## Production notes

This is a teaching example. Production systems should scope keys to an authenticated customer/account, define key-retention policy, persist complete order/payment state, and use Flyway/Liquibase rather than `ddl-auto: update`. A Redis lock and a database transaction are not one distributed transaction.


## Fencing token + database conditional update

This revision adds `products.fencing_token` and a PostgreSQL sequence `product_fencing_token_seq`. After a request acquires the per-product Redisson lock, it obtains a monotonically increasing token from PostgreSQL. The stock update succeeds only if `stock >= quantity` AND the stored token is lower than the request token; the same atomic update decrements stock and stores the accepted token. A stale lock holder cannot write after a newer token has already been accepted.

`src/main/resources/schema.sql` creates the sequence at startup (`spring.sql.init.mode: always`). For an existing database, make sure the sequence exists (`CREATE SEQUENCE IF NOT EXISTS product_fencing_token_seq START WITH 1 INCREMENT BY 1;`). Hibernate schema update adds the `fencing_token` column; its default is 0 for existing rows. For production, use a Flyway/Liquibase migration to create the column and sequence explicitly.

Fencing does not replace the stock predicate: both checks must remain in the database update.
