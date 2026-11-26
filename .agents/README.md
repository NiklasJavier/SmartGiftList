# `.agents` — Repository Orientation for Agents & Contributors

> **Context:** SmartGiftList is preserved as a completed, archived reference showcasing a first web development project modernized with Gradle, JUnit 5, Nix, and Docker Compose. There is no active feature roadmap.

## Read Order

1. [`README.md`](../README.md) — project overview, End-to-End Quickstart (macOS/Linux/Windows-WSL2 Nix + `just`), architecture diagram, and features.
2. [`justfile`](../justfile) & [`flake.nix`](../flake.nix) — canonical command surface (`just check`, `just up`, `just verify-e2e`, `just down`).
3. [`.agents/skills/smartgiftlist-navigator/SKILL.md`](./skills/smartgiftlist-navigator/SKILL.md) — module map, commands, form contracts, and privacy invariants.
4. [`docs/architecture.md`](../docs/architecture.md) & [`docs/database-schema.md`](../docs/database-schema.md) — JSP View/Handler flow and PostgreSQL tables.

## Repository Map

| Path | Component | Key Entry Points |
| :--- | :--- | :--- |
| [`src/main/java/dev/smartgiftlist/beans/`](../src/main/java/dev/smartgiftlist/beans) | Session Facade & Managers | `OwnWishListBean`, `AccountBean`, `LoginBean`, `WishlistManager`, `GiftManager`, `ChatManager`, `ProfilManager`, `MessageBean` |
| [`src/main/java/dev/smartgiftlist/jdbc/`](../src/main/java/dev/smartgiftlist/jdbc) | PostgreSQL Connectivity | `PostgreSQLAccess`, `JDBCAccess`, `AppTables`, `NoConnectionException` |
| [`src/main/java/dev/smartgiftlist/security/`](../src/main/java/dev/smartgiftlist/security) | Hashing & Escaping | `PasswordUtil`, `HtmlUtil` |
| [`src/main/webapp/jsp/`](../src/main/webapp/jsp) | Views & Action Handlers | `LandingView`, `LoginView`, `RegView`, `OwnWishListView`, `ProfilView` + `*Appl.jsp` |
| [`src/test/java/dev/smartgiftlist/`](../src/test/java/dev/smartgiftlist) | JUnit 5 Test Suite | `PasswordUtilTest`, `HtmlUtilTest`, `PostgreSQLAccessConfigTest`, `DomainAndMessageBeanTest` |
| [`docker/`](../docker) · [`scripts/`](../scripts) | Container & E2E Stack | `docker/init.sql`, `docker-compose.yml`, `Dockerfile`, `scripts/stack.sh`, `scripts/local-stack.sh`, `scripts/verify-e2e.sh` |
| [`docs/`](../docs) | Guides & Reference Docs | `getting-started.md`, `architecture.md`, `database-schema.md`, `security-notes.md` |

## Command Surface (`nix` + `just`)

```bash
nix develop         # Enter JDK 21 + Gradle + PostgreSQL 16 + Tomcat 10.1 + just devShell
just doctor         # Verify local toolchain, container runtime (Docker/Podman), and project files
just check          # Build WAR, run JUnit 5 tests, lint scripts/configs/docs/privacy
just up             # Boot PostgreSQL 16 + Tomcat 10.1 via Docker or Podman
just verify-e2e     # Run live 7-step HTTP/PostgreSQL end-to-end smoke test
just down           # Tear down containers, networks, and DB volumes
just local-up       # Boot ephemeral Nix PostgreSQL 16 + Tomcat 10.1 without Docker/Podman
just test-e2e-local # Run local-up -> verify-e2e -> local-down automatically
just local-down     # Stop local Nix services and delete .local-runtime state
```
