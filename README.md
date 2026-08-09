# AI-Bug Tracker

Full-stack bug tracking system with AI-generated Playwright test scripts.

## Prerequisites

- **Java 17** — runtime
- **PostgreSQL 15** — database
- **Node.js 18+** — to execute Playwright tests
- **Playwright** — for test execution

## Setup

### 1. Database

```sql
CREATE DATABASE aibt_db;
CREATE USER aibt_user WITH PASSWORD 'aibt_pass';
GRANT ALL PRIVILEGES ON DATABASE aibt_db TO aibt_user;
```

### 2. Environment Variables

```bash
export DEEPSEEK_API_KEY=sk-your-key-here
export DEEPSEEK_MODEL=deepseek-v4-pro        # optional, has default
export DEEPSEEK_TEMP=0.3                      # optional, has default
export DEEPSEEK_URL=https://api.deepseek.com/chat/completions  # optional
```

### 3. Node.js & Playwright

```bash
brew install node              # macOS
npm install -g playwright
npx playwright install chromium
```

Verify:
```bash
node --version
npx playwright --version
```

### 4. Run Backend

```bash
./mvnw spring-boot:run
```

Server starts at `http://localhost:8080`.

### 5. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:3000` (or next available port e.g. `http://localhost:3002`).

### 6. Run via Docker Compose

```bash
docker-compose up --build
```

### 7. Run Unit Tests

```bash
./mvnw test
```

## API Endpoints

| Method | Endpoint | Auth | Role |
|--------|----------|------|------|
| POST | `/api/auth/signup` | Public | — |
| POST | `/api/auth/login` | Public | — |
| POST | `/api/auth/logout` | JWT | Any |
| GET | `/api/bugs` | JWT | All roles (scoped) |
| GET | `/api/bugs/{id}` | JWT | All roles |
| POST | `/api/bugs` | JWT | ADMIN, TESTER |
| PUT | `/api/bugs/{id}` | JWT | ADMIN |
| DELETE | `/api/bugs/{id}` | JWT | ADMIN |
| PATCH | `/api/bugs/{id}/status` | JWT | DEVELOPER |
| PATCH | `/api/bugs/{id}/cancel` | JWT | TESTER |
| GET | `/api/bugs/{id}/test-result` | JWT | All roles (scoped) |
| GET | `/api/users` | JWT | ADMIN |
| PUT | `/api/users/{id}` | JWT | ADMIN |
| DELETE | `/api/users/{id}` | JWT | ADMIN |
| PATCH | `/api/users/password` | JWT | Any |

## Sprint Status

| Sprint | Description | Status |
|--------|-------------|--------|
| S1 | Foundation & JWT Authentication | ✅ Done |
| S2 | Bug CRUD APIs & Scoping | ✅ Done |
| S3 | User Management APIs | ✅ Done |
| S4 | AI DeepSeek Integration | ✅ Done |
| S5 | Playwright Test Execution Service | ✅ Done |
| S6 | Email Notifications (Brevo API) | ✅ Done |
| S7 | Unit Testing & Hardening | ✅ Done |
| S8–S12 | React Vite Frontend & Role Dashboards | ✅ Done |
