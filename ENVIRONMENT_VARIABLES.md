# 🔑 QueueLess — Cloud Production Environment Variables Reference

This document provides the complete reference for setting up **QueueLess** on **Render** (Backend), **Vercel** (Frontend), **TiDB Cloud** (MySQL 8), and **Upstash Redis**.

---

## ⚙️ Backend Environment Variables (Spring Boot 3 on Render)

Add these environment variables in your **Render Web Service Dashboard** under **Environment**:

| Environment Variable | Description | Example / Production Value |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `JAVA_VERSION` | Java JDK Version | `21` |
| `PORT` | Web Service HTTP Port | `8080` |
| `DB_URL` (or `SPRING_DATASOURCE_URL`) | TiDB Cloud JDBC Connection String | `jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/queueless_db?useSSL=true&enabledTLSProtocols=TLSv1.2,TLSv1.3` |
| `DB_USERNAME` (or `SPRING_DATASOURCE_USERNAME`) | TiDB Cloud Database User | `xxxxxx.root` |
| `DB_PASSWORD` (or `SPRING_DATASOURCE_PASSWORD`) | TiDB Cloud Database Password | `<TIDB_CLOUD_PASSWORD>` |
| `REDIS_HOST` (or `SPRING_REDIS_HOST`) | Upstash Redis Host Endpoint | `your-upstash-instance.upstash.io` |
| `REDIS_PORT` (or `SPRING_REDIS_PORT`) | Upstash Redis TLS Port | `6379` |
| `REDIS_PASSWORD` (or `SPRING_REDIS_PASSWORD`) | Upstash Redis Access Password | `<UPSTASH_REDIS_PASSWORD>` |
| `SPRING_REDIS_SSL_ENABLED` | SSL/TLS for Upstash Redis | `true` |
| `JWT_SECRET` | 64+ character Base64 Secret Key | `<YOUR_SECURE_JWT_SECRET>` |

---

## 🌐 Frontend Environment Variables (React + Vite on Vercel)

Add these environment variables in your **Vercel Project Settings** under **Environment Variables**:

| Environment Variable | Description | Example / Production Value |
| :--- | :--- | :--- |
| `VITE_API_BASE_URL` | Render Spring Boot REST API Base URL | `https://queueless-8u4a.onrender.com` |
| `VITE_WS_URL` | STOMP WebSocket Endpoint | `wss://queueless-8u4a.onrender.com/ws` |
| `VITE_DEFAULT_TENANT_ID` | Default Multi-Tenant ID | `1` |
