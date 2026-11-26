# Getting Started

This guide covers running **SmartGiftList** locally from a clean checkout using **Nix + `just`** with either **Docker / Podman containers** (`scripts/stack.sh`) or the **zero-container Nix stack** (`scripts/local-stack.sh`), as well as manual builds without Nix.

---

## 1. Prerequisites

| Tool | Purpose |
| :--- | :--- |
| **Docker or Podman** | Runs PostgreSQL 16 (`smartgiftlist-postgres`) and Apache Tomcat 10.1 (`smartgiftlist-app`) via `scripts/stack.sh` (supports Docker Compose v2/v1, `podman compose`, `podman-compose`, and rootless `podman` fallback) |
| **Nix (Recommended)** | Declarative dev shell (`flake.nix`) supplying **JDK 21**, **Gradle**, `just`, `curl`, `jq`, `shellcheck`, `python3`, `ruby`, **PostgreSQL 16**, and **Apache Tomcat 10.1** |
| **Java 21 (Manual Fallback)** | Needed only when invoking `./gradlew` directly outside `nix develop` |

### Install Nix with Flakes Enabled

- **macOS / Linux:**
  ```bash
  curl --proto '=https' --tlsv1.2 -sSf -L https://install.determinate.systems/nix | sh -s -- install
  ```
  (Official upstream alternative: [nixos.org/download](https://nixos.org/download/).)
- **Windows:** Enable [WSL2](https://learn.microsoft.com/en-us/windows/wsl/install) and turn on **Docker Desktop -> Settings -> Resources -> WSL Integration**, then run the same Linux installer inside your WSL2 distribution (`scripts/verify-e2e.sh` requires a POSIX shell).

---

## 2. Recommended End-to-End Flow (`nix` + `just`)

```bash
git clone https://github.com/NiklasJavier/SmartGiftList.git
cd SmartGiftList
nix develop

# 1. Verify tools, compile SmartGiftList.war, run JUnit 5 tests, lint scripts/configs/docs
just doctor
just check

# 2. Start PostgreSQL 16 (auto-initialized via docker/init.sql) and Tomcat 10.1 via Docker or Podman
just up

# 3. Run automated 7-step HTTP + PostgreSQL smoke check
just verify-e2e

# 4. Inspect logs or open http://localhost:8080/ in your browser
just logs

# 5. Stop containers, remove network state, and delete ephemeral DB volumes
just down
```

### Zero-Container Nix Flow (`local-up` / `test-e2e-local` / `local-down`)

When Docker or Podman is not installed, `flake.nix` includes PostgreSQL 16 and Apache Tomcat 10.1 directly:

```bash
# Single command: boot ephemeral Nix PostgreSQL + Tomcat, run verify-e2e, and wipe .local-runtime/
just test-e2e-local

# Or manage the native runtime manually:
just local-up
just verify-e2e
just local-down
```

All temporary Unix sockets, PostgreSQL `PGDATA`, and Tomcat `CATALINA_BASE` files live inside `.local-runtime/` and are completely removed by `just local-down`.

### What `just verify-e2e` Validates

[`scripts/verify-e2e.sh`](../scripts/verify-e2e.sh) runs a non-destructive 7-step HTTP + PostgreSQL smoke test against the live stack (`http://localhost:8080`), covering:

- Polling `GET /jsp/LandingView.jsp` for HTTP `200` and expected landing content.
- Registering a uniquely named temporary test account (`POST /jsp/RegAppl.jsp`) to test PBKDF2 password hashing and PostgreSQL `Users` persistence.
- Signing in (`POST /jsp/LoginAppl.jsp`) with session cookies (`curl -b/-c`) and verifying redirect to the authenticated dashboard (`My Wishlists`).
- Creating a wishlist (`POST /jsp/OwnWishListAppl.jsp`) and gift entry (`E2E Mug`) in PostgreSQL and confirming they render in HTML.
- Signing out cleanly and confirming the unauthenticated public state.

---

## 3. Running Without Nix (Manual Gradle & Docker)

If you already have **JDK 21** and **Docker Compose v2** installed locally:

```bash
# Build WAR and run unit tests
./gradlew --no-daemon war test

# Boot PostgreSQL + Tomcat
docker compose up -d --build

# Run the HTTP + DB smoke test
chmod +x scripts/verify-e2e.sh
./scripts/verify-e2e.sh

# Tear down cleanly
docker compose down -v
```

---

## 4. Environment Configuration

Copy [`.env.example`](../.env.example) to `.env` before customizing ports or credentials:

| Variable | Default (`docker-compose.yml`) | Default (`PostgreSQLAccess`) | Purpose |
| :--- | :--- | :--- | :--- |
| `APP_PORT` | `8080` | `8080` | Host port mapped to Tomcat HTTP (`8080`) |
| `PG_PORT` | `5432` | `5432` | Host port mapped to PostgreSQL (`5432`) |
| `POSTGRES_DB` | `smartgiftlist` | — | Database created by `postgres:16-alpine` |
| `DB_URL` | `jdbc:postgresql://postgres:5432/smartgiftlist` | `jdbc:postgresql://localhost:5432/smartgiftlist` | JDBC connection URL |
| `DB_USER` | `smartgiftlist` | `smartgiftlist` | PostgreSQL role name |
| `DB_PASSWORD` | `smartgiftlist` | `smartgiftlist` | PostgreSQL role password (local dev only) |
| `DB_SCHEMA` | `smartgiftlist` | `smartgiftlist` | PostgreSQL schema applied via `SET search_path` |

---

## 5. Manual Schema Reset (`AppTables`)

When using `docker compose up`, [`docker/init.sql`](../docker/init.sql) creates the `smartgiftlist` schema and all six tables automatically on first volume initialization. If you point the application at an external empty PostgreSQL database, you can recreate tables via [`AppTables`](../src/main/java/dev/smartgiftlist/jdbc/AppTables.java) or execute `docker/init.sql` directly with `psql`.

---

## 6. Troubleshooting

| Symptom | Cause | Fix |
| :--- | :--- | :--- |
| `PostgreSQLAccess` throws `NoConnectionException` | PostgreSQL not reachable or wrong `DB_URL` | Wait for `smartgiftlist-postgres` healthcheck (`docker compose ps`) or check `.env` |
| Schema changes in `docker/init.sql` ignored on restart | Docker named volume already initialized | Recreate volume cleanly via `just down && just up` |
| `just verify-e2e` times out waiting for `8080` | Tomcat still starting or non-default `APP_PORT` | Run `SMARTGIFTLIST_URL=http://localhost:<port> ./scripts/verify-e2e.sh` |
