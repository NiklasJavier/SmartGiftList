# Changelog

All notable changes for the public showcase release of **SmartGiftList** are recorded in this file.

## [1.0.0] — Public Showcase Release

### Added
- Reproducible Gradle WAR build (`build.gradle`, `settings.gradle`, Gradle Wrapper) targeting JDK 21 and Apache Tomcat 10.1 (`ROOT.war`).
- JUnit 5 test suite covering `PasswordUtil`, `HtmlUtil`, `PostgreSQLAccess` schema validation, `AccountBean`, `Gift`, and `MessageBean`.
- Declarative Nix Flake (`flake.nix`, `flake.lock`, `.envrc`) providing JDK 21, Gradle, `just`, PostgreSQL 16, and Apache Tomcat 10.1, plus `justfile` recipes (`just doctor`, `just check`, `just up`, `just verify-e2e`, `just down`, `just local-up`, `just test-e2e-local`, `just local-down`).
- Multi-stage `Dockerfile`, `docker-compose.yml` (PostgreSQL 16 + Tomcat 10.1), `docker/init.sql` auto-provisioning, `scripts/stack.sh` (Docker Compose v2/v1 + Podman Compose/Podman runtime wrapper with volume teardown), `scripts/local-stack.sh` (zero-container Nix runtime), and `scripts/verify-e2e.sh` 7-step HTTP + PostgreSQL smoke test.
- Full English documentation suite (`README.md`, `docs/getting-started.md`, `docs/architecture.md`, `docs/database-schema.md`, `docs/security-notes.md`), community files, GitHub Actions CI, and `.agents/` orientation skill.

### Changed
- Refactored Java packages to `dev.smartgiftlist.{beans,jdbc,security}` and parameterized PostgreSQL connection settings via `DB_URL`, `DB_USER`, `DB_PASSWORD`, and `DB_SCHEMA`.
- Upgraded password storage from plaintext comparisons to salted `PBKDF2WithHmacSHA256` (`PasswordUtil`) and added HTML escaping (`HtmlUtil`) across dynamic fragments and JSP forms.
- Switched authentication, registration, profile, and mutation forms from HTTP `GET` to `POST` with consistent `UTF-8` encoding and fixed null/string comparisons.
- Translated all Java logs/messages, JSP pages, JavaScript validations, and CSS comments into idiomatic English.

### Removed
- Legacy university identifiers, hardcoded campus DB host/credentials, Eclipse workspace metadata (`.classpath`, `.project`, `.settings/`), unreferenced mockup PDF, and legacy commit history containing contributor emails/handles.
