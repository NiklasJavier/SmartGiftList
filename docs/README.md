# SmartGiftList Documentation

| Guide | Contents |
| :--- | :--- |
| [Getting Started](./getting-started.md) | Prerequisites for macOS, Linux, and Windows (WSL2), Nix + `just`, manual Gradle/Docker fallback, and 12-step E2E verification |
| [Architecture](./architecture.md) | Layered Servlet 6 MVC flow (`Web -> Service -> Repository`), HikariCP pooling, JSTL views, and static assets |
| [Database Schema](./database-schema.md) | PostgreSQL ER diagram, `docker/init.sql`, unique constraints, indexes, and `DB_SCHEMA` configuration |
| [Security Notes](./security-notes.md) | PBKDF2 hashing, CSRF filter, AuthFilter & IDOR guards, URL sanitization, and HTTP security headers |
