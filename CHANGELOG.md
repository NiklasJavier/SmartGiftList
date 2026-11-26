# Changelog

All notable changes for the public showcase release of **SmartGiftList** are recorded in this file.

## [1.1.0] — Layered Jakarta Servlet 6 MVC Refactor

### Added
- 5-layer package structure under `dev.smartgiftlist.{config,model,security,repository,service,web}` with HikariCP connection pooling (`DataSourceFactory`), SLF4J structured logging, `SchemaInitializer`, and `AppContextListener`.
- Centralized HTTP filters (`EncodingAndSecurityFilter`, `CsrfFilter`, `AuthFilter`), `/health` readiness endpoint (`HealthServlet`), and PRG controllers (`LandingServlet`, `AuthServlet`, `WishlistServlet`, `ProfileServlet`).
- Scriptlet-free JSTL 3.0 views and shared `.jspf` partials under `WEB-INF/views/` (`<scripting-invalid>true</scripting-invalid>` in `web.xml`).
- Deduplicated CSS architecture (`css/base.css` + `css/pages.css`) with CSS variables and `[data-status]` gift styling, plus 4 strict-mode ES6 modules (`js/nav.js`, `js/landing.js`, `js/auth.js`, `js/wishlists.js`).
- Expanded unit test suite (17 JUnit 5 tests across `config`, `model`, `security`, and `service` layers with in-memory repositories) and 12-step E2E suite covering auth/CSRF/IDOR guards and multi-user collaboration.

### Changed
- Upgraded gift claiming to atomic row-locked transactions (`SELECT ... FOR UPDATE`) backed by `UNIQUE (UserID, GiftID)` and `UNIQUE (UserID, WishlistID)` database constraints plus 5 lookup indexes.
- Replaced sequential wishlist NetID generation with collision-checked random `SGL-XXXX-XXXX` invite codes and nullable `EventDate` handling.
- Enabled optional-password profile updates (keeping active sessions valid without forced sign-out) and enforced strict ownership/creator checks before wishlist edits or gift deletions.

### Removed
- Legacy session-coupled `dev.smartgiftlist.beans.*` / `dev.smartgiftlist.jdbc.*` classes, root `*View.jsp`/`*Appl.jsp` scriptlet pages, monolithic `SmartWishList.css`, and legacy JavaScript globals (`Owl.js`, `Dashboard.js`, `LandingPage.js`, `Profil.js`).

## [1.0.0] — Public Showcase Release

### Added
- Reproducible Gradle WAR build (`build.gradle`, `settings.gradle`, Gradle Wrapper) targeting JDK 21 and Apache Tomcat 10.1 (`ROOT.war`).
- Initial JUnit 5 test suite covering `PasswordUtil`, `HtmlUtil`, configuration defaults, and domain formatting.
- Declarative Nix Flake (`flake.nix`, `flake.lock`, `.envrc`) providing JDK 21, Gradle, `just`, PostgreSQL 16, and Apache Tomcat 10.1, plus `justfile` recipes (`just doctor`, `just check`, `just up`, `just verify-e2e`, `just down`, `just local-up`, `just test-e2e-local`, `just local-down`).
- Multi-stage `Dockerfile`, `docker-compose.yml` (PostgreSQL 16 + Tomcat 10.1), `docker/init.sql` auto-provisioning, `scripts/stack.sh` (Docker Compose v2/v1 + Podman Compose/Podman runtime wrapper with volume teardown), `scripts/local-stack.sh` (zero-container Nix runtime), and `scripts/verify-e2e.sh`.
- Full English documentation suite, community files, GitHub Actions CI, and `.agents/` orientation skill.

### Removed
- Legacy university identifiers, hardcoded campus DB host/credentials, Eclipse workspace metadata, unreferenced mockup PDF, and legacy commit history containing contributor emails/handles.
