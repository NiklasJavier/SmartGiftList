---
name: smartgiftlist-navigator
description: >-
  Orient and navigate the SmartGiftList repository (Java 21 JSP/Servlets on Tomcat 10.1, PostgreSQL 16,
  Gradle WAR build, Nix flake.nix, justfile, and Docker Compose E2E stack). Use when exploring
  SmartGiftList modules, running builds/tests/E2E checks, or inspecting JSP form-to-handler contracts
  and privacy boundaries. Triggers: smartgiftlist, orient, just check, nix develop, verify-e2e, jsp.
  NOT: unrelated repositories or production SaaS deployment.
---

# smartgiftlist-navigator — Codebase & DX Guide

## Repository Scope

SmartGiftList is an archived reference web application built as Niklas's first software/web development project (gift wishlists, `NetID` sharing, group/surprise gift claims, and wishlist discussion chat). Treat it as a completed educational/reference codebase rather than an active product roadmap.

## Canonical Entry Points

| Concern | Location | What Lives There |
| :--- | :--- | :--- |
| **DX & Automation** | [`flake.nix`](../../../flake.nix) · [`justfile`](../../../justfile) | Reproducible `JDK 21` + `Gradle` + `just` shell and verification recipes |
| **Runtime Stack** | [`docker-compose.yml`](../../../docker-compose.yml) · [`Dockerfile`](../../../Dockerfile) · [`docker/init.sql`](../../../docker/init.sql) | Multi-stage Gradle -> Tomcat 10.1 `ROOT.war` and auto-initialized PostgreSQL 16 |
| **Database & Schema** | [`PostgreSQLAccess.java`](../../../src/main/java/dev/smartgiftlist/jdbc/PostgreSQLAccess.java) · [`JDBCAccess.java`](../../../src/main/java/dev/smartgiftlist/jdbc/JDBCAccess.java) | Env-driven `DB_URL`/`DB_USER`/`DB_PASSWORD`/`DB_SCHEMA` with schema regex validation |
| **Security Helpers** | [`PasswordUtil.java`](../../../src/main/java/dev/smartgiftlist/security/PasswordUtil.java) · [`HtmlUtil.java`](../../../src/main/java/dev/smartgiftlist/security/HtmlUtil.java) | PBKDF2-HMAC-SHA256 hashing/verification and HTML escaping |
| **Session & Managers** | [`OwnWishListBean.java`](../../../src/main/java/dev/smartgiftlist/beans/OwnWishListBean.java) · [`WishlistManager.java`](../../../src/main/java/dev/smartgiftlist/beans/WishlistManager.java) | Wishlist/gift/chat/profile coordination and SQL persistence |
| **JSP Views & Handlers** | [`OwnWishListView.jsp`](../../../src/main/webapp/jsp/OwnWishListView.jsp) · [`OwnWishListAppl.jsp`](../../../src/main/webapp/jsp/OwnWishListAppl.jsp) | Server-rendered views and `POST/Redirect/GET` form dispatchers |
| **Live E2E Smoke Check** | [`verify-e2e.sh`](../../../scripts/verify-e2e.sh) | Automated HTTP + session cookie + PostgreSQL smoke verification |

## Verified Commands (`nix develop -c just ...`)

| Recipe | Purpose |
| :--- | :--- |
| `nix develop -c just doctor` | Verify `java`, `gradle`, `curl`, `shellcheck`, `python3`, `ruby`, `postgres`, `CATALINA_HOME`, and Docker/Podman status |
| `nix develop -c just check` | Build `SmartGiftList.war`, run JUnit 5 tests, lint scripts/configs/docs, and run privacy scan |
| `nix develop -c just up` | Build containers and start PostgreSQL 16 + Tomcat 10.1 via `scripts/stack.sh` (Docker or Podman) |
| `nix develop -c just verify-e2e` | Execute 7-step end-to-end HTTP + PostgreSQL verification against `http://localhost:8080` |
| `nix develop -c just down` | Stop containers and remove networks and PostgreSQL volumes |
| `nix develop -c just local-up` | Start ephemeral Nix PostgreSQL 16 + Tomcat 10.1 without Docker/Podman (`scripts/local-stack.sh`) |
| `nix develop -c just test-e2e-local` | Run `local-up`, `verify-e2e`, and guaranteed `local-down` cleanup |
| `nix develop -c just local-down` | Stop ephemeral Nix services and delete `.local-runtime/` |

## Key Invariants & Pitfalls

1. **Privacy & Identity Isolation:** Never commit university identifiers, student IDs, private emails/names, or shared campus database hosts/credentials; `just check-privacy` enforces zero matches across the repository.
2. **JSP Parameter Contracts:** View button `name`/`value` pairs in `*View.jsp` and Java HTML card builders must stay synchronized with handler `if/else` checks in `*Appl.jsp` (`Register`, `Login`, `Create Wishlist`, `Save Gift`, `Connect Wishlist`, `Save`, `Delete Account`).
3. **Gift Status Keys:** `GiftManager.determineDataStatus` emits `single`, `group`, `surprise`, `claimed` on `<tr data-status="...">`, matched directly by [`Owl.js`](../../../src/main/webapp/js/Owl.js) row color highlighting; keep those keys synchronized.
4. **Database Schema Initialization:** Every table in `docker/init.sql` and `AppTables.java` relies on `SET search_path TO <DB_SCHEMA>` inside `JDBCAccess.setSchema()`, keeping all SQL queries schema-unqualified.

## Do NOT

- Do NOT hardcode database credentials, schemas, or local filesystem paths in Java sources or build files; see [`SECURITY.md`](../../../SECURITY.md).
- Do NOT expose the default development credentials from [`.env.example`](../../../.env.example) outside local testing.
- Do NOT introduce German prose or personal data into user-facing strings, documentation, or git commit metadata.
