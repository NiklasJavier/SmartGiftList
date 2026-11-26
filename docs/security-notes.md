# Security & Hardening Notes

During public showcase preparation, core credential, schema, and XSS defects from the original learning prototype were remediated, while structural characteristics of classic JSP scriptlets are documented transparently below.

---

## 1. Remediations Applied

| Area | Original Prototype Behavior | Current Implementation |
| :--- | :--- | :--- |
| **Password Storage** | Plaintext passwords stored and queried directly via `WHERE Password = ?` | Salted `PBKDF2WithHmacSHA256` (120,000 iterations, 16-byte `SecureRandom` salt, constant-time `MessageDigest.isEqual`) via [`PasswordUtil`](../src/main/java/dev/smartgiftlist/security/PasswordUtil.java) |
| **HTTP Form Transport** | Passwords and mutations submitted via `method="get"` query strings; passwords logged to stdout | Authentication, registration, profile, and wishlist/gift/chat forms switched to `method="post"` with password stdout logging removed |
| **Database Config & Schema Isolation** | Hardcoded campus DB host, static shared credentials, and hardcoded legacy student schema prefix | Environment-configured (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_SCHEMA`) in [`PostgreSQLAccess`](../src/main/java/dev/smartgiftlist/jdbc/PostgreSQLAccess.java) with strict schema identifier regex validation (`^[a-zA-Z_][a-zA-Z0-9_]*$`) |
| **Reflected & Stored XSS** | Unescaped user fields concatenated directly into JavaBean HTML fragments and JSP values | [`HtmlUtil.escape`](../src/main/java/dev/smartgiftlist/security/HtmlUtil.java) applied across wishlist cards, gift rows, chat comments, user banners, and JSP input values |
| **Character Encoding** | Mixed `ISO-8859-1` / `UTF-8` pages | Consistent `UTF-8` page directives and `request.setCharacterEncoding("UTF-8")` across all views and handlers |

---

## 2. Known Legacy Architecture Limitations

- **Classic JSP Scriptlet Pattern:** Routing and authorization rely on session-scoped `loginb.isLoggedIn()` guards inside `*View.jsp` and `*Appl.jsp` rather than a Servlet `Filter` or Spring Security chain.
- **No CSRF Tokens:** Traditional HTML form POST handlers do not enforce synchronizer CSRF tokens (`SameSite` cookie defaults from Tomcat apply).
- **Wishlist Ownership Checks:** `OwnWishListBean` verifies wishlist ownership before deleting or modifying wishlist metadata, whereas gift-level delete/edit actions assume trusted members of a connected `NetID` wishlist.
- **No TLS Termination Inside Tomcat:** `Dockerfile` exposes HTTP on `8080`; any remote deployment would require terminating TLS in front of Tomcat.
