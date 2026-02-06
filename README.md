# Sports Management System

## Overview
A full-stack sports management platform with role-based access, club/team/player management, fixtures/results, standings, and a complete posts/news module with comments and image upload.

## Repository Structure
- `/backend` Spring Boot 3.x application
- `/frontend` React + Vite + TypeScript application
- `docker-compose.yml` PostgreSQL (and pgAdmin)

## Backend Setup

### Prerequisites
- Java 17
- Maven
- Docker (for PostgreSQL)

### Start PostgreSQL
```bash
docker compose up -d
```

### Run Backend
```bash
cd backend
mvn spring-boot:run
```

Backend runs at `http://localhost:8080`.

### API Docs
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

### Seeded Admin Account
- Email: `admin@sportsms.com`
- Password: `Admin123!`

## Frontend Setup

### Prerequisites
- Node.js 18+

### Run Frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`.

## Environment Variables

### Backend (Production)
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL` (e.g., `jdbc:postgresql://<host>:<port>/<db>`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_SSLMODE` (default `require`)
- `DB_POOL_MAX_SIZE` (default `10`)
- `DB_POOL_MIN_IDLE` (default `2`)
- `DB_POOL_IDLE_TIMEOUT` (default `30000`)
- `DB_POOL_MAX_LIFETIME` (default `1800000`)
- `DB_POOL_CONNECTION_TIMEOUT` (default `30000`)
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES` (default `60`)
- `JWT_REFRESH_EXPIRATION_MINUTES` (default `10080`)
- `UPLOAD_DIR` (default `uploads`)
- `UPLOAD_BASE_URL` (public URL to serve uploads)
- `CORS_ALLOWED_ORIGINS` (comma-separated frontend origins)
- `PORT` (default `8080`)

### Frontend (Production)
- `VITE_API_BASE_URL` (e.g., `https://<backend-host>`)

## Sample API Requests

### Auth
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sportsms.com","password":"Admin123!"}'
```

### Create Club (ADMIN/MANAGER)
```bash
curl -X POST http://localhost:8080/api/clubs \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Northside FC","city":"North City"}'
```

### Search Posts (Public)
```bash
curl "http://localhost:8080/api/posts/published?keyword=season&from=2025-01-01T00:00:00Z"
```

### Upload Post Image (ADMIN)
```bash
curl -X POST http://localhost:8080/api/uploads \
  -H "Authorization: Bearer <TOKEN>" \
  -F "file=@/path/to/image.jpg"
```

### Add Comment (Authenticated)
```bash
curl -X POST http://localhost:8080/api/comments/post/<POST_ID> \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"content":"Great announcement!"}'
```

## Notes
- JWT tokens are stored in localStorage in the frontend.
- CORS is configured via `CORS_ALLOWED_ORIGINS`.

## Deployment (Render + Vercel) - Step by Step

> Update the placeholders below with your actual URLs after deployment.

### Live URLs
- Frontend: `https://<your-frontend-domain>`
- Backend: `https://<your-backend-domain>`

### 1) Backend Deployment (Render)
1. **Create a new Render Web Service** from the `backend` folder.
2. **Build command**:
   ```bash
   mvn -q -DskipTests package
   ```
3. **Start command**:
   ```bash
   java -jar target/sports-management-system-1.0.0.jar
   ```
4. **Environment variables** (set in Render):
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SSLMODE`
   - `JWT_SECRET`
   - `UPLOAD_BASE_URL` (e.g., `https://<your-backend-domain>/uploads`)
   - `CORS_ALLOWED_ORIGINS=https://<your-frontend-domain>`
5. **Health check**:
   - `https://<your-backend-domain>/actuator/health`

### 2) Managed PostgreSQL (Render)
1. **Create a Render PostgreSQL database** and copy the connection details.
2. **Set connection variables** in the backend service:
   - `DB_URL=jdbc:postgresql://<host>:<port>/<db>`
   - `DB_USERNAME=<user>`
   - `DB_PASSWORD=<password>`
   - `DB_SSLMODE=require` (Render requires SSL)
3. **Migrations** run automatically via Flyway on application startup.
4. **Connection pooling** uses HikariCP with defaults that can be tuned via env vars.

### 3) Frontend Deployment (Vercel)
1. **Create a Vercel project** pointing to the `frontend` folder.
2. **Environment variable**:
   - `VITE_API_BASE_URL=https://<your-backend-domain>`
3. **Build** (Vercel default):
   ```bash
   npm install
   npm run build
   ```
4. **SPA fallback**:
   - Vercel automatically serves `index.html` for client-side routes in SPA mode.

### 4) Security & Production Checks
- Ensure HTTPS is enabled for both frontend and backend.
- Confirm `CORS_ALLOWED_ORIGINS` matches the frontend URL.
- Validate JWT auth and role-based endpoints (ADMIN/USER) in production.
- Confirm no secrets are committed and all secrets are set via environment variables.

### 5) Rollback Instructions
- **Backend**: redeploy the last successful Render build or roll back to a previous Git commit and redeploy.
- **Database**: restore from Render PostgreSQL backups/snapshots before the migration that failed.
- **Frontend**: in Vercel, revert to the previous successful deployment.

### Deployment Verification Checklist
- [ ] `/actuator/health` returns `UP`.
- [ ] API endpoints respond correctly using production base URLs.
- [ ] Login works and returns JWT tokens.
- [ ] Role-based routes enforce ADMIN/USER access.
- [ ] File uploads are accessible via `UPLOAD_BASE_URL`.
- [ ] Frontend routing works on refresh.

### Common Pitfalls
- **CORS errors**: verify `CORS_ALLOWED_ORIGINS` includes the exact frontend domain.
- **Database SSL errors**: set `DB_SSLMODE=require` for Render.
- **401/403 in production**: ensure the frontend points to the correct `VITE_API_BASE_URL`.
- **Health check failure**: confirm `SPRING_PROFILES_ACTIVE=prod` and actuator dependency is enabled.
