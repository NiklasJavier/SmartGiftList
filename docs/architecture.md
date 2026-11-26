# Architecture Overview

SmartGiftList is a server-rendered Java 21 web application built on **Jakarta Servlet 6 MVC**, **JSTL 3.0 views** (`<scripting-invalid>true</scripting-invalid>`), **HikariCP connection pooling**, and explicit **JDBC `PreparedStatement`** repositories in PostgreSQL 16.

---

## 1. Request Lifecycle & Routes (`Filter -> Servlet -> /WEB-INF/views/*.jsp`)

All state-changing actions follow **Post/Redirect/Get (`PRG`)** behind `EncodingAndSecurityFilter`, `CsrfFilter`, and `AuthFilter`:

| Route | Controller Servlet | Auth / CSRF | Template (`/WEB-INF/views/`) | Responsibilities |
| :--- | :--- | :--- | :--- | :--- |
| `/health` | [`HealthServlet`](../src/main/java/dev/smartgiftlist/web/HealthServlet.java) | Public / GET | — (JSON) | Runs `SELECT 1` on HikariCP pool (`200` ok / `503` error) |
| `/`, `/landing` | [`LandingServlet`](../src/main/java/dev/smartgiftlist/web/LandingServlet.java) | Public / GET | [`landing.jsp`](../src/main/webapp/WEB-INF/views/landing.jsp) | Marketing hero, FAQ accordion, login/register links |
| `/register` | [`AuthServlet`](../src/main/java/dev/smartgiftlist/web/AuthServlet.java) | Public / CSRF | [`register.jsp`](../src/main/webapp/WEB-INF/views/register.jsp) | Validation, duplicate checks, PBKDF2 registration |
| `/login` | [`AuthServlet`](../src/main/java/dev/smartgiftlist/web/AuthServlet.java) | Public / CSRF | [`login.jsp`](../src/main/webapp/WEB-INF/views/login.jsp) | Credential verification, session rotation, PRG redirect |
| `/logout` | [`AuthServlet`](../src/main/java/dev/smartgiftlist/web/AuthServlet.java) | Public POST / CSRF | — (Redirect) | Session invalidation and clean sign-out flash banner |
| `/wishlists` | [`WishlistServlet`](../src/main/java/dev/smartgiftlist/web/WishlistServlet.java) | `AuthFilter` + CSRF | [`wishlists.jsp`](../src/main/webapp/WEB-INF/views/wishlists.jsp) | Wishlist CRUD, `SGL-*` NetID joins, gift claims, discussion thread |
| `/profile` | [`ProfileServlet`](../src/main/java/dev/smartgiftlist/web/ProfileServlet.java) | `AuthFilter` + CSRF | [`profile.jsp`](../src/main/webapp/WEB-INF/views/profile.jsp) | Optional-password profile edits, avatar selection, account deletion |

Shared JSTL fragments live in [`WEB-INF/views/partials/`](../src/main/webapp/WEB-INF/views/partials/header.jspf) (`header.jspf`, `nav.jspf`, `footer.jspf`, `flash.jspf`).

---

## 2. Backend Layers (`dev.smartgiftlist`)

### `dev.smartgiftlist.config`
- [`DatabaseConfig.java`](../src/main/java/dev/smartgiftlist/config/DatabaseConfig.java) resolves `DB_URL`, `DB_USER`, `DB_PASSWORD`, and `DB_SCHEMA` from JVM properties or environment variables with strict schema regex validation (`^[a-zA-Z_][a-zA-Z0-9_]*$`).
- [`DataSourceFactory.java`](../src/main/java/dev/smartgiftlist/config/DataSourceFactory.java) configures a bounded `HikariDataSource` (`smartgiftlist-pool`) applying `SET search_path TO <schema>` with bounded startup retries (`5` attempts, `1s` backoff).
- [`SchemaInitializer.java`](../src/main/java/dev/smartgiftlist/config/SchemaInitializer.java) ensures schema, 6 tables, uniqueness constraints, and 5 indexes idempotently on startup or via CLI `main`.
- [`AppContextListener.java`](../src/main/java/dev/smartgiftlist/config/AppContextListener.java) wires repositories and services into [`ServiceRegistry.java`](../src/main/java/dev/smartgiftlist/config/ServiceRegistry.java) on deploy and closes the pool cleanly on shutdown.

