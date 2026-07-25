# 🛠️ QueueLess — Local Installation & Setup Guide

This guide walks you through setting up **QueueLess** on your local machine for development and testing.

---

## 💻 Method 1: Docker Compose Setup (Recommended)

### Prerequisites:
- Docker Desktop or Docker Engine 24.0+ installed and running.

### Steps:
1. **Clone Repository**:
   ```bash
   git clone https://github.com/Chitesh09/QueueLess.git
   cd QueueLess
   ```

2. **Launch All Services**:
   ```bash
   docker compose up -d --build
   ```

3. **Verify Containers**:
   ```bash
   docker compose ps
   ```
   Containers `queueless_mysql`, `queueless_redis`, `queueless_backend`, `queueless_frontend`, and `queueless_nginx` will be running.

4. Open **[http://localhost:3000](http://localhost:3000)** in your browser!

---

## ⚙️ Method 2: Native Development Setup

### Prerequisites:
- Java 21 JDK
- Maven 3.9+
- Node.js 20+ & npm 10+
- MySQL 8 running on port 3307 (or 3306)
- Redis 7 running on port 6379

### 1. Database Setup:
Create a MySQL database named `queueless_db`:
```sql
CREATE DATABASE queueless_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Launch Spring Boot Backend:
```bash
cd backend
mvn clean spring-boot:run
```
The backend will run at `http://localhost:8080` and Flyway will automatically execute database migrations.

### 3. Launch React Frontend:
```bash
cd frontend
npm install
npm run dev
```
The frontend dev server will run at `http://localhost:3000`.
