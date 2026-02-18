# VPS Checklist: Fix `library_management_db` and force SportsMS DB (`sportsms`)

Use this checklist when backend startup fails with:

```text
org.postgresql.util.PSQLException: FATAL: database "library_management_db" does not exist
```

---

## 1) Find where the wrong DB value is coming from

Run these commands from project root (`/opt/sportsms` or your repo path):

```bash
# Check Spring config files
rg -n "library_management_db|datasource|DB_URL|SPRING_DATASOURCE_URL|DATABASE_URL" backend/src/main/resources

# Check compose + env files
rg -n "library_management_db|POSTGRES_DB|SPRING_DATASOURCE_URL|DB_URL|DATABASE_URL" docker-compose.yml deploy/systemd/*.env

# Check shell environment on VPS
printenv | egrep "SPRING_DATASOURCE_URL|DB_URL|DATABASE_URL|SPRING_DATASOURCE_USERNAME|DB_USERNAME"
```

If deployed with systemd, inspect effective service environment:

```bash
sudo systemctl cat sportsms
sudo systemctl show sportsms --property=Environment
sudo journalctl -u sportsms -n 200 --no-pager
```

If deployed with Docker Compose, inspect container environment:

```bash
docker compose ps
docker compose config
docker inspect sportsms-backend --format='{{range .Config.Env}}{{println .}}{{end}}' | egrep "SPRING_DATASOURCE_URL|DB_URL|DATABASE_URL"
```

> IntelliJ note: local Run/Debug configs can override app properties via `Environment variables`. Verify in **Run > Edit Configurations > your Spring Boot config**.

---

## 2A) Docker Compose fix (recommended container deployment)

`docker-compose.yml` should include:

- `db` service with `POSTGRES_DB=sportsms`
- backend env:
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/sportsms`
  - `SPRING_DATASOURCE_USERNAME=postgres`
  - `SPRING_DATASOURCE_PASSWORD=...`
- DB healthcheck + backend `depends_on: condition: service_healthy`

Redeploy commands:

```bash
docker compose down
docker compose up -d --build
docker compose logs -f backend
```

---

## 2B) systemd JAR fix (non-container backend)

Use `/etc/systemd/system/sportsms.service` like:

```ini
[Service]
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sportsms
Environment=SPRING_DATASOURCE_USERNAME=postgres
Environment=SPRING_DATASOURCE_PASSWORD=change-me
EnvironmentFile=-/opt/sportsms/sportsms.env
ExecStart=/usr/bin/java -jar /opt/sportsms/app.jar
```

Apply changes:

```bash
sudo systemctl daemon-reload
sudo systemctl restart sportsms
sudo journalctl -u sportsms -f
```

---

## 3) Database creation and verification on VPS

Check existing DBs:

```bash
psql -U postgres -c "\l"
```

Create DB if missing:

```bash
createdb -U postgres sportsms
```

Grant privileges:

```bash
psql -U postgres -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE sportsms TO postgres;"
```

Connectivity test:

```bash
psql -U postgres -d sportsms -c "SELECT current_database(), current_user;"
```

Confirm tables (Flyway/JPA objects):

```bash
psql -U postgres -d sportsms -c "\dt"
psql -U postgres -d sportsms -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"
```

---

## 4) Flyway-specific checks

Expected Spring config:

- `spring.flyway.enabled=true`
- `spring.flyway.locations=classpath:db/migration`

If Flyway fails:

1. Verify migration files exist in `backend/src/main/resources/db/migration`.
2. Verify DB user can create schema objects in `sportsms`.
3. For permission issues, run:

```bash
psql -U postgres -d sportsms -c "GRANT ALL ON SCHEMA public TO postgres;"
psql -U postgres -d sportsms -c "ALTER SCHEMA public OWNER TO postgres;"
```

4. Restart app and watch logs again.

---

## 5) Expected success logs

In `docker compose logs -f backend` or `journalctl -u sportsms -f`, you should see lines similar to:

```text
HikariPool-1 - Start completed.
Flyway Community Edition ...
Successfully applied ... migrations to schema "public" ...
Tomcat started on port(s): 8080 (http)
Started SportsManagementSystemApplication in ... seconds
```

If these lines appear and `/api-docs` responds on `:8080`, deployment is healthy.
