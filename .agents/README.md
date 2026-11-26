# `.agents` — Repository Orientation for Agents & Contributors

> **Context:** SmartGiftList is preserved as a completed, archived reference showcasing a first web development project modernized with Gradle, JUnit 5, HikariCP, Jakarta Servlet 6 MVC, JSTL 3.0, Nix, and Docker/Podman. There is no active feature roadmap.

## Read Order

1. [`README.md`](../README.md) — project overview, End-to-End Quickstart (macOS/Linux/Windows-WSL2 Nix + `just`), architecture diagram, and features.
2. [`justfile`](../justfile) & [`flake.nix`](../flake.nix) — canonical command surface (`just check`, `just up`, `just verify-e2e`, `just down`, `just test-e2e-local`).
3. [`.agents/skills/smartgiftlist-navigator/SKILL.md`](./skills/smartgiftlist-navigator/SKILL.md) — module map, commands, servlet contracts, and privacy invariants.
4. [`docs/architecture.md`](../docs/architecture.md) & [`docs/database-schema.md`](../docs/database-schema.md) — layered MVC lifecycle and PostgreSQL tables.

## Repository Map

| Path | Component | Key Entry Points |
| :--- | :--- | :--- |
| [`src/main/java/dev/smartgiftlist/config/`](../src/main/java/dev/smartgiftlist/config) | Startup, Pooling & DDL | `DatabaseConfig`, `DataSourceFactory`, `SchemaInitializer`, `ServiceRegistry`, `AppContextListener` |
| [`src/main/java/dev/smartgiftlist/model/`](../src/main/java/dev/smartgiftlist/model) | Domain Value Objects | `User`, `Wishlist`, `Gift`, `GiftStatus`, `GiftClaimer`, `WishlistParticipant`, `ChatComment`, `FlashMessage`, `DashboardData`, `IconCatalog` |
| [`src/main/java/dev/smartgiftlist/repository/`](../src/main/java/dev/smartgiftlist/repository) | Pooled JDBC Persistence | `JdbcUserRepository`, `JdbcWishlistRepository`, `JdbcGiftRepository`, `JdbcCommentRepository` |
| [`src/main/java/dev/smartgiftlist/service/`](../src/main/java/dev/smartgiftlist/service) | Business Logic & AuthZ | `AuthService`, `WishlistService`, `GiftService`, `CommentService`, `ServiceResult` |
| [`src/main/java/dev/smartgiftlist/web/`](../src/main/java/dev/smartgiftlist/web) | Filters & Servlets | `EncodingAndSecurityFilter`, `CsrfFilter`, `AuthFilter`, `HealthServlet`, `LandingServlet`, `AuthServlet`, `WishlistServlet`, `ProfileServlet` |
| [`src/main/java/dev/smartgiftlist/security/`](../src/main/java/dev/smartgiftlist/security) | Hashing, CSRF & XSS | `PasswordUtil`, `HtmlUtil`, `CsrfTokenUtil` |
| [`src/main/webapp/WEB-INF/views/`](../src/main/webapp/WEB-INF/views) | Scriptlet-Free JSTL Views | `landing.jsp`, `login.jsp`, `register.jsp`, `wishlists.jsp`, `profile.jsp`, `partials/` |
| [`src/main/webapp/css/`](../src/main/webapp/css) · [`js/`](../src/main/webapp/js) | Deduplicated Styles & ES6 | `base.css`, `pages.css`, `nav.js`, `landing.js`, `auth.js`, `wishlists.js` |
| [`src/test/java/dev/smartgiftlist/`](../src/test/java/dev/smartgiftlist) | JUnit 5 Test Suite | `DatabaseConfigTest`, `DomainModelTest`, `PasswordUtilTest`, `HtmlUtilTest`, `CsrfTokenUtilTest`, `*ServiceTest` |
| [`docker/`](../docker) · [`scripts/`](../scripts) | Container & E2E Stack | `docker/init.sql`, `docker-compose.yml`, `Dockerfile`, `scripts/stack.sh`, `scripts/local-stack.sh`, `scripts/verify-e2e.sh` |
| [`docs/`](../docs) | Guides & Reference Docs | `getting-started.md`, `architecture.md`, `database-schema.md`, `security-notes.md` |

## Command Surface (`nix` + `just`)

```bash
nix develop         # Enter JDK 21 + Gradle + PostgreSQL 16 + Tomcat 10.1 + just devShell
just doctor         # Verify local toolchain, container runtime (Docker/Podman), and project files
just check          # Build WAR, run JUnit 5 tests, lint scripts/configs/docs/privacy
just up             # Boot PostgreSQL 16 + Tomcat 10.1 via Docker or Podman
just verify-e2e     # Run live 12-step HTTP/PostgreSQL end-to-end smoke test
just down           # Tear down containers, networks, and DB volumes
just local-up       # Boot ephemeral Nix PostgreSQL 16 + Tomcat 10.1 without Docker/Podman
just test-e2e-local # Run local-up -> verify-e2e -> local-down automatically
just local-down     # Stop local Nix services and delete .local-runtime state
```
