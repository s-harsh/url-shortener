# CLAUDE.md — URL Shortener Project Guide

## Project Overview
Production-grade URL shortening service. Spring Boot 3.2 + Redis + PostgreSQL.

## Quick Start
```bash
docker compose up --build
# App: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

## Architecture
- **Hot path** (redirect): Redis cache → 302 redirect. Sub-5ms.
- **Write path** (shorten): Redis INCR → Base62 code → PostgreSQL + Redis cache.
- **Analytics**: Fully async `@Async`, never blocks redirect.
- **Rate limiting**: Bucket4j token-bucket per IP in servlet filter.

## Key Directories
- `src/main/java/com/urlshortener/service/impl/` — all business logic
- `src/main/java/com/urlshortener/util/Base62Encoder.java` — short code algorithm
- `src/main/resources/db/migration/` — Flyway SQL migrations (edit these to change schema)
- `src/main/resources/application.yml` — main config; env-var overrides for prod

## Common Tasks

### Add a new endpoint
1. Add method to `UrlService` interface
2. Implement in `UrlServiceImpl`
3. Add `@Operation` annotated method in `UrlController`

### Change the short code length
Set `app.short-code-length` in `application.yml` or via env var.

### Add a new database column
1. Create `V{N}__description.sql` in `db/migration/`
2. Add the field to the JPA entity
3. Flyway applies automatically on next startup

## Testing
```bash
mvn test          # unit tests (H2 in-memory, no Docker)
mvn verify        # full build + tests
```

## Build
```bash
mvn package -DskipTests        # build fat jar
docker build -t url-shortener .  # build Docker image
```

## Deployment
See `docs/DEPLOYMENT.md` for Railway, Render, Fly.io, and VPS guides.
