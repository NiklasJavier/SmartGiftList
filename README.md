# SmartGiftList

> **Collaborative gift wishlist web application built with Java 21, Jakarta EE JSP/Servlets, JDBC, and PostgreSQL — packaged with a reproducible Nix Flake, Gradle WAR build, and Docker Compose end-to-end runtime stack.**

[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](./LICENSE)
![Status: Archived First Project (Reference)](https://img.shields.io/badge/Status-Archived%20First%20Project%20%28Reference%29-6c757d)
[![CI](https://github.com/NiklasJavier/SmartGiftList/actions/workflows/ci.yml/badge.svg)](https://github.com/NiklasJavier/SmartGiftList/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21%20%28WAR%20%2F%20Tomcat%2010.1%29-ED8B00?logo=openjdk&logoColor=white)](./build.gradle)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](./docker/init.sql)
[![Nix Flake](https://img.shields.io/badge/Nix-Flake-5277C3?logo=nixos&logoColor=white)](./flake.nix)

---

> [!NOTE]
> **Project Context:** SmartGiftList was my very first software and web development project, built to learn server-side Java/JSP development, relational SQL data modeling, and session-driven MVC workflows from scratch. It has since been sanitized and modernized for reproducible local runs and unit testing, and remains **archived as a completed reference with no ongoing feature development**. Default container credentials in [`.env.example`](./.env.example) are strictly for local evaluation — see [`SECURITY.md`](./SECURITY.md) before reusing any component.

---

## Quickstart (End-to-End Go-Live & Verification)

**1. Install prerequisites:** [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/) (`scripts/stack.sh` supports Docker Compose v2/v1, `podman compose`, `podman-compose`, and rootless `podman`), plus [Nix](https://nixos.org/download/) with Flakes enabled (`flake.nix` provides **JDK 21**, **Gradle**, `just`, `curl`, `jq`, `shellcheck`, `python3`, `ruby`, `postgresql_16`, and `tomcat10` for zero-container E2E runs):

- **macOS / Linux:** `curl --proto '=https' --tlsv1.2 -sSf -L https://install.determinate.systems/nix | sh -s -- install` (official alternative: [nixos.org/download](https://nixos.org/download/))
- **Windows:** Enable [WSL2](https://learn.microsoft.com/en-us/windows/wsl/install) and turn on Docker Desktop / Podman WSL2 integration, then run the same Nix installer inside your WSL2 Linux shell.

**2. Verify, boot, and smoke-test end-to-end:**

```bash
# Clone, enter reproducible dev shell, verify WAR build, unit tests, configs, docs & privacy
git clone https://github.com/NiklasJavier/SmartGiftList.git && cd SmartGiftList
nix develop
just doctor && just check

# Container path (Docker or Podman): start PostgreSQL 16 + auto-connected Tomcat 10.1
just up

# Live 7-step E2E check: Landing page -> Register -> Sign in -> Wishlist -> Gift item -> Sign out
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

SmartGiftList follows a classic **JSP View / Handler (`*View.jsp` -> `*Appl.jsp`) + JavaBean Session Facade (`OwnWishListBean`) + JDBC Manager** architecture backed by PostgreSQL:

```mermaid
flowchart LR
  Browser["Web Browser<br/>JSP + CSS + Vanilla JS"] -->|HTTP GET/POST| Tomcat["Apache Tomcat 10.1<br/>ROOT.war (JDK 21)"]
  subgraph WebLayer["JSP View & Action Layer"]
    Views["LandingView · LoginView<br/>RegView · OwnWishListView · ProfilView"]
    Handlers["NavAppl · LoginAppl · RegAppl<br/>OwnWishListAppl · ProfilAppl"]
    Views -->|Form Submit| Handlers
    Handlers -->|Redirect| Views
  end
  Tomcat --> WebLayer
  WebLayer -->|Session Beans| Facade["AccountBean · LoginBean · MessageBean<br/>OwnWishListBean"]
  Facade --> Managers["WishlistManager · GiftManager<br/>ChatManager · ProfilManager"]
  Facade --> Security["PasswordUtil (PBKDF2)<br/>HtmlUtil (XSS Escape)"]
  Managers -->|JDBC + PreparedStatements| PG[("PostgreSQL 16<br/>smartgiftlist schema")]
```

### Features Implemented

- **User Onboarding & Profile Management:** Registration (`RegView.jsp`), PBKDF2-HMAC-SHA256 authentication (`LoginView.jsp`), profile editing with avatar selection (`Lumibella`, `Novaterra`, `Quixart`), and cascade account deletion (`ProfilView.jsp`).
- **Personal & Shared Wishlists (`NetID` Invites):** Create themed wishlists (`PaperList`, `BrozeList`, `SilverList`, `GoldenList`), edit metadata/event dates, and invite friends or family via a wishlist `NetID` code.
- **Gift Types & Claim Coordination:**
  - `IG` (**Individual Gift**) — single-contributor item
  - `GG` (**Group Gift**) — multi-contributor item tracking participant capacity
  - `SG` (**Surprise Gift**) — marks surprise items for wishlist planning
  - `CL` (**Claimed**) — automatically flips row status once participant slots are filled
- **Per-Wishlist Discussion Feed:** Participant chat messages with timestamp ordering and one-click message withdrawal (`ChatManager`).

---

## Documentation & Navigation

| Document | Focus |
| :--- | :--- |
| [docs/README.md](./docs/README.md) | Documentation index |
| [docs/getting-started.md](./docs/getting-started.md) | macOS/Linux/Windows setup, manual build, env vars, E2E walkthrough |
| [docs/architecture.md](./docs/architecture.md) | JSP MVC flow, JavaBeans, JDBC schema resolution, frontend JS/CSS |
| [docs/database-schema.md](./docs/database-schema.md) | PostgreSQL ER diagram and all 6 relational tables (`docker/init.sql`) |
| [docs/security-notes.md](./docs/security-notes.md) | Password hashing, SQL injection guards, HTML escaping, legacy caveats |
| [.agents/README.md](./.agents/README.md) | Agent & contributor codebase index and `repo-skills` guide |

Community & governance: [`CONTRIBUTING.md`](./CONTRIBUTING.md) · [`SECURITY.md`](./SECURITY.md) · [`CODE_OF_CONDUCT.md`](./CODE_OF_CONDUCT.md) · [`CHANGELOG.md`](./CHANGELOG.md) · [`LICENSE`](./LICENSE) (Apache-2.0)
