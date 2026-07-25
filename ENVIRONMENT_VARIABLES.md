# 🔑 QueueLess — Environment Variables Reference

This document provides a complete reference for all environment variables used by the **QueueLess** backend and frontend.

---

## ⚙️ Backend Environment Variables (Spring Boot 3)

| Environment Variable | Description | Default Value | Example / Production Value |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` | `prod` |
| `PORT` | Server HTTP port | `8080` | `8080` |
| `SPRING_DATASOURCE_URL` | JDBC Database URL | `jdbc:mysql://localhost:3307/queueless_db...` | `jdbc:mysql://mysql:3306/queueless_db?useSSL=false` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `queueless_user` | `queueless_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `queueless_password` | `<SECURE_PASSWORD>` |
| `SPRING_REDIS_HOST` | Redis cache host | `localhost` | `redis` |
| `SPRING_REDIS_PORT` | Redis cache port | `6379` | `6379` |
| `SPRING_REDIS_PASSWORD` | Redis auth password | `` | `<REDIS_PASSWORD>` |
| `JWT_SECRET` | Base64 HMAC secret key | `d1FhOFk5...` | `<64_CHAR_BASE64_SECRET>` |
| `JWT_EXPIRATION` | Token validity in ms | `86400000` (24h) | `86400000` |
| `HIKARI_MAX_POOL_SIZE` | Max DB pool connections | `20` | `20` |

---

## 🌐 Frontend Environment Variables (React + Vite)

| Environment Variable | Description | Default Value | Example / Production Value |
| :--- | :--- | :--- | :--- |
| `VITE_API_BASE_URL` | Spring Boot REST API Base URL | `http://localhost:8080` | `https://queueless-backend.onrender.com` |
| `VITE_WS_URL` | STOMP WebSocket URL | `http://localhost:8080/ws` | `wss://queueless-backend.onrender.com/ws` |
