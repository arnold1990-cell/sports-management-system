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

#### Database host note (fix for `UnknownHostException: postgres`)
If you see `UnknownHostException: postgres` when starting the backend from your IDE or terminal, your `DB_URL` is pointing at the Docker network hostname. That hostname only resolves **inside** Docker. For local runs, either unset `DB_URL` or set it to:
```
jdbc:postgresql://localhost:5432/sportsms
```
Use `backend/.env.example` as a reference for local values.

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

### Backend (`backend/.env.example`)
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `UPLOAD_DIR`, `UPLOAD_BASE_URL`

### Frontend (`frontend/.env.example`)
- `VITE_API_URL` (default `http://localhost:8080`)

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
- CORS is configured for `http://localhost:5173`.
