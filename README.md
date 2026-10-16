# ⚡ QueueLess — Enterprise Virtual Queue & Operations Management SaaS

[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2.4-emerald?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java 21](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![React 18](https://img.shields.io/badge/React-18.2-sky?style=flat-square&logo=react)](https://react.dev)
[![Vite 5](https://img.shields.io/badge/Vite-5.4-purple?style=flat-square&logo=vite)](https://vitejs.dev)
[![TanStack Query](https://img.shields.io/badge/TanStack%20Query-v5-ff4154?style=flat-square&logo=reactquery)](https://tanstack.com/query)
[![Flyway](https://img.shields.io/badge/Flyway-10.x-cc0200?style=flat-square&logo=flyway)](https://flywaydb.org/)
[![STOMP WebSocket](https://img.shields.io/badge/WebSocket-STOMP%20%2F%20SockJS-amber?style=flat-square)](https://spring.io)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

QueueLess is an enterprise multi-tenant Virtual Queue & Operations Management SaaS platform. It streamlines customer triage, wait-time estimation, weighted round-robin (WRR) counter dispatch, real-time staff operations, and public TV waiting room display boards with end-to-end tenant isolation and role-based access control (RBAC).

---

## 🏗️ Architecture & Production Stack

- **Frontend**: React 18, Vite, TanStack Query v5, Recharts, TailwindCSS, STOMP/SockJS client (Hosted on **Vercel**).
- **Backend**: Spring Boot 3.2.4, Java 21, Spring Security, Spring Data JPA, Hibernate 6, Spring Boot Actuator (Hosted on **Render**).
- **Primary Database**: TiDB Cloud Serverless (MySQL 8 wire-compatible with TLSv1.2/1.3) managed with **Flyway** migrations.
- **Cache & Ephemeral Store**: Upstash Redis (Serverless Redis with TLS enabled).
- **Real-Time Sync**: Transaction-synchronized STOMP WebSocket topics with access-controlled private customer channels and sanitized public branch boards.

---

## 🔐 Security & Operational Baseline

1. **Multi-Tenant Isolation**: Tenant scoping is enforced at filter, service, and controller layers. Non-superadmin users derive tenant ID strictly from authenticated JWT claims; client `X-Tenant-ID` headers are ignored. `SUPER_ADMIN` can explicitly scope queries using `X-Tenant-ID`.
2. **Role-Based Access Control (RBAC)**: Distinct permissions for `SUPER_ADMIN`, `ORG_ADMIN`, `BRANCH_ADMIN`, `OPERATOR`, and `USER` (customer).
3. **Token Privacy**: Detailed token data (including QR HMAC signatures) is returned only to verified token owners and authorized branch staff. Public displays and unauthenticated lookups receive sanitized models.
4. **Structured Logging & Tracing**: Inbound requests carry or auto-generate `X-Correlation-ID`. Logs output structured entries with MDC context:
   ```
   2026-10-16 12:15:46.342 [main] INFO [corr=f47ac10b-58cc-4372-a567-0e02b2c3d479] [tenant=1] c.q.c.QueueController - Token created
   ```
5. **Actuator Health & Sensitive Data Redaction**: `/actuator/health` exposes liveness and readiness states (`UP` / `DOWN`). Detailed component credentials, database queries, and hostnames are redacted for unauthenticated callers via `management.endpoint.health.show-details=when-authorized`.
6. **Error Sanitization**: Unhandled server exceptions return sanitized RFC 7807 `ProblemDetail` responses with the correlation ID, suppressing internal SQL syntax and stack traces.

---

## 📋 Environment Variables Reference

### Backend Configuration

| Environment Variable | Description | Default / Example | Required in Prod |
| :--- | :--- | :--- | :---: |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev`, `prod`, `test`) | `dev` | Yes (`prod`) |
| `PORT` | HTTP port for web server | `8080` | Yes (Render sets this) |
| `DB_URL` | JDBC URL for TiDB / MySQL | `jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/queueless_db?useSSL=true&enabledTLSProtocols=TLSv1.2,TLSv1.3` | **Yes** |
| `DB_USERNAME` | Database username | `queueless_user` | **Yes** |
| `DB_PASSWORD` | Database password | `queueless_password` | **Yes** |
| `HIKARI_MAX_POOL_SIZE` | Maximum connection pool size | `20` | No |
| `HIKARI_MIN_IDLE` | Minimum idle connections | `5` | No |
| `REDIS_HOST` | Redis hostname (Upstash or local) | `localhost` | **Yes** |
| `REDIS_PORT` | Redis port | `6379` | **Yes** |
| `REDIS_PASSWORD` | Redis auth password / token | `""` | **Yes** |
| `SPRING_REDIS_SSL_ENABLED`| Enable TLS for Redis connections | `true` | Yes (for Upstash) |
| `JWT_SECRET` | HMAC-SHA512 secret for JWT tokens | *Hex/Base64 string (>= 64 chars)* | **Yes** |
| `GUEST_SECRET` | HMAC secret for signed guest sessions | Falls back to `JWT_SECRET` | Recommended |
| `QR_SECRET` | HMAC secret for counter check-in QR codes | Falls back to `JWT_SECRET` | Recommended |

### Frontend Configuration

| Environment Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `VITE_API_BASE_URL` | Base URL of the backend REST API | `https://queueless-8u4a.onrender.com` |
| `VITE_WS_URL` | WebSocket URL for STOMP / SockJS | Derived automatically (`wss://.../ws`) |
| `VITE_DEFAULT_TENANT_ID` | Default organization identifier | `1` |

---

## 🚀 Local Development Setup

### Option A: Native Development (Recommended)

#### Prerequisites
- Java 21 JDK
- Node.js 20+ and npm
- (Optional) Local MySQL 8 and Redis 7, or rely on H2 test mode

#### 1. Backend Setup
```bash
cd backend

# Run the complete test suite (89 automated tests)
./mvnw clean test

# Run Spring Boot backend locally (uses dev profile on port 8080)
./mvnw spring-boot:run
```

#### 2. Frontend Setup
```bash
cd frontend

# Install dependencies
npm ci

# Start Vite dev server on port 5173
npm run dev

# Run production build validation
npm run build
```

### Option B: Docker Compose (Local Stack)
```bash
docker compose up -d --build
```
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator Health: `http://localhost:8080/actuator/health`

---

## 🗄️ Database Migrations (Flyway)

Database migrations reside in `backend/src/main/resources/db/migration`:
- **`V1__init_schema.sql`**: Initial schema definition (organizations, branches, departments, services, counters, tokens, users, audit logs).
- **`V2__seed_data.sql`**: Default seed data for initial setup and automated test scenarios.
- **`V3__phase1_queue_correctness.sql`**: Token sequence generator schema, daily sequences, and uniqueness constraints.

### Migration Rules
- Hibernate `ddl-auto` is set to `validate`. Hibernate **never** modifies schemas directly.
- Migrations run automatically on application startup.
- Connection retry logic is configured for Serverless TiDB (`connect-retries: 5`, `connect-retries-interval: 3`).
- **Never modify existing versioned migration files** once committed; always create a new incremental migration (`V4__...sql`).

---

## 🧪 Automated Quality Gates & Tests

### Backend Test Suite
The backend contains **89 automated integration and unit tests** across all core subsystems:
```bash
cd backend
./mvnw clean verify -B
```

**Test Suite Coverage:**
- **`TenantIsolationAndRbacSecurityTest` (34 tests)**: Multi-tenant boundary isolation, RBAC role restrictions, spoofed header prevention, super-admin scoping, token privacy sanitization, and async audit logging context preservation.
- **`WebSocketSecurityTest` (15 tests)**: STOMP `CONNECT` JWT validation, topic subscription authorization, cross-tenant subscription rejections.
- **`OperationalReadinessTest` (5 tests)**: Actuator health status, credential redaction, request correlation ID generation/propagation, 500 exception sanitization, and security headers.
- **`QueueEngineConcurrencyTest` (7 tests)**: Overlapping concurrent joins and calls, pessimistic locking, sequence consistency.
- **`QueueStateMachineTest` (7 tests)**: Complete token lifecycle validation (`WAITING` ➔ `CALLED` ➔ `SERVING` ➔ `COMPLETED` / `CANCELLED` / `NO_SHOW`).
- **`GuestSessionSecurityTest` (8 tests)**: HMAC guest sessions, token tampering detection, IP rate limiting.
- **`DisplayBoardAndCounterApiTest` (4 tests)**: Display board snapshots, counter queue and pause/resume APIs.
- **`TokenSequenceGeneratorTest` (3 tests)**: Daily atomic token sequence allocation.
- **`PrioritySchedulerTest` (4 tests)**: Weighted Round-Robin (WRR) queue scheduling algorithm.
- **`QueueBroadcasterTest` (2 tests)**: Transaction-synchronized event broadcasting.

### Frontend Production Build
```bash
cd frontend
npm run build
```

---

## ☁️ Production Deployment Guide

### Backend on Render
1. Create a new **Web Service** connected to the repository.
2. Root Directory: `backend`
3. Environment: `Java`
4. Build Command: `./mvnw clean package`
5. Start Command: `java -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -jar target/queueless-backend-1.0.0-SNAPSHOT.jar`
6. Configure environment variables in the Render Dashboard:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (from TiDB Cloud)
   - `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` (from Upstash)
   - `JWT_SECRET`, `GUEST_SECRET`, `QR_SECRET`

### Frontend on Vercel
1. Import repository into Vercel.
2. Root Directory: `frontend`
3. Framework Preset: `Vite`
4. Build Command: `npm run build`
5. Output Directory: `dist`
6. Configure environment variables:
   - `VITE_API_BASE_URL`: `https://<your-render-backend-url>`
   - `VITE_DEFAULT_TENANT_ID`: `1`

---

## ⚠️ Operational Boundaries & Known Limitations

1. **Serverless Cold Starts**:
   - Free-tier Render instances spin down after inactivity, causing initial request latency (~30-50s) while the JVM initializes.
2. **Distributed Transactions**:
   - Tests run in-memory using H2. While TiDB is MySQL wire-compatible, distributed lock behaviors and TiKV conflict retries under high concurrency should be verified against a staging TiDB cluster.
3. **Redis Failover / Eviction**:
   - When Upstash Redis request quotas are exhausted or connections drop, rate limiting and WebSocket caching degrade gracefully to database lookups.
4. **WebSocket Reconnections**:
   - The frontend automatically reconnects within 3 seconds and resubscribes to active branch channels, but customer tickets should verify state via REST polling snapshot on reconnect.

---

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
