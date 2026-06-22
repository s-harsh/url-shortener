<div align="center">

# 🔗 URL Shortener

**Production-grade URL shortener built with Spring Boot 3, Redis & PostgreSQL**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

[**Live Demo**](https://github.com/s-harsh/url-shortener) · [**API Docs**](https://github.com/s-harsh/url-shortener) · [**Report Bug**](https://github.com/s-harsh/url-shortener/issues) · [**Request Feature**](https://github.com/s-harsh/url-shortener/issues)

</div>

---

## ✨ Why This Project Stands Out

Most URL shortener tutorials show you a toy app with an H2 database and a HashMap. **This one is different** — built the way it would be done at a real company:

- ⚡ **Sub-5ms redirects** — Redis L1 cache means PostgreSQL is never hit on the hot path
- 🔢 **3.5 trillion unique codes** — Base62 encoding of an atomic Redis INCR counter
- 📊 **Zero-latency analytics** — click recording is fully `@Async`, never blocks the redirect
- 🛡️ **Rate limiting** — Bucket4j token-bucket per IP, gracefully rejects abusers
- 🐟 **Stunning 3D homepage** — Three.js boid fish simulation with flocking AI
- 🐳 **One command to run** — `docker compose up --build` and everything works

---

## 🚀 Quick Start

```bash
git clone https://github.com/s-harsh/url-shortener.git
cd url-shortener
docker compose up --build
```

| Service | URL |
|---------|-----|
| 🏠 Homepage (3D Fish Scene) | http://localhost:8080 |
| 📖 Swagger / API Docs | http://localhost:8080/swagger-ui.html |
| ❤️ Health Check | http://localhost:8080/actuator/health |

> **Only requirement:** Docker Desktop. No Java or databases needed locally.

---

## 🏗️ Architecture

```
                    ┌──────────────────────────────────────────┐
                    │             URL Shortener                │
                    │                                          │
  Browser ────────► │   Spring Boot 3.2  ◄──►  Redis (Cache)  │
                    │      Port 8080          Port 6379        │
                    │          │                               │
                    │          ▼                               │
                    │    PostgreSQL 16  (source of truth)      │
                    │      Port 5432                           │
                    └──────────────────────────────────────────┘
```

**Redirect hot path (< 5ms):**
```
GET /{code} → Redis HIT → 302 Redirect ✅
             → Redis MISS → PostgreSQL → cache backfill → 302 Redirect
```

**Write path:**
```
POST /api/v1/urls → validate → Redis INCR → Base62 → PostgreSQL + Redis cache
```

**Analytics (never blocks redirects):**
```
Redirect fires → @Async thread → INSERT click_event + UPDATE click_count
```

---

## ⚡ Features

| Feature | Details |
|---------|---------|
| 🔗 **URL Shortening** | Base62 short codes, 7 chars, 3.5T unique capacity |
| 🏷️ **Custom Aliases** | `/my-brand`, `/launch-day`, `/portfolio` |
| ⏱️ **Expiry TTL** | Per-link expiry from 1 to 365 days |
| 📊 **Click Analytics** | Total clicks + daily breakdown (last 30 days) |
| ⚡ **Redis Caching** | Write-through cache, sub-millisecond reads |
| 🛡️ **Rate Limiting** | Bucket4j token-bucket, 100 req/min per IP |
| 🗄️ **DB Migrations** | Flyway versioned SQL — safe schema upgrades |
| 🐟 **3D Homepage** | Three.js fish boid simulation (120 fish, real flocking AI) |
| 📖 **OpenAPI Docs** | Full Swagger UI, try every endpoint in browser |
| 🐳 **Docker Ready** | `docker compose up` — PostgreSQL + Redis + App |
| ✅ **GitHub Actions CI** | Tests run on every push |
| 🚀 **Free Deploy** | Railway / Render / Fly.io guides included |

---

## 📡 API Reference

### Shorten a URL
```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"url": "https://very-long-url.com/path?query=param", "customAlias": "my-link", "ttlDays": 30}'
```
```json
{
  "shortCode": "my-link",
  "shortUrl": "http://localhost:8080/my-link",
  "originalUrl": "https://very-long-url.com/path?query=param",
  "createdAt": "2024-01-15T10:30:00",
  "expiresAt":  "2024-02-14T10:30:00"
}
```

### All Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/urls` | Shorten a URL |
| `GET` | `/{shortCode}` | **302 redirect** to original URL |
| `GET` | `/api/v1/urls/{shortCode}` | Get URL metadata & click count |
| `GET` | `/api/v1/urls/{shortCode}/stats` | Daily click analytics (30 days) |
| `DELETE` | `/api/v1/urls/{shortCode}` | Soft-delete a short URL |

Full interactive docs at `/swagger-ui.html`.

---

## 🛠️ Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| **Backend** | Spring Boot 3.2, Java 21 | Industry standard, Virtual Threads ready |
| **Database** | PostgreSQL 16 | ACID, battle-tested, free managed hosting |
| **Cache** | Redis 7 + Lettuce | Sub-ms reads, atomic INCR for ID gen |
| **Migrations** | Flyway | Version-controlled schema, safe rollouts |
| **Rate Limiting** | Bucket4j | Token-bucket — fairer than fixed-window |
| **API Docs** | SpringDoc OpenAPI 3 | Auto-generated, always up to date |
| **Frontend** | Three.js + Vanilla JS | Zero-dependency, stunning 3D fish scene |
| **Container** | Docker + Compose | One command, reproducible everywhere |
| **CI** | GitHub Actions | Test on every push, build Docker image |

---

## 📁 Project Structure

```
url-shortener/
├── src/main/java/com/urlshortener/
│   ├── config/          # Redis, OpenAPI, AppProperties (@ConfigurationProperties)
│   ├── controller/      # UrlController, RedirectController, RootController
│   ├── dto/             # Request/Response DTOs with Bean Validation
│   ├── exception/       # 5 custom exceptions + GlobalExceptionHandler
│   ├── filter/          # RateLimitFilter (Bucket4j per-IP token bucket)
│   ├── model/           # Url entity, ClickEvent entity (JPA + sequences)
│   ├── repository/      # Spring Data JPA + @Modifying JPQL queries
│   ├── service/         # Interfaces + impls (UrlService, CacheService, AnalyticsService)
│   └── util/            # Base62Encoder, UrlValidator
├── src/main/resources/
│   ├── static/index.html      # 🐟 Three.js 3D fish homepage
│   ├── application.yml        # Base config (env-var driven)
│   ├── application-dev.yml    # Dev overrides
│   ├── application-prod.yml   # Prod overrides
│   └── db/migration/          # V1__create_urls_table.sql, V2__create_click_events_table.sql
├── src/test/                  # Unit tests — H2 in-memory, no Docker needed
├── docs/
│   ├── ARCHITECTURE.md   # 6 Architecture Decision Records (ADRs)
│   └── DEPLOYMENT.md     # Free deployment guides
├── Dockerfile            # Multi-stage: maven:3.9 builder → jre:21 runtime
├── docker-compose.yml    # App + PostgreSQL 16 + Redis 7
└── .github/workflows/ci.yml  # Test + Build on every push
```

---

## 🧪 Tests

```bash
mvn test      # Unit tests — H2 in-memory, no Docker needed
mvn verify    # Full build + tests
```

---

## 📈 Performance Benchmarks

| Endpoint | Latency p50 | Notes |
|----------|------------|-------|
| `GET /{code}` cache hit | **< 5ms** | Redis only, DB not touched |
| `GET /{code}` cache miss | **< 20ms** | Redis miss → PostgreSQL → cache |
| `POST /api/v1/urls` | **< 30ms** | Redis INCR + PostgreSQL write |
| `GET /stats` | **< 50ms** | PostgreSQL aggregate + GROUP BY |

---

## 🧠 Architecture Decisions (ADRs)

All key decisions documented in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md):

- **ADR-001** — Base62 vs UUID: shorter, URL-safe, human-readable, ~3.5T capacity
- **ADR-002** — Redis as L1 cache: sub-ms reads, graceful degradation on Redis outage
- **ADR-003** — Async analytics: redirect must never wait for DB writes
- **ADR-004** — Soft delete: preserves click analytics referential integrity
- **ADR-005** — Bucket4j token-bucket: fairer than fixed-window, burst-friendly
- **ADR-006** — Flyway migrations: reproducible schema, safe zero-downtime upgrades

---

## 🤝 Contributing

1. Fork the repo
2. Create your branch: `git checkout -b feature/amazing-feature`
3. Commit: `git commit -m 'feat: add amazing feature'`
4. Push: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📄 License

MIT — see [LICENSE](LICENSE) for details.

---

<div align="center">

**⭐ Star this repo if it helped you — it means the world!**

Built with Spring Boot · Redis · PostgreSQL · Three.js · Docker

[⬆ Back to top](#-url-shortener)

</div>
