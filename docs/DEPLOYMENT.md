# Deployment Guide

## Option A — Railway (Recommended for Free Tier)

Railway offers a $5/month free credit, enough to run the full stack.

### Steps

1. **Push to GitHub**
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/url-shortener.git
   git push -u origin main
   ```

2. **Create Railway project** at [railway.app](https://railway.app)

3. **Add services:**
   - `New → Database → PostgreSQL`
   - `New → Database → Redis`
   - `New → Deploy from GitHub repo`

4. **Set environment variables** on the app service:
   ```
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=${{Postgres.DATABASE_URL}}
   DATABASE_USERNAME=${{Postgres.PGUSER}}
   DATABASE_PASSWORD=${{Postgres.PGPASSWORD}}
   REDIS_HOST=${{Redis.REDIS_HOST}}
   REDIS_PORT=${{Redis.REDIS_PORT}}
   REDIS_PASSWORD=${{Redis.REDIS_PASSWORD}}
   REDIS_SSL=false
   APP_BASE_URL=https://YOUR-APP.up.railway.app
   ```

5. Railway auto-detects the Dockerfile and deploys. Done!

---

## Option B — Render

### Steps

1. **Create a PostgreSQL** database at render.com (free tier: 1GB)

2. **Create a Redis** instance (Render Managed Redis or use Redis Cloud free tier at redis.io)

3. **Create a Web Service** from your GitHub repo:
   - Environment: Docker
   - Dockerfile path: `./Dockerfile`

4. Set environment variables (same as Railway above, adapt connection strings)

5. Add a health check path: `/actuator/health`

---

## Option C — Fly.io

```bash
# Install Fly CLI
brew install flyctl   # or see fly.io/docs

# Login
flyctl auth login

# Create app
flyctl launch --no-deploy

# Create PostgreSQL
flyctl postgres create

# Create Redis (via Upstash)
# Sign up at upstash.com, create a Redis database, copy credentials

# Set secrets
flyctl secrets set \
  SPRING_PROFILES_ACTIVE=prod \
  DATABASE_URL="jdbc:postgresql://..." \
  DATABASE_USERNAME=... \
  DATABASE_PASSWORD=... \
  REDIS_HOST=... \
  REDIS_PORT=6379 \
  REDIS_PASSWORD=... \
  REDIS_SSL=true \
  APP_BASE_URL=https://YOUR-APP.fly.dev

# Deploy
flyctl deploy
```

---

## Option D — Docker Compose (Self-hosted VPS)

Any VPS with 1GB RAM (free on Oracle Cloud Always Free, or $4/mo DigitalOcean).

```bash
# On the server
git clone <repo>
cd url-shortener

# Create .env from template
cp .env.example .env
# Edit .env with your values

docker compose -f docker-compose.yml up -d --build
```

---

## Environment Variables Reference

| Variable | Required | Description |
|----------|----------|-------------|
| `DATABASE_URL` | Yes | Full JDBC URL, e.g. `jdbc:postgresql://host:5432/dbname` |
| `DATABASE_USERNAME` | Yes | DB username |
| `DATABASE_PASSWORD` | Yes | DB password |
| `REDIS_HOST` | Yes | Redis hostname |
| `REDIS_PORT` | No (6379) | Redis port |
| `REDIS_PASSWORD` | No | Redis AUTH password |
| `REDIS_SSL` | No (false) | `true` for managed Redis with TLS |
| `APP_BASE_URL` | Yes | Public URL, e.g. `https://short.myapp.com` |
| `SPRING_PROFILES_ACTIVE` | Yes | `prod` |

---

## Post-Deployment Checks

```bash
# Health check
curl https://YOUR-APP/actuator/health

# Shorten a URL
curl -X POST https://YOUR-APP/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com"}'

# Follow redirect
curl -L https://YOUR-APP/<shortCode>

# Swagger UI
open https://YOUR-APP/swagger-ui.html
```

---

## Database Migrations

Flyway runs automatically on startup. To check migration status:

```bash
# Via actuator (if flyway endpoint enabled)
curl https://YOUR-APP/actuator/flyway
```
