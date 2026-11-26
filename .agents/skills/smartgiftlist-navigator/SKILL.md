---
name: smartgiftlist-navigator
description: >-
  Orient and navigate the SmartGiftList repository (Java 21 Jakarta Servlet 6 MVC, JSTL 3.0,
  HikariCP + PostgreSQL 16, Tomcat 10.1, Gradle WAR build, Nix flake.nix, justfile, and
  Docker/Podman E2E stack). Use when exploring SmartGiftList modules, running builds/tests/E2E checks,
  or inspecting servlet contracts and privacy boundaries. Triggers: smartgiftlist, orient,
  just check, nix develop, verify-e2e, servlet, jstl. NOT: unrelated repositories or production SaaS deployment.
---

# smartgiftlist-navigator — Codebase & DX Guide

## Repository Scope

SmartGiftList is an archived reference web application showcasing Niklas's first software/web project, refactored into a clean 5-layer Jakarta Servlet 6 MVC + JSTL 3.0 + HikariCP architecture (gift wishlists, `SGL-*` `NetID` sharing, atomic group/surprise gift claims, and wishlist discussion chat). Treat it as a completed educational/reference codebase rather than an active product roadmap.

## Canonical Entry Points

| Concern | Location | What Lives There |
| :--- | :--- | :--- |
| **DX & Automation** | [`flake.nix`](../../../flake.nix) · [`justfile`](../../../justfile) | Reproducible `JDK 21` + `Gradle` + `just` shell and verification recipes |
| **Runtime Stack** | [`docker-compose.yml`](../../../docker-compose.yml) · [`Dockerfile`](../../../Dockerfile) · [`docker/init.sql`](../../../docker/init.sql) | Multi-stage Gradle -> Tomcat 10.1 `ROOT.war` and auto-initialized PostgreSQL 16 |
| **Config & Startup** | [`DatabaseConfig.java`](../../../src/main/java/dev/smartgiftlist/config/DatabaseConfig.java) · [`AppContextListener.java`](../../../src/main/java/dev/smartgiftlist/config/AppContextListener.java) | HikariCP pooling, `DB_*` resolution, schema validation, and startup DDL assurance |
| **Security Helpers** | [`PasswordUtil.java`](../../../src/main/java/dev/smartgiftlist/security/PasswordUtil.java) · [`HtmlUtil.java`](../../../src/main/java/dev/smartgiftlist/security/HtmlUtil.java) · [`CsrfTokenUtil.java`](../../../src/main/java/dev/smartgiftlist/security/CsrfTokenUtil.java) | PBKDF2 hashing, URL sanitization, HTML escaping, and CSRF token validation |
| **Services & Repos** | [`WishlistService.java`](../../../src/main/java/dev/smartgiftlist/service/WishlistService.java) · [`JdbcGiftRepository.java`](../../../src/main/java/dev/smartgiftlist/repository/JdbcGiftRepository.java) | Ownership/IDOR authorization and row-locked atomic gift claiming |
| **Web & Views** | [`WishlistServlet.java`](../../../src/main/java/dev/smartgiftlist/web/WishlistServlet.java) · [`wishlists.jsp`](../../../src/main/webapp/WEB-INF/views/wishlists.jsp) · [`wishlists.js`](../../../src/main/webapp/js/wishlists.js) | PRG controllers behind `CsrfFilter`/`AuthFilter` and scriptlet-free JSTL views |
| **Live E2E Smoke Check** | [`verify-e2e.sh`](../../../scripts/verify-e2e.sh) | Automated 12-step HTTP + CSRF + IDOR + PostgreSQL smoke verification |

## Verified Commands (`nix develop -c just ...`)

| Recipe | Purpose |
| :--- | :--- |
| `nix develop -c just doctor` | Verify `java`, `gradle`, `curl`, `shellcheck`, `python3`, `ruby`, `postgres`, `CATALINA_HOME`, and Docker/Podman status |
| `nix develop -c just check` | Build `SmartGiftList.war`, run JUnit 5 tests, lint scripts/configs/docs, and run privacy scan |
| `nix develop -c just up` | Build containers and start PostgreSQL 16 + Tomcat 10.1 via `scripts/stack.sh` (Docker or Podman) |
| `nix develop -c just verify-e2e` | Execute 12-step end-to-end HTTP + PostgreSQL verification against `http://localhost:8080` |
| `nix develop -c just down` | Stop containers and remove networks and PostgreSQL volumes |
| `nix develop -c just local-up` | Start ephemeral Nix PostgreSQL 16 + Tomcat 10.1 without Docker/Podman (`scripts/local-stack.sh`) |
| `nix develop -c just test-e2e-local` | Run `local-up`, `verify-e2e`, and guaranteed `local-down` cleanup |
| `nix develop -c just local-down` | Stop ephemeral Nix services and delete `.local-runtime/` |

## Key Invariants & Pitfalls

1. **Privacy & Identity Isolation:** Never commit university identifiers, student IDs, private emails/names, or shared campus database hosts/credentials; `just check-privacy` enforces zero matches across the repository.
2. **CSRF & Scriptlet Enforcement:** `WEB-INF/web.xml` enforces `<scripting-invalid>true</scripting-invalid>`; every `POST` form must include `_csrf`, and `AuthFilter`/`CsrfFilter` protect `/wishlists` and `/profile`.
3. **Gift Status Keys:** `Gift.getStatusKey()` emits `single`, `group`, `surprise`, `claimed` on `<tr data-status="...">`, styled directly in [`pages.css`](../../../src/main/webapp/css/pages.css); keep those keys synchronized.
4. **JavaBean Getters for Jakarta EL 5.0:** Domain classes in `dev.smartgiftlist.model` intentionally use immutable `final class` + `getXxx()`/`isXxx()` methods rather than Java `record` accessors so Tomcat 10.1 EL `${...}` expressions resolve cleanly.

## Do NOT

- Do NOT hardcode database credentials, schemas, or local filesystem paths in Java sources or build files; see [`SECURITY.md`](../../../SECURITY.md).
- Do NOT expose the default development credentials from [`.env.example`](../../../.env.example) outside local testing.
- Do NOT introduce German prose or personal data into user-facing strings, documentation, or git commit metadata.