### `dev.smartgiftlist.model`
- Immutable JavaBean domain objects compatible with Tomcat 10.1 Jakarta EL 5.0: [`User.java`](../src/main/java/dev/smartgiftlist/model/User.java), [`Wishlist.java`](../src/main/java/dev/smartgiftlist/model/Wishlist.java), [`Gift.java`](../src/main/java/dev/smartgiftlist/model/Gift.java), [`GiftStatus.java`](../src/main/java/dev/smartgiftlist/model/GiftStatus.java), [`GiftClaimer.java`](../src/main/java/dev/smartgiftlist/model/GiftClaimer.java), [`WishlistParticipant.java`](../src/main/java/dev/smartgiftlist/model/WishlistParticipant.java), [`ChatComment.java`](../src/main/java/dev/smartgiftlist/model/ChatComment.java), [`DashboardData.java`](../src/main/java/dev/smartgiftlist/model/DashboardData.java), [`FlashMessage.java`](../src/main/java/dev/smartgiftlist/model/FlashMessage.java), and [`IconCatalog.java`](../src/main/java/dev/smartgiftlist/model/IconCatalog.java).

### `dev.smartgiftlist.repository` & `dev.smartgiftlist.service`
- Clean repository interfaces and pooled JDBC implementations: [`JdbcUserRepository.java`](../src/main/java/dev/smartgiftlist/repository/JdbcUserRepository.java), [`JdbcWishlistRepository.java`](../src/main/java/dev/smartgiftlist/repository/JdbcWishlistRepository.java), [`JdbcGiftRepository.java`](../src/main/java/dev/smartgiftlist/repository/JdbcGiftRepository.java) (using `SELECT ... FOR UPDATE` transactions for atomic gift claims), and [`JdbcCommentRepository.java`](../src/main/java/dev/smartgiftlist/repository/JdbcCommentRepository.java).
- Business rules and authorization guards live cleanly in [`AuthService.java`](../src/main/java/dev/smartgiftlist/service/AuthService.java), [`WishlistService.java`](../src/main/java/dev/smartgiftlist/service/WishlistService.java), [`GiftService.java`](../src/main/java/dev/smartgiftlist/service/GiftService.java), and [`CommentService.java`](../src/main/java/dev/smartgiftlist/service/CommentService.java).

### `dev.smartgiftlist.security`
- [`PasswordUtil.java`](../src/main/java/dev/smartgiftlist/security/PasswordUtil.java): `PBKDF2WithHmacSHA256` hashing (`120,000` iterations, 16-byte random salt, constant-time compare).
- [`HtmlUtil.java`](../src/main/java/dev/smartgiftlist/security/HtmlUtil.java): HTML escaping, `http/https` URL validation, and truncation helpers.
- [`CsrfTokenUtil.java`](../src/main/java/dev/smartgiftlist/security/CsrfTokenUtil.java): 32-byte `SecureRandom` hex tokens with constant-time validation.

---

## 3. Client Assets (`src/main/webapp`)

- **Styles:** [`css/base.css`](../src/main/webapp/css/base.css) defines shared layout tokens, chrome, navigation, flash banners, and footers; [`css/pages.css`](../src/main/webapp/css/pages.css) defines page layouts, `[data-status]` gift row coloring (`single`, `group`, `surprise`, `claimed`), horizontal wishlist shelves, chat thread, and modal dialogs.
- **Strict-Mode ES6 Modules (`defer`, no inline handlers):**
  - [`js/nav.js`](../src/main/webapp/js/nav.js): Responsive mobile navbar toggle and Escape-key closing.
  - [`js/landing.js`](../src/main/webapp/js/landing.js): Accessible FAQ accordion (`aria-expanded`).
  - [`js/auth.js`](../src/main/webapp/js/auth.js): Registration validation, optional-password profile validation, and live avatar previews.
  - [`js/wishlists.js`](../src/main/webapp/js/wishlists.js): Declarative modal open/close (`data-auto-open`), cover preview switching, single-row gift selection, group participant field lock, clipboard copy, chat auto-scroll, and scroll preservation.
