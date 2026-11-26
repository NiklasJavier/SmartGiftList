# SmartGiftList

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](./LICENSE)
![Status: Archived First Project (Reference)](https://img.shields.io/badge/Status-Archived%20First%20Project%20%28Reference%29-6c757d)
[![CI](https://github.com/NiklasJavier/SmartGiftList/actions/workflows/ci.yml/badge.svg)](https://github.com/NiklasJavier/SmartGiftList/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21%20%28WAR%20%2F%20Tomcat%2010.1%29-ED8B00?logo=openjdk&logoColor=white)](./build.gradle)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](./docker/init.sql)
[![Nix Flake](https://img.shields.io/badge/Nix-Flake-5277C3?logo=nixos&logoColor=white)](./flake.nix)

---

> [!NOTE]
> **Project Context:** SmartGiftList was my very first software and web development project, originally written with JSP scriptlets to learn server-side Java and SQL modeling. It has since been sanitized and refactored into a clean layered **Jakarta Servlet 6 MVC + JSTL 3.0 + HikariCP** architecture for reproducible local runs and testing, while remaining **archived as a completed reference with no ongoing feature roadmap**. Default credentials in [`.env.example`](./.env.example) are strictly for local evaluation — see [`SECURITY.md`](./SECURITY.md) before reusing any component.

## Visual Overview

<table>
  <tr>
    <td align="center">
      <a href="./docs/screenshots/landing.png">
        <img src="./docs/screenshots/landing.png" width="380" alt="SmartGiftList Landing Page">
      </a>
      <br/><sub><b>Landing Page &amp; FAQ Accordion (<code>/</code>)</b></sub>
    </td>
    <td align="center">
      <a href="./docs/screenshots/gift-details-modal.png">
        <img src="./docs/screenshots/gift-details-modal.png" width="380" alt="Gift Details and Claiming Modal">
      </a>
      <br/><sub><b>Gift Details &amp; Claiming Modal (<code>?openDetails=1</code>)</b></sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="./docs/screenshots/login.png">
        <img src="./docs/screenshots/login.png" width="380" alt="Sign In View">
      </a>
      <br/><sub><b>Sign In (<code>/login</code>)</b></sub>
    </td>
    <td align="center">
      <a href="./docs/screenshots/register.png">
        <img src="./docs/screenshots/register.png" width="380" alt="Registration View">
      </a>
      <br/><sub><b>Account Registration (<code>/register</code>)</b></sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <a href="./docs/screenshots/profile.png">
        <img src="./docs/screenshots/profile.png" width="380" alt="Profile Management View">
      </a>
      <br/><sub><b>Profile Management (<code>/profile</code>)</b></sub>
    </td>
    <td align="left" valign="top">
      <b>All 6 captured views</b> (<code>1440x900</code>):
      <ul>
        <li><a href="./docs/screenshots/landing.png">Landing Page</a> (<code>/</code>)</li>
        <li><a href="./docs/screenshots/register.png">Registration</a> (<code>/register</code>)</li>
        <li><a href="./docs/screenshots/login.png">Sign In</a> (<code>/login</code>)</li>
        <li><a href="./docs/screenshots/wishlists.png">Wishlist Dashboard</a> (<code>/wishlists</code>)</li>
        <li><a href="./docs/screenshots/gift-details-modal.png">Gift Details Modal</a> (<code>?openDetails=1</code>)</li>
        <li><a href="./docs/screenshots/profile.png">Profile Management</a> (<code>/profile</code>)</li>
      </ul>
      <sub>Full-resolution PNG captures stored in <a href="./docs/screenshots/"><code>docs/screenshots/</code></a>.</sub>
    </td>
  </tr>
</table>

---

## Quickstart (End-to-End Go-Live & Verification)

**1. Install prerequisites:** [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/) (`scripts/stack.sh` supports Docker Compose v2/v1, `podman compose`, `podman-compose`, and rootless `podman`), plus [Nix](https://nixos.org/download/) with Flakes enabled (`flake.nix` provides **JDK 21**, **Gradle**, `just`, `curl`, `jq`, `shellcheck`, `python3`, `ruby`, `postgresql_16`, and `tomcat10` for zero-container E2E runs):

- **macOS / Linux:** `curl --proto '=https' --tlsv1.2 -sSf -L https://install.determinate.systems/nix | sh -s -- install` (official alternative: [nixos.org/download](https://nixos.org/download/))
- **Windows:** Enable [WSL2](https://learn.microsoft.com/en-us/windows/wsl/install) and turn on Docker Desktop / Podman WSL2 integration, then run the same Nix installer inside your WSL2 Linux shell.

**2. Verify, boot, and smoke-test end-to-end:**

```bash
# Clone, enter reproducible dev shell, verify WAR build, JUnit 5 tests, configs, docs & privacy
git clone https://github.com/NiklasJavier/SmartGiftList.git && cd SmartGiftList
nix develop
just doctor && just check

# Container path (Docker or Podman): start PostgreSQL 16 + auto-connected Tomcat 10.1
just up

# Live 12-step E2E check: /health -> auth/CSRF guards -> register -> login -> wishlist -> group gift -> IDOR -> profile -> logout
just verify-e2e
# Browse interactively at: http://localhost:8080/

# Stop containers and delete network state & ephemeral DB volumes
just down

# Zero-container Nix path (when neither Docker nor Podman is installed):
just test-e2e-local   # runs local-up (Nix PostgreSQL 16 + Tomcat 10.1) -> verify-e2e -> local-down
```

Manual container/Gradle usage without Nix, environment variable overrides, and `just local-up` / `just local-down` details live in [docs/getting-started.md](./docs/getting-started.md).

---

## Architecture & Core Capabilities

SmartGiftList uses a layered **Jakarta Servlet 6 MVC architecture** backed by pooled **HikariCP JDBC** connections, **JSTL 3.0** views (`scripting-invalid=true`), and **PostgreSQL 16**:

```mermaid
flowchart LR
  Browser["Web Browser<br/>JSTL 3.0 + CSS + Vanilla JS"] -->|HTTP GET/POST| Filters["Filters: EncodingAndSecurity · Csrf · Auth"]
  Filters --> Servlets["Servlets: Health · Landing · Auth · Wishlist · Profile"]
  Servlets -->|Render via PRG| Views["/WEB-INF/views/*.jsp<br/>Zero Scriptlets"]
  Servlets --> Services["Services: Auth · Wishlist · Gift · Comment<br/>Validation & IDOR Guards"]
  Services --> Security["PasswordUtil (PBKDF2)<br/>HtmlUtil · CsrfTokenUtil"]
  Services --> Repos["JDBC Repositories<br/>HikariCP Pool + Row Locks"]
  Repos --> PG[("PostgreSQL 16<br/>smartgiftlist schema")]
```

### Features Implemented

- **User Onboarding & Profile Management:** Registration (`/register`), PBKDF2-HMAC-SHA256 authentication (`/login`) with session ID rotation, optional-password profile updates with avatar selection (`Lumibella`, `Novaterra`, `Quixart`) on `/profile`, CSRF protection (`_csrf`), and cascade account deletion.
- **Personal & Shared Wishlists (`SGL-XXXX-XXXX` Invites):** Create themed wishlists (`PaperList`, `BronzeList`, `SilverList`, `GoldenList`), edit metadata/event dates (owner-enforced), and invite friends or family via a high-entropy random `NetID`.
- **Gift Types & Atomic Claim Coordination:**
  - `IG` (**Individual Gift**) — single-contributor item
  - `GG` (**Group Gift**) — multi-contributor item tracking participant capacity atomically via `FOR UPDATE`
  - `SG` (**Surprise Gift**) — marks surprise items for wishlist planning
  - `CL` (**Claimed**) — automatically flips row status once participant slots are filled
- **Per-Wishlist Discussion Feed:** Member-scoped chat messages with timestamp ordering and one-click withdrawal of the user's latest comment.

---

## Documentation & Navigation

| Document | Focus |
| :--- | :--- |
| [docs/README.md](./docs/README.md) | Documentation index |
| [docs/getting-started.md](./docs/getting-started.md) | macOS/Linux/Windows setup, manual build, env vars, 12-step E2E walkthrough |
| [docs/architecture.md](./docs/architecture.md) | Layered Servlet 6 MVC, HikariCP/JDBC repositories, services, JSTL views, JS/CSS |
| [docs/database-schema.md](./docs/database-schema.md) | PostgreSQL ER diagram, 6 relational tables, unique constraints, and indexes (`docker/init.sql`) |
| [docs/security-notes.md](./docs/security-notes.md) | PBKDF2 hashing, CSRF tokens, AuthFilter/IDOR guards, URL sanitization, HTML escaping |
| [.agents/README.md](./.agents/README.md) | Agent & contributor codebase index and `repo-skills` guide |

---

## License & Disclaimer

- **License:** Released under the [MIT License](./LICENSE).
- **No Warranty / Limitation of Liability:** This software and accompanying documentation are provided **"as is"**, without warranty of any kind, express or implied (including merchantability, fitness for a particular purpose, and noninfringement). Any use, modification, or deployment is strictly at your own risk; the author and copyright holders assume no liability for any claims, damages, data loss, or security incidents arising out of or in connection with this software.

Community & governance: [`CONTRIBUTING.md`](./CONTRIBUTING.md) · [`SECURITY.md`](./SECURITY.md) · [`CODE_OF_CONDUCT.md`](./CODE_OF_CONDUCT.md) · [`CHANGELOG.md`](./CHANGELOG.md) · [`LICENSE`](./LICENSE)
