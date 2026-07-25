# QueueLess SaaS — Production Deployment & Cloud Guide

This guide provides step-by-step instructions for deploying the **QueueLess SaaS Platform** (Spring Boot 3 + React/Vite + MySQL 8 + Redis 7 + Nginx + WebSockets) to production across cloud providers.

---

## 🛠️ Option 1: Linux VPS / AWS EC2 / DigitalOcean (Recommended)

### Prerequisites:
- Ubuntu 22.04 LTS or Debian 12 server
- Docker Engine 24.0+ & Docker Compose v2.20+ installed
- Domain name pointed to server IP address (e.g. `queueless.example.com`)

### 1. Clone Repository & Setup Environment:
```bash
git clone https://github.com/Chitesh09/QueueLess.git
cd QueueLess

cp .env.example .env
nano .env  # Update MYSQL_PASSWORD and JWT_SECRET with secure random strings
```

### 2. Launch Docker Production Stack:
```bash
docker compose up -d --build
```

### 3. Verify Container Status & Health:
```bash
docker compose ps
docker compose logs -f backend
```

---

## 🔒 Option 2: Enabling HTTPS with Let's Encrypt & Certbot

To secure traffic with free SSL certificates:

1. Install Certbot:
   ```bash
   sudo apt update && sudo apt install -y certbot python3-certbot-nginx
   ```
2. Obtain SSL Certificate:
   ```bash
   sudo certbot --nginx -d queueless.example.com
   ```
3. Certbot will automatically configure HTTPS redirects in `nginx.conf`.

---

## 🚀 Option 3: Railway / Render / PaaS Deployment

- **Backend Service**: Deploy `/backend` subdirectory as a Maven Web Service. Environment variables: `SPRING_PROFILES_ACTIVE=prod`, `SPRING_DATASOURCE_URL`, `JWT_SECRET`.
- **Frontend Service**: Deploy `/frontend` subdirectory as a Static Site. Build Command: `npm run build`, Publish Directory: `dist`.
- **Managed Databases**: Provision managed MySQL 8 and Redis instances.

---

## 📊 Maintenance & Backup Procedures

### Database Backup:
```bash
docker exec queueless_mysql mysqldump -u root -p'root_password' queueless_db > queueless_backup_$(date +%Y%m%d).sql
```

### Database Restore:
```bash
docker exec -i queueless_mysql mysql -u root -p'root_password' queueless_db < queueless_backup_20260725.sql
```
