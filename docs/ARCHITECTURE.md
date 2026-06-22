# Architecture Decision Record

## System Overview

```
+----------------------------------------------------------------+
|                        URL Shortener                           |
|                                                                |
|  +-----------+    +-----------------+    +------------------+  |
|  |  Browser  |--->|  Spring Boot    |--->|  Redis (Cache)   |  |
|  |  / API    |    |  (Port 8080)    |    |  (Port 6379)     |  |
|  +-----------+    +--------+--------+    +------------------+  |
|                            |                                   |
|                            v                                   |
|                   +----------------+                           |
|                   |  PostgreSQL    |                           |
|                   |  (Port 5432)   |                           |
|                   +----------------+                           |
+----------------------------------------------------------------+
```

## ADR-001: Base62 Short Code Generation

**Decision:** Use atomic Redis INCR counter + Base62 encoding.

**Rationale:**
- Atomic counter guarantees uniqueness without DB roundtrips
- Base62 (0-9, a-z, A-Z) produces URL-safe, human-readable codes
- 7 characters = 62^7 ≈ 3.5 trillion unique codes
- Counter survives restarts (Redis persistence)

**Trade-offs:**
- Counter is predictable (not random). Codes are sequential, not guessable for specific targets, but a determined attacker can enumerate. Mitigated by rate limiting.
- Redis outage falls back to `System.nanoTime()` XOR'd with random — collision probability is very low.

## ADR-002: Redis as L1 Cache

**Decision:** Cache resolved short codes in Redis with URL expiry as TTL.

**Strategy:** Write-through on create, read-through on redirect.

**Why:**
- Redirect is the hot path (>90% of traffic). Redis hits are sub-millisecond.
- PostgreSQL reserved for writes and cache misses.
- If Redis is down, the app degrades gracefully — all operations fall through to PostgreSQL.

**Cache keys:**
- `url:{shortCode}` → original URL string

## ADR-003: Async Analytics

**Decision:** Click recording is fully async via Spring's `@Async`.

**Rationale:**
- Recording a click must not add latency to the redirect (user experience).
- A failed analytics write must never fail the redirect.
- Background threads handle DB writes independently.

**Trade-off:** Clicks may be lost if the JVM crashes between redirect and async write. Acceptable for an analytics use case (not financial data).

## ADR-004: Soft Delete

**Decision:** Deleting a URL sets `active = false` rather than removing the row.

**Rationale:**
- Preserves click_events foreign key integrity.
- Allows audit trails and potential restore.
- Expired URLs are also effectively soft-deleted (checked in `isExpired()`).

## ADR-005: Rate Limiting with Bucket4j

**Decision:** Use Bucket4j token-bucket algorithm in a servlet filter.

**Default config:** 100 requests / minute per IP.

**Production note:** The in-memory `ConcurrentHashMap` bucket store works for a single instance. For multi-instance deployments, replace with the `bucket4j-redis` backend for distributed rate limiting.

## ADR-006: Flyway Database Migrations

**Decision:** All schema changes are managed by Flyway version-controlled SQL scripts.

**Benefits:**
- Reproducible schema across all environments
- Safe incremental upgrades (no `ddl-auto: create-drop` in prod)
- Baseline-on-migrate supports existing databases

## Performance Characteristics

| Endpoint | Latency (p50) | Notes |
|----------|--------------|-------|
| `GET /{code}` (cache hit) | < 5ms | Redis only |
| `GET /{code}` (cache miss) | < 20ms | Redis + PostgreSQL |
| `POST /api/v1/urls` | < 30ms | Redis INCR + PostgreSQL write |
| `GET /api/v1/urls/{code}/stats` | < 50ms | PostgreSQL aggregate query |

## Scaling Strategy

**Horizontal scaling:** Add more Spring Boot instances behind a load balancer (stateless — all state in Redis and PostgreSQL).

**Database:** PostgreSQL read replicas for analytics queries.

**Cache:** Redis Cluster for larger datasets; Bucket4j-Redis for distributed rate limiting.
