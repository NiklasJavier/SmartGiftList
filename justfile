set shell := ["bash", "-euo", "pipefail", "-c"]

# List available tasks
default:
    @just --list

# Verify required toolchain, container runtimes, and repository structure
doctor:
    @echo "==> Toolchain checks"
    @java -version 2>&1 | head -n 1
    @command -v gradle >/dev/null && echo "OK: gradle" || { echo "FAIL: gradle missing"; exit 1; }
    @command -v curl >/dev/null && echo "OK: curl" || { echo "FAIL: curl missing"; exit 1; }
    @command -v shellcheck >/dev/null && echo "OK: shellcheck" || { echo "FAIL: shellcheck missing"; exit 1; }
    @command -v python3 >/dev/null && echo "OK: python3" || { echo "FAIL: python3 missing"; exit 1; }
    @command -v ruby >/dev/null && echo "OK: ruby" || { echo "FAIL: ruby missing"; exit 1; }
    @if command -v psql >/dev/null 2>&1; then echo "OK: postgresql-16"; else echo "INFO: postgresql_16 not on PATH (available inside nix develop)"; fi
    @if [ -n "${CATALINA_HOME:-}" ] && [ -d "${CATALINA_HOME:-}" ]; then echo "OK: tomcat10 (${CATALINA_HOME})"; else echo "INFO: tomcat10 CATALINA_HOME unset (available inside nix develop)"; fi
    @if command -v docker >/dev/null 2>&1; then echo "OK: docker"; elif command -v podman >/dev/null 2>&1; then echo "OK: podman"; else echo "INFO: neither docker nor podman found (container stack uses Docker/Podman; native Nix uses just local-up / just test-e2e-local)"; fi
    @echo "==> Repository checks"
    @test -f gradlew && echo "OK: gradlew present"
    @test -f docker-compose.yml && echo "OK: docker-compose.yml present"
    @echo "doctor: OK"

# Build the deployable SmartGiftList.war artifact
build:
    ./gradlew --no-daemon war

# Run JUnit 5 unit tests
test:
    ./gradlew --no-daemon test

# Lint shell scripts
lint-scripts:
    @for f in scripts/*.sh; do bash -n "$f"; done
    @shellcheck -S error scripts/*.sh
    @echo "lint-scripts: OK"

# Validate YAML, XML, and SQL syntax
check-configs:
    @ruby -ryaml -e 'Dir.glob("{docker-compose.yml,.github/**/*.yml}").each { |f| YAML.safe_load(File.read(f), aliases: true) }; puts "check-configs YAML: OK"'
    @python3 -c 'import xml.etree.ElementTree as ET, glob; files = sorted(glob.glob("src/main/webapp/WEB-INF/*.xml")); [ET.parse(f) for f in files]; print(f"check-configs XML: OK ({len(files)} files)")'
    @python3 -c 'import pathlib; sql = pathlib.Path("docker/init.sql").read_text(encoding="utf-8"); assert "CREATE SCHEMA IF NOT EXISTS smartgiftlist" in sql and sql.count("CREATE TABLE IF NOT EXISTS") == 6; print("check-configs SQL: OK")'

# Validate relative Markdown links
check-docs:
    @python3 -c 'import re, pathlib; root = pathlib.Path("."); mds = sorted([p for p in root.rglob("*.md") if ".git" not in p.parts]); broken = []; [broken.append((str(md), m.group(1))) for md in mds for m in re.finditer(r"!?\[[^\]]*\]\(([^)]+)\)", re.sub(r"```.*?```", "", md.read_text(encoding="utf-8"), flags=re.S)) if not m.group(1).startswith(("http://", "https://", "mailto:", "#")) and m.group(1).split("#")[0] and not (md.parent / m.group(1).split("#")[0]).resolve().exists()]; print(f"check-docs: {len(mds)} files, broken={len(broken)}"); assert not broken, broken'

# Verify no local paths, legacy university prefixes, or non-ASCII German characters remain
check-privacy:
    @python3 -c 'import re, pathlib; pat = re.compile("([\xe4\xf6\xfc\xc4\xd6\xdc\xdf]|[/]Users/|[/]home/[a-zA-Z0-9._-]+|jdbc:postgresql://(?!127[.]0[.]0[.]1)[0-9]+[.]|de[.][a-z]{3}[.]sgl|[a-z]{3}[0-9]{3}_[0-9]{5,})"); hits = [f"{p}:{i}" for p in sorted(pathlib.Path(".").rglob("*")) if p.is_file() and ".git" not in p.parts and "build" not in p.parts and ".gradle" not in p.parts and ".local-runtime" not in p.parts and p.suffix.lower() not in {".png",".jpg",".jpeg",".webp",".svg",".jar"} and p.name != "flake.lock" for i, l in enumerate(p.read_text(encoding="utf-8", errors="ignore").splitlines(), 1) if pat.search(l)]; print(f"check-privacy: hits={len(hits)}"); assert not hits, hits'

# Run full static verification and unit test suite
check: build test lint-scripts check-configs check-docs check-privacy

# Start PostgreSQL 16 + Tomcat 10.1 via Docker or Podman (auto-detected)
up:
    chmod +x scripts/stack.sh
    ./scripts/stack.sh up

# Run automated live HTTP + PostgreSQL end-to-end verification against running stack
verify-e2e:
    chmod +x scripts/verify-e2e.sh
    SMARTGIFTLIST_URL="http://localhost:${APP_PORT:-8080}" ./scripts/verify-e2e.sh

# Show container logs
logs:
    chmod +x scripts/stack.sh
    ./scripts/stack.sh logs

# Show container status
status:
    chmod +x scripts/stack.sh
    ./scripts/stack.sh status

# Stop and delete containers, networks, and database volumes cleanly
down:
    chmod +x scripts/stack.sh
    ./scripts/stack.sh down

# Start native Nix PostgreSQL 16 + Tomcat 10.1 without Docker/Podman (run inside nix develop)
local-up: build
    chmod +x scripts/local-stack.sh
    ./scripts/local-stack.sh up

# Stop native Nix PostgreSQL 16 + Tomcat 10.1 and delete .local-runtime database files
local-down:
    chmod +x scripts/local-stack.sh
    ./scripts/local-stack.sh down

# Run full automated live HTTP + PostgreSQL E2E suite on native Nix stack and clean up
test-e2e-local: build
    chmod +x scripts/local-stack.sh scripts/verify-e2e.sh
    trap './scripts/local-stack.sh down' EXIT; ./scripts/local-stack.sh up && SMARTGIFTLIST_URL="http://localhost:${APP_PORT:-8080}" ./scripts/verify-e2e.sh
