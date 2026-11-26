# Security Policy

## Scope & Intent

SmartGiftList is an **archived learning reference** modernized with layered **Jakarta Servlet 6 MVC**, salted `PBKDF2WithHmacSHA256` password hashing, synchronizer CSRF protection (`CsrfFilter`), authentication/authorization filters (`AuthFilter`), service-layer ownership/membership checks, `http/https` URL sanitization, and JSTL/HTML escaping so it can be demonstrated safely on a local development machine.

## Local Development Defaults

Default values in [`.env.example`](./.env.example), [`docker-compose.yml`](./docker-compose.yml), and [`DatabaseConfig.java`](./src/main/java/dev/smartgiftlist/config/DatabaseConfig.java) (`smartgiftlist`/`smartgiftlist`) are **strictly for ephemeral local testing**:

- Override `DB_URL`, `DB_USER`, `DB_PASSWORD`, and `DB_SCHEMA` before running in any non-local environment.
- Never expose Tomcat HTTP port `8080` directly without TLS termination and reverse-proxy hardening.
- See [`docs/security-notes.md`](./docs/security-notes.md) for the complete control matrix and deployment boundaries.

## Reporting a Vulnerability

If you discover a security concern in this repository, please open a minimal private advisory via GitHub Security Advisories or contact the repository owner directly without posting exploitable secrets publicly.
