# URL Shortener

A production-grade URL shortening service built with **Spring Boot 3**, **Redis**, and **PostgreSQL**.

## Features

- Shorten any HTTP/HTTPS URL to a 7-character code
- Custom aliases (`/my-brand`)
- Configurable TTL per link (1–365 days)
- Click analytics with daily breakdown (last 30 days)
- Redis-backed caching — sub-millisecond redirects
- Distributed rate limiting (Bucket4j)
- Flyway database migrations
- OpenAPI / Swagger UI at `/swagger-ui.html`
- Docker Compose for one-command local setup
- GitHub Actions CI pipeline
- Ready to deploy free on Railway, Render, or Fly.io

## Architecture

```
Browser / Client
      │
      ▼
Spring Boot App  ──── Redis (L1 cache, rate limiter counters)
      │
      ▼
PostgreSQL (source of truth)
```

**Redirect path (hot):** `GET /{code}` → Redis lookup → 302 redirect
**Analytics:** async fire-and-forget via `@Async` — never blocks redirects
**Short code generation:** atomic Redis INCR → Base62 encode (≈ 3.5T unique codes)

## Quick Start (Docker Compose)

```bash
git clone <repo>
cd url-shortener
docker compose up --build
```

- App:       http://localhost:8080
- Swagger:   http://localhost:8080/swagger-ui.html
- Health:    http://localhost:8080/actuator/health

## API Reference

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/urls` | Shorten a URL |
| `GET`  | `/{shortCode}` | Redirect to original URL |
| `GET`  | `/api/v1/urls/{shortCode}` | Get URL metadata |
| `GET`  | `/api/v1/urls/{shortCode}/stats` | Get click analytics |
| `DELETE` | `/api/v1/urls/{shortCode}` | Soft-delete a URL |

### Shorten a URL

```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.example.com/very/long/path/that/is/hard/to/share",
    "customAlias": "my-link",
    "ttlDays": 30
  }'
```

Response:
```json
{
  "shortCode": "my-link",
  "shortUrl": "http://localhost:8080/my-link",
  "originalUrl": "https://www.example.com/...",
  "createdAt": "2024-01-15T10:30:00",
  "expiresAt": "2024-02-14T10:30:00"
}
```

## Local Development (without Docker)

Prerequisites: Java 21, Maven, PostgreSQL, Redis

```bash
# 1. Start PostgreSQL and Redis locally
# PostgreSQL: createdb urlshortener_dev
# Redis: redis-server

# 2. Run the app
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DATABASE_URL` | Yes (prod) | `jdbc:postgresql://localhost:5432/urlshortener` | PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | Yes (prod) | `postgres` | DB username |
| `DATABASE_PASSWORD` | Yes (prod) | `postgres` | DB password |
| `REDIS_HOST` | Yes (prod) | `localhost` | Redis hostname |
| `REDIS_PORT` | No | `6379` | Redis port |
| `REDIS_PASSWORD` | No | `` | Redis password (if auth enabled) |
| `REDIS_SSL` | No | `false` | Enable Redis TLS |
| `APP_BASE_URL` | Yes (prod) | `http://localhost:8080` | Public base URL for short links |
| `SPRING_PROFILES_ACTIVE` | No | `dev` | Spring profile (`dev` / `prod`) |

## Deployment

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for step-by-step guides for Railway, Render, and Fly.io.

## Project Structure

```
src/
├── main/java/com/urlshortener/
│   ├── config/          # Redis, OpenAPI, AppProperties
│   ├── controller/      # UrlController, RedirectController
│   ├── dto/             # Request/Response DTOs
│   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   ├── filter/          # RateLimitFilter (Bucket4j)
│   ├── model/           # JPA entities (Url, ClickEvent)
│   ├── repository/      # Spring Data JPA repositories
│   ├── service/         # Interfaces + implementations
│   └── util/            # Base62Encoder, UrlValidator
└── main/resources/
    ├── application.yml
    ├── application-dev.yml
    ├── application-prod.yml
    └── db/migration/    # Flyway SQL migrations
```

## Running Tests

```bash
mvn test
```

Tests use H2 in-memory database and mock Redis — no external services needed.

## License

MIT
