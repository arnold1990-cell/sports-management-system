# Sports Management System

## Overview
A full-stack sports management platform with role-based access, club/team/player management, fixtures/results, standings, and a complete posts/news module with comments and image upload.

## Repository Structure
- `/backend` Spring Boot 3.x application
- `/frontend` React + Vite + TypeScript application
- `/deploy/systemd` production systemd templates
- `/deploy/nginx` production nginx reverse-proxy template
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

### Run Backend (local)
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

### Run Frontend (development)
```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`.

### Build Frontend (production bundle)
```bash
cd frontend
npm run build
```

### Serve Frontend Bundle (simple static plan)
```bash
cd frontend
npx serve -s dist -l 4173
```

## Environment Variables

### Backend
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `UPLOAD_DIR`, `UPLOAD_BASE_URL`
- `APP_CORS_ALLOWED_ORIGINS` (comma-separated allowlist)

### Frontend
- `VITE_API_BASE_URL` (for axios base URL)

Example files are provided in:
- `frontend/.env.development`
- `frontend/.env.production`

## VPS Deployment

### systemd backend service
Use templates from:
- `deploy/systemd/sportsms.service`
- `deploy/systemd/sportsms.env`

Typical install commands:
```bash
sudo cp deploy/systemd/sportsms.service /etc/systemd/system/sportsms.service
sudo cp deploy/systemd/sportsms.env /opt/sportsms/sportsms.env
sudo systemctl daemon-reload
sudo systemctl enable --now sportsms
sudo systemctl status sportsms
```

### Optional nginx reverse proxy
Use template:
- `deploy/nginx/sportsms.conf`

Typical install commands:
```bash
sudo cp deploy/nginx/sportsms.conf /etc/nginx/sites-available/sportsms
sudo ln -s /etc/nginx/sites-available/sportsms /etc/nginx/sites-enabled/sportsms
sudo nginx -t
sudo systemctl reload nginx
```

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
- JWT tokens are stored in localStorage in the frontend and attached automatically by `frontend/src/api/client.ts`.
- CORS allowlist is configured in backend security with `APP_CORS_ALLOWED_ORIGINS`.
