#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

ACTION="${1:-}"
APP_PORT="${APP_PORT:-8080}"
PG_PORT="${PG_PORT:-5432}"
POSTGRES_DB="${POSTGRES_DB:-smartgiftlist}"
DB_USER="${DB_USER:-smartgiftlist}"
DB_PASSWORD="${DB_PASSWORD:-smartgiftlist}"
DB_SCHEMA="${DB_SCHEMA:-smartgiftlist}"
WAIT_SECONDS="${STACK_WAIT_SECONDS:-90}"

NET_NAME="smartgiftlist-net"
PG_CONTAINER="smartgiftlist-postgres"
APP_CONTAINER="smartgiftlist-app"
PG_VOLUME="smartgiftlist_pgdata"
APP_IMAGE="smartgiftlist-app"

usage() {
    cat <<EOF
Usage: $0 {up|down|logs|status|engine}
Environment:
  CONTAINER_ENGINE=docker|podman  (optional override)
  APP_PORT=8080 PG_PORT=5432      (optional port overrides)
EOF
}

detect_engine() {
    local forced="${CONTAINER_ENGINE:-}"

    if [ -z "${forced}" ] || [ "${forced}" = "docker" ]; then
        if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
            echo "docker-compose-v2"
            return 0
        fi
        if command -v docker-compose >/dev/null 2>&1; then
            echo "docker-compose-v1"
            return 0
        fi
    fi

    if [ -z "${forced}" ] || [ "${forced}" = "podman" ]; then
        if command -v podman >/dev/null 2>&1; then
            if podman compose version >/dev/null 2>&1; then
                echo "podman-compose-sub"
                return 0
            fi
            if command -v podman-compose >/dev/null 2>&1; then
                echo "podman-compose-bin"
                return 0
            fi
            echo "podman-raw"
            return 0
        fi
    fi

    echo "none"
    return 1
}

ensure_runtime_ready() {
    local engine="$1"
    case "${engine}" in
        docker-*)
            if ! docker info >/dev/null 2>&1; then
                echo "ERROR: Docker CLI found, but Docker daemon is not running." >&2
                exit 1
            fi
            ;;
        podman-*)
            if ! podman info >/dev/null 2>&1; then
                echo "ERROR: Podman CLI found, but Podman machine/socket is not running (run: podman machine start)." >&2
                exit 1
            fi
            ;;
        *)
            cat >&2 <<'EOF'
ERROR: Neither Docker nor Podman is installed on this host.
Install Docker or Podman + Compose for containerized runs (`just up`),
or run the zero-container Nix stack (`just local-up` / `just test-e2e-local`).
EOF
            exit 1
            ;;
    esac
}

wait_for_http() {
    local url="http://localhost:${APP_PORT}/jsp/LandingView.jsp"
    local deadline=$((SECONDS + WAIT_SECONDS))
    echo "==> Waiting for SmartGiftList at ${url} ..."
    until curl -fsS "${url}" >/dev/null 2>&1; do
        if [ "${SECONDS}" -ge "${deadline}" ]; then
            echo "ERROR: SmartGiftList failed to answer HTTP 200 within ${WAIT_SECONDS}s." >&2
            return 1
        fi
        sleep 2
    done
    echo "SmartGiftList is live at http://localhost:${APP_PORT}/"
}

