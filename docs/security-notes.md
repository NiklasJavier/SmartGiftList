# Security & Hardening Notes

SmartGiftList began as a prototype built with JSP scriptlets. During public showcase hardening and architectural refactoring into a layered Jakarta Servlet 6 MVC application, authentication, session, input, authorization, and persistence controls were systematically rebuilt.

---

## 1. Security Controls Implemented

| Area | Original Prototype Behavior | Current Implementation |
| :--- | :--- | :--- |
| **Password Storage** | Plaintext passwords stored and queried directly via `WHERE Password = ?` | Salted `PBKDF2WithHmacSHA256` (`120,000` iterations, 16-byte `SecureRandom` salt, constant-time `MessageDigest.isEqual`) via [`PasswordUtil`](../src/main/java/dev/smartgiftlist/security/PasswordUtil.java) |
| **CSRF Protection** | Unprotected HTML forms and `GET` navigation mutations | Per-session 32-byte `SecureRandom` tokens validated via constant-time compare in [`CsrfFilter`](../src/main/java/dev/smartgiftlist/web/CsrfFilter.java) (`403` on missing/invalid `_csrf` on all `POST` endpoints) |
| **Route Authentication & Session Hygiene** | Session checks duplicated inside every JSP scriptlet; session object stored DB connection | Centralized [`AuthFilter`](../src/main/java/dev/smartgiftlist/web/AuthFilter.java) redirects unauthenticated `/wishlists` & `/profile` requests to `/login`; [`AuthServlet`](../src/main/java/dev/smartgiftlist/web/AuthServlet.java) calls `changeSessionId()` on login/password change and [`SessionState`](../src/main/java/dev/smartgiftlist/web/SessionState.java) stores only scalar IDs/flash strings |
| **Authorization / IDOR Prevention** | `setNetID` accepted arbitrary values; gifts could be viewed or deleted without membership verification | Service-layer enforcement in [`WishlistService`](../src/main/java/dev/smartgiftlist/service/WishlistService.java), [`GiftService`](../src/main/java/dev/smartgiftlist/service/GiftService.java), and [`CommentService`](../src/main/java/dev/smartgiftlist/service/CommentService.java): wishlist updates require ownership; gift views/claims/comments require owner or participant membership; gift deletion requires owner or creator |
| **Atomic Concurrency Control** | Separate `SELECT COUNT` and `INSERT` with TOCTOU race condition | Single transaction with `SELECT NumberOfPeople FROM Gifts WHERE GiftID = ? FOR UPDATE` + DB `UNIQUE (UserID, GiftID)` in [`JdbcGiftRepository`](../src/main/java/dev/smartgiftlist/repository/JdbcGiftRepository.java) |
| **XSS, URL & Asset Safety** | Unescaped Java HTML string assembly and arbitrary `url`/`icon` strings | Zero HTML in Java/services; JSTL `<c:out>` + `<scripting-invalid>true</scripting-invalid>` in [`web.xml`](../src/main/webapp/WEB-INF/web.xml); [`HtmlUtil.sanitizeHttpUrl`](../src/main/java/dev/smartgiftlist/security/HtmlUtil.java) restricts links to `http/https`; [`IconCatalog`](../src/main/java/dev/smartgiftlist/model/IconCatalog.java) allowlists avatars & covers |
| **Database Config & Schema Isolation** | Hardcoded campus DB host, static shared credentials, and hardcoded legacy student schema prefix | Environment-configured (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_SCHEMA`) in [`DatabaseConfig`](../src/main/java/dev/smartgiftlist/config/DatabaseConfig.java) with regex schema validation (`^[a-zA-Z_][a-zA-Z0-9_]*$`) + HikariCP `connectionInitSql` |
| **HTTP Security Headers** | Default Tomcat headers only | [`EncodingAndSecurityFilter`](../src/main/java/dev/smartgiftlist/web/EncodingAndSecurityFilter.java) forces `UTF-8`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: same-origin`, and `Cache-Control: no-store` on dynamic views |

---

## 2. Deployment Boundaries

- **Local Development Defaults:** Default PostgreSQL credentials in [`.env.example`](../.env.example), [`docker-compose.yml`](../docker-compose.yml), and `DatabaseConfig` (`smartgiftlist`/`smartgiftlist`) are strictly for ephemeral local verification.
- **No TLS Termination Inside Tomcat:** `Dockerfile` exposes HTTP on `8080`; any remote deployment must terminate TLS in front of Tomcat and supply strong database credentials via environment variables.
