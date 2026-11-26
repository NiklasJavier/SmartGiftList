# Architecture Overview

SmartGiftList is a classic server-rendered Java web application using **Jakarta Server Pages (JSP)** paired with session-scoped JavaBeans and explicit **JDBC `PreparedStatement`** persistence in PostgreSQL.

---

## 1. Request Lifecycle (`*View.jsp` -> `*Appl.jsp`)

Every browser interaction separates **presentation views** from **action controller JSPs** (`Post/Redirect/Get` pattern):

| View JSP | Handler JSP | Session Beans Used | Responsibilities |
| :--- | :--- | :--- | :--- |
| [`index.jsp`](../src/main/webapp/index.jsp) | — | — | Redirects root `/` traffic to `/jsp/LandingView.jsp` |
| [`LandingView.jsp`](../src/main/webapp/jsp/LandingView.jsp) | [`NavAppl.jsp`](../src/main/webapp/jsp/NavAppl.jsp) | `MessageBean` | Public marketing hero, FAQ accordion, login/register navigation |
| [`RegView.jsp`](../src/main/webapp/jsp/RegView.jsp) | [`RegAppl.jsp`](../src/main/webapp/jsp/RegAppl.jsp) | `AccountBean`, `MessageBean` | User registration, password confirmation, duplicate username/email checks |
| [`LoginView.jsp`](../src/main/webapp/jsp/LoginView.jsp) | [`LoginAppl.jsp`](../src/main/webapp/jsp/LoginAppl.jsp) | `LoginBean`, `MessageBean` | Credential verification via `PasswordUtil.verifyPassword` and session login flag |
| [`OwnWishListView.jsp`](../src/main/webapp/jsp/OwnWishListView.jsp) | [`OwnWishListAppl.jsp`](../src/main/webapp/jsp/OwnWishListAppl.jsp) | `LoginBean`, `OwnWishListBean`, `MessageBean` | Wishlist selection, creation, editing, `NetID` sharing, gift CRUD, claiming, and chat |
| [`ProfilView.jsp`](../src/main/webapp/jsp/ProfilView.jsp) | [`ProfilAppl.jsp`](../src/main/webapp/jsp/ProfilAppl.jsp) | `LoginBean`, `OwnWishListBean`, `ProfilManager`, `MessageBean` | Avatar selection, username/password updates, and account deletion |

---

## 2. Backend Packages (`dev.smartgiftlist`)

### `dev.smartgiftlist.jdbc`
- [`PostgreSQLAccess.java`](../src/main/java/dev/smartgiftlist/jdbc/PostgreSQLAccess.java) reads `DB_URL`, `DB_USER`, `DB_PASSWORD`, and `DB_SCHEMA` from system properties or environment variables (defaulting to local Docker/localhost development settings) and enforces strict identifier regex validation on `DB_SCHEMA`.
- [`JDBCAccess.java`](../src/main/java/dev/smartgiftlist/jdbc/JDBCAccess.java) opens the JDBC connection and applies `SET search_path TO <schema>` on checkout so all queries remain isolated inside the configured schema without hardcoded table prefixes.
- [`AppTables.java`](../src/main/java/dev/smartgiftlist/jdbc/AppTables.java) provides a standalone programmatic table initializer aligned with [`docker/init.sql`](../docker/init.sql).

### `dev.smartgiftlist.beans`
- [`OwnWishListBean.java`](../src/main/java/dev/smartgiftlist/beans/OwnWishListBean.java) acts as the session facade for an authenticated user, delegating SQL operations to [`WishlistManager.java`](../src/main/java/dev/smartgiftlist/beans/WishlistManager.java), [`GiftManager.java`](../src/main/java/dev/smartgiftlist/beans/GiftManager.java), [`ChatManager.java`](../src/main/java/dev/smartgiftlist/beans/ChatManager.java), and [`ProfilManager.java`](../src/main/java/dev/smartgiftlist/beans/ProfilManager.java).
- Domain value objects: [`Wishlist.java`](../src/main/java/dev/smartgiftlist/beans/Wishlist.java), [`Gift.java`](../src/main/java/dev/smartgiftlist/beans/Gift.java), and [`ChatMessage.java`](../src/main/java/dev/smartgiftlist/beans/ChatMessage.java).
- [`MessageBean.java`](../src/main/java/dev/smartgiftlist/beans/MessageBean.java) holds localized status and validation feedback displayed across view redirects.

### `dev.smartgiftlist.security`
- [`PasswordUtil.java`](../src/main/java/dev/smartgiftlist/security/PasswordUtil.java) implements `PBKDF2WithHmacSHA256` hashing (120,000 iterations, 16-byte random salt, constant-time byte comparison).
- [`HtmlUtil.java`](../src/main/java/dev/smartgiftlist/security/HtmlUtil.java) escapes dynamic strings before rendering into JSP/JavaBean HTML fragments.

---

## 3. Client Assets (`src/main/webapp`)

- **Styles:** [`css/SmartWishList.css`](../src/main/webapp/css/SmartWishList.css) provides responsive breakpoints, gift status color coding, modal popups, scrollable card carousels, and chat thread layouts.
- **Scripts:**
  - [`js/LandingPage.js`](../src/main/webapp/js/LandingPage.js): FAQ accordion toggle logic.
  - [`js/Dashboard.js`](../src/main/webapp/js/Dashboard.js): Responsive mobile navigation menu toggle.
  - [`js/Owl.js`](../src/main/webapp/js/Owl.js): Popup state persistence (`localStorage`), scroll preservation across POST/Redirect reloads, table row checkbox exclusivity, status row highlighting (`single`, `group`, `surprise`, `claimed`), and registration validation.
  - [`js/Profil.js`](../src/main/webapp/js/Profil.js): Avatar live preview and client-side password confirmation validation.
