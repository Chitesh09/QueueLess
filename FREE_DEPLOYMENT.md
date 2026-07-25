# 🚀 QueueLess SaaS — Verified 100% Free Deployment Guide ($0/Month)

You can host the entire **QueueLess SaaS Platform** (Spring Boot 3 + MySQL 8 + Redis 7 + React Frontend + STOMP WebSockets) completely **for FREE** using verified, long-term free cloud tiers.

---

## 🏗️ Verified Free Cloud Architecture Blueprint

| Component | Verified Free Cloud Provider | Free Tier Specifications | Cost |
| :--- | :--- | :--- | :---: |
| **Frontend (React + Vite)** | **Vercel** | Unlimited Bandwidth, Global CDN, SSL | **$0 / mo** |
| **Backend (Spring Boot 3)** | **Render.com** | 512 MB RAM, Java 21 Runtime | **$0 / mo** |
| **Database (MySQL Compatible)** | **TiDB Cloud Serverless** | Managed Serverless MySQL, 5 GB Storage, No CC Needed | **$0 / mo** |
| **Redis Cache** | **Upstash Redis** | 10,000 requests/day, Serverless | **$0 / mo** |

---

## 📋 Step-by-Step Verified Free Deployment Walkthrough

### Step 1: Create Free Serverless MySQL Database on TiDB Cloud
1. Go to **[TiDB Cloud (tidbcloud.com)](https://tidbcloud.com)** and sign up for a free account (No credit card required).
2. Click **Create Cluster** → Select **Serverless** → Choose **Free Tier** (5 GB MySQL-compatible storage).
3. Once provisioned, click **Connect** → Copy your connection details:
   - **Host**: e.g., `gateway01.us-east-1.prod.aws.tidbcloud.com`
   - **Port**: `4000`
   - **User**: e.g., `3xxx.root`
   - **Password**: `<YOUR_TIDB_PASSWORD>`
   - **JDBC Connection String**: `jdbc:mysql://<TIDB_HOST>:4000/test?sslMode=VERIFY_IDENTITY`

---

### Step 2: Create Free Serverless Redis on Upstash
1. Go to **[Upstash.com](https://upstash.com)** and sign up for a free account.
2. Click **Create Database** → Name: `queueless-redis` → Region: Choose closest to your backend.
3. Copy your **Redis Endpoint** (e.g., `queueless-redis-xxxx.upstash.io`) and **Password**.

---

### Step 3: Deploy Backend Spring Boot 3 on Render ($0/Month)
1. Push your QueueLess repository to **GitHub**.
2. Go to **[Render.com](https://render.com)** → Sign in → Click **New +** → **Web Service**.
3. Connect your GitHub repository `QueueLess`.
4. Configure service settings:
   - **Root Directory**: `backend`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests` (or `mvn package -DskipTests`)
   - **Start Command**: `java -Dspring.profiles.active=prod -jar target/*.jar`
5. Add **Environment Variables** under Render settings (DO NOT hardcode secrets in code):
   - `SPRING_DATASOURCE_URL` = `jdbc:mysql://<TIDB_HOST>:4000/test?sslMode=VERIFY_IDENTITY`
   - `SPRING_DATASOURCE_USERNAME` = `<TIDB_USER>`
   - `SPRING_DATASOURCE_PASSWORD` = `<TIDB_PASSWORD>`
   - `SPRING_REDIS_HOST` = `<UPSTASH_REDIS_HOST>`
   - `SPRING_REDIS_PORT` = `6379`
   - `SPRING_REDIS_PASSWORD` = `<UPSTASH_PASSWORD>`
   - `JWT_SECRET` = `<GENERATE_UNIQUE_64_CHAR_RANDOM_BASE64_KEY_ONLY_ON_RENDER>`
6. Click **Create Web Service**. Render will build and launch your backend!
   - Copy your Render backend URL (e.g. `https://queueless-backend.onrender.com`).

---

### Step 4: Deploy Frontend React + Vite on Vercel ($0/Month)
1. Go to **[Vercel.com](https://vercel.com)** → Sign in with GitHub.
2. Click **Add New...** → **Project** → Select `QueueLess` repository.
3. Configure project settings:
   - **Framework Preset**: `Vite`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
4. Add **Environment Variables** for HTTP & Secure WebSockets (`wss://`):
   - `VITE_API_BASE_URL` = `https://queueless-backend.onrender.com`
   - `VITE_WS_URL` = `wss://queueless-backend.onrender.com/ws`
5. Click **Deploy**. Vercel will build and launch your frontend with automatic HTTPS!

---

## 🔒 Security & WebSocket Production Checklist

1. **JWT Secret Protection**: Generate a unique random secret for production (`JWT_SECRET`) and store it ONLY in Render environment variables. Never commit JWT secrets to Git.
2. **WebSocket Path Alignment**: Frontend `VITE_WS_URL=wss://queueless-backend.onrender.com/ws` matches the Spring Boot backend `/ws` endpoint path.
3. **Environment Variable Injection**: Spring Boot production profile (`application-prod.yml`) reads all database URLs, usernames, passwords, Redis credentials, and JWT secrets directly from environment variables.

---

## ✅ Post Deployment Checklist

- Backend URL is accessible
- Frontend loads successfully
- User registration works
- Login works
- JWT authentication works
- Database connection successful
- Redis connection successful
- WebSocket connection established
- Queue updates work in real time
- No browser console errors
- No backend startup errors