raw_podman_up() {
    podman network exists "${NET_NAME}" >/dev/null 2>&1 || podman network create "${NET_NAME}" >/dev/null
    podman volume exists "${PG_VOLUME}" >/dev/null 2>&1 || podman volume create "${PG_VOLUME}" >/dev/null

    podman rm -f "${APP_CONTAINER}" "${PG_CONTAINER}" >/dev/null 2>&1 || true

    echo "==> Starting PostgreSQL container (${PG_CONTAINER})..."
    podman run -d \
        --name "${PG_CONTAINER}" \
        --network "${NET_NAME}" \
        -e "POSTGRES_DB=${POSTGRES_DB}" \
        -e "POSTGRES_USER=${DB_USER}" \
        -e "POSTGRES_PASSWORD=${DB_PASSWORD}" \
        -v "${PG_VOLUME}:/var/lib/postgresql/data" \
        -v "${REPO_ROOT}/docker/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro" \
        -p "${PG_PORT}:5432" \
        docker.io/library/postgres:16-alpine >/dev/null

    local deadline=$((SECONDS + 45))
    until podman exec "${PG_CONTAINER}" pg_isready -U "${DB_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; do
        if [ "${SECONDS}" -ge "${deadline}" ]; then
            echo "ERROR: PostgreSQL container did not become ready within 45s." >&2
            podman logs "${PG_CONTAINER}" >&2 || true
            exit 1
        fi
        sleep 1
    done

    echo "==> Building application image (${APP_IMAGE})..."
    podman build -t "${APP_IMAGE}" "${REPO_ROOT}"

    echo "==> Starting Tomcat application container (${APP_CONTAINER})..."
    podman run -d \
        --name "${APP_CONTAINER}" \
        --network "${NET_NAME}" \
        -e "DB_URL=jdbc:postgresql://${PG_CONTAINER}:5432/${POSTGRES_DB}" \
        -e "DB_USER=${DB_USER}" \
        -e "DB_PASSWORD=${DB_PASSWORD}" \
        -e "DB_SCHEMA=${DB_SCHEMA}" \
        -p "${APP_PORT}:8080" \
        "${APP_IMAGE}" >/dev/null
}

raw_podman_down() {
    podman rm -f "${APP_CONTAINER}" "${PG_CONTAINER}" >/dev/null 2>&1 || true
    podman volume rm -f "${PG_VOLUME}" >/dev/null 2>&1 || true
    podman network rm -f "${NET_NAME}" >/dev/null 2>&1 || true
}

ENGINE="$(detect_engine || true)"

case "${ACTION}" in
    engine)
        echo "${ENGINE}"
        ;;
    up)
        ensure_runtime_ready "${ENGINE}"
        echo "==> Booting SmartGiftList stack via ${ENGINE} ..."
        case "${ENGINE}" in
            docker-compose-v2) docker compose up -d --build --wait ;;
            docker-compose-v1) docker-compose up -d --build ;;
            podman-compose-sub) podman compose up -d --build ;;
            podman-compose-bin) podman-compose up -d --build ;;
            podman-raw) raw_podman_up ;;
        esac
        wait_for_http
        ;;
    down)
        ensure_runtime_ready "${ENGINE}"
        echo "==> Stopping and deleting SmartGiftList containers and DB volumes via ${ENGINE} ..."
        case "${ENGINE}" in
            docker-compose-v2) docker compose down -v --remove-orphans ;;
            docker-compose-v1) docker-compose down -v --remove-orphans ;;
            podman-compose-sub) podman compose down -v ;;
            podman-compose-bin) podman-compose down -v ;;
            podman-raw) raw_podman_down ;;
        esac
        echo "SmartGiftList containers and database volume deleted."
        ;;
    logs)
        ensure_runtime_ready "${ENGINE}"
        case "${ENGINE}" in
            docker-compose-v2) docker compose logs -f --tail=100 ;;
            docker-compose-v1) docker-compose logs -f --tail=100 ;;
            podman-compose-sub) podman compose logs -f --tail=100 ;;
            podman-compose-bin) podman-compose logs -f --tail=100 ;;
            podman-raw)
                podman logs --tail=50 "${PG_CONTAINER}" 2>&1 || true
                podman logs -f --tail=100 "${APP_CONTAINER}"
                ;;
        esac
        ;;
    status)
        ensure_runtime_ready "${ENGINE}"
        case "${ENGINE}" in
            docker-compose-v2) docker compose ps ;;
            docker-compose-v1) docker-compose ps ;;
            podman-compose-sub) podman compose ps ;;
            podman-compose-bin) podman-compose ps ;;
            podman-raw) podman ps -a --filter "name=smartgiftlist-" ;;
        esac
        ;;
    *)
        usage >&2
        exit 1
        ;;
esac
