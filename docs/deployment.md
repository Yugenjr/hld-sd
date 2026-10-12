# Smart EV Charging Platform — Deployment & Operations Guide

## 1. Quick Start (Zero-Dependency Standalone Mode)

The application includes an embedded in-memory PostgreSQL-compatible mode, enabling immediate local execution without requiring Docker:

### Step 1: Start Backend (Port 8080)
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
- Swagger UI will be available at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Actuator Health Check at: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Step 2: Start Frontend (Port 3000)
```bash
cd frontend
npm install
npm run dev
```
- Open [http://localhost:3000](http://localhost:3000) in your web browser.

---

## 2. Seed Credentials for Local Exploration

| Role | Email | Password |
|---|---|---|
| **EV Driver (Alex Morgan)** | `demo@smartev.com` | `Password123!` |
| **System Administrator** | `admin@smartev.com` | `AdminPassword123!` |

*(A 1-click login button is also provided directly on the sign-in page for instant exploration).*

---

## 3. Docker Infrastructure (PostgreSQL & Redis)

For production-identical testing using PostgreSQL 16:

```bash
# Start PostgreSQL & Redis services
cd infra
docker compose up -d
```

### Run Backend with PostgreSQL Profile
```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=docker
```

---

## 4. Environment Variables Reference

| Variable | Description | Default |
|---|---|---|
| `POSTGRES_DB` | PostgreSQL database name | `evcharging` |
| `POSTGRES_USER` | Database username | `postgres` |
| `POSTGRES_PASSWORD` | Database password | `postgres` |
| `POSTGRES_HOST` | Database host | `localhost` |
| `POSTGRES_PORT` | Database port | `5432` |
| `JWT_SECRET` | Secret key for JWT signing | *(Safe local default)* |
| `JWT_EXPIRATION_MS` | Token lifespan in ms | `86400000` (24h) |
| `NEXT_PUBLIC_API_BASE_URL` | Frontend API Target | `http://localhost:8080/api/v1` |
