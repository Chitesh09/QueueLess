# ⚡ QueueLess — Enterprise Virtual Queue & Operations Management SaaS

[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2-emerald?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18.3-sky?style=flat-square&logo=react)](https://react.dev)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple?style=flat-square&logo=vite)](https://vitejs.dev)
[![TanStack Query](https://img.shields.io/badge/TanStack%20Query-v5-ff4154?style=flat-square&logo=reactquery)](https://tanstack.com/query)
[![MySQL 8](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)](https://www.mysql.com)
[![Redis 7](https://img.shields.io/badge/Redis-7.0-red?style=flat-square&logo=redis)](https://redis.io)
[![STOMP WebSocket](https://img.shields.io/badge/WebSocket-STOMP%20%2F%20SockJS-amber?style=flat-square)](https://spring.io)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

QueueLess is a production-ready, multi-tenant enterprise Virtual Queue & Operations Management SaaS platform. Inspired by systems at Epic, Cerner, Stripe Dashboard, Linear, and Vercel, it streamlines customer triage, wait-time estimation, weighted round-robin counter dispatch, real-time operations analytics, and public TV waiting room display boards.

---

## 🌟 Key Architecture & Feature Highlights

- **Multi-Tenant System**: Complete tenant isolation with `X-Tenant-ID` header routing and multi-branch support.
- **Weighted Round-Robin (WRR) Dispatch**: Intelligent backend algorithm prioritizing Emergency triage (`🔴 Emergency`), Senior Citizens/PwD (`🟡 Senior`), and Appointments (`🟣 Appointment`).
- **Real-Time WebSocket Sync**: STOMP over SockJS (`/ws`) automatically triggering zero-latency TanStack Query cache invalidations across connected devices.
- **Enterprise Operations Analytics**: Recharts interactive dashboards plotting peak arrival hours, department throughput comparison, and wait-time SLA compliance.
- **TV Waiting Room Display Board**: Large-screen hero token call ticker with counter assignment and audio chime notification simulation.
- **Multi-Language i18n**: Real-time language switching across **English 🇺🇸**, **Spanish 🇪🇸**, **French 🇫🇷**, and **Hindi 🇮🇳**.
- **WCAG 2.1 AA Accessibility**: High-contrast accessibility theme, Skip to Main Content link, screen reader `aria-live="polite"` announcers, and `prefers-reduced-motion` compliance.

---

## 🏗️ System Architecture Diagram

```
[ React 18 + Vite SPA ]  <--- STOMP WebSockets --->  [ Spring Boot 3 Backend ]
    (TanStack Query)                                       (Port 8080)
           |                                                    |
     Axios REST API                                       Flyway Migrations
           |                                                    |
           v                                                    v
[ Nginx Reverse Proxy ]                           [ MySQL 8 DB ] + [ Redis 7 Cache ]
```

---

## 🚀 Quick Start (Local Development)

### Prerequisites:
- Java 21 JDK
- Node.js 20+
- Docker & Docker Compose

### 1. Launch Stack with Docker Compose:
```bash
git clone https://github.com/Chitesh09/QueueLess.git
cd QueueLess
docker compose up -d --build
```

### 2. Access Local Applications:
- **Frontend SPA**: [http://localhost:3000](http://localhost:3000) (or port 80 via Nginx)
- **Spring Boot API**: [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
- **Swagger API Docs**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Actuator Health**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### 3. Demo Credentials:
- **Counter Operator / Admin**: `operator@cityhospital.com` / `password123`
- **Super Admin**: `superadmin@queueless.com` / `password123`
- **Customer**: `john.doe@gmail.com` / `password123`

---

## 🌐 100% Free Cloud Deployment Blueprint

Deploy QueueLess completely for free ($0/month):
- **Frontend**: [Vercel](https://vercel.com) (Static Vite site, free custom SSL)
- **Backend**: [Render.com](https://render.com) (Spring Boot Java 21 web service)
- **Database**: [TiDB Cloud Serverless](https://tidbcloud.com) (Managed MySQL 8 compatible, 5 GB free)
- **Redis Cache**: [Upstash Redis](https://upstash.com) (Serverless Redis)

For full step-by-step free deployment instructions, refer to **[FREE_DEPLOYMENT.md](FREE_DEPLOYMENT.md)**.
For production Linux VPS / Docker instructions, refer to **[DEPLOYMENT.md](DEPLOYMENT.md)**.

---

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
