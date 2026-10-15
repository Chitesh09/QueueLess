# ⚡ QueueLess — Virtual Queue & Operations Management

[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.2-emerald?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18.3-sky?style=flat-square&logo=react)](https://react.dev)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple?style=flat-square&logo=vite)](https://vitejs.dev)
[![MySQL 8](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)](https://www.mysql.com)
[![Redis 7](https://img.shields.io/badge/Redis-7.0-red?style=flat-square&logo=redis)](https://redis.io)

QueueLess is a virtual queue and operations management platform designed for customer triage, wait-time estimation, counter dispatch, operations analytics, and public waiting-room displays. Security, tenant-isolation, and concurrency claims should be treated as verified only after the corresponding tests pass.

## Features

- Tenant routing through the `X-Tenant-ID` header and multi-branch support.
- Priority queue categories for emergency, senior/PwD, and appointment cases.
- STOMP over SockJS for real-time updates across connected clients.
- Operations dashboards for arrival patterns, department throughput, and wait-time SLAs.
- Public waiting-room display and multilingual UI.
- Accessibility support including high contrast, skip navigation, live announcements, and reduced motion.

## Architecture

```
[ React + Vite SPA ] <--- STOMP WebSockets ---> [ Spring Boot API ]
       |                                             |
    REST API                                  Flyway migrations
       |                                             |
    [ Nginx ]                               [ MySQL ] + [ Redis ]
```

## Quick start (local Docker)

Prerequisites: Java 21, Node.js 20+, Docker and Docker Compose.

1. Copy `.env.example` to `.env`.
2. Replace all placeholder values with freshly generated secrets. Generate random values using `openssl rand -base64 48` or `openssl rand -hex 32`; use a distinct value for each secret.
3. Start the stack:

```bash
cp .env.example .env
# Edit .env and replace every REPLACE_WITH_* value before starting.
docker compose up -d --build
```

Open the app at http://localhost. MySQL and Redis host ports are bound to loopback only. The backend is not published directly to the host; Nginx reaches it through the private Compose network.

If the local database contains seeded demo accounts, use them only for local testing. Change or remove demo credentials before any public deployment.

## Security notes

- Keep `.env` private; only commit `.env.example`.
- Secrets must be unique per environment and are loaded from environment variables.
- MySQL and Redis ports are bound to localhost in the Compose setup.
- This repository is being hardened; do not describe it as production-ready until authentication, authorization, tenant isolation, concurrency, and deployment checks have been tested.

## License

MIT — see [LICENSE](LICENSE).
