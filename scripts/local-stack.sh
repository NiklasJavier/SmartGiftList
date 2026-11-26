#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_ROOT}"

ACTION="${1:-}"
RUNTIME_DIR="${REPO_ROOT}/.local-runtime"
PGDATA="${RUNTIME_DIR}/pgdata"
PGSOCK="${RUNTIME_DIR}/pgsock"
PGLOG="${RUNTIME_DIR}/postgres.log"
TOMCAT_BASE="${RUNTIME_DIR}/tomcat"
PWFILE="${RUNTIME_DIR}/pgpass.txt"

APP_PORT="${APP_PORT:-8080}"
TOMCAT_SHUTDOWN_PORT="${LOCAL_TOMCAT_SHUTDOWN_PORT:-8005}"
LOCAL_PG_HOST="${LOCAL_PG_HOST:-127.0.0.1}"
LOCAL_PG_PORT="${LOCAL_PG_PORT:-55432}"
POSTGRES_DB="${POSTGRES_DB:-smartgiftlist}"
DB_USER="${DB_USER:-smartgiftlist}"
DB_PASSWORD="${DB_PASSWORD:-smartgiftlist}"
DB_SCHEMA="${DB_SCHEMA:-smartgiftlist}"
WAR_FILE="${REPO_ROOT}/build/libs/SmartGiftList.war"

usage() {
    echo "Usage: $0 {up|down|status}" >&2
    exit 1
}

require_tools() {
    for tool in initdb pg_ctl createdb psql curl python3; do
        if ! command -v "${tool}" >/dev/null 2>&1; then
            echo "ERROR: Required command '${tool}' not found (run inside 'nix develop')." >&2
            exit 1
        fi
    done
    if [ -z "${CATALINA_HOME:-}" ] || [ ! -d "${CATALINA_HOME}" ]; then
        echo "ERROR: CATALINA_HOME is not set (run inside 'nix develop')." >&2
        exit 1
    fi
    if [ ! -f "${WAR_FILE}" ]; then
        echo "ERROR: WAR artifact missing at ${WAR_FILE}; run './gradlew war' first." >&2
        exit 1
    fi
}

cleanup_ports_or_processes() {
    if [ -f "${TOMCAT_BASE}/tomcat.pid" ]; then
        local tpid
        tpid="$(cat "${TOMCAT_BASE}/tomcat.pid" 2>/dev/null || true)"
        if [ -n "${tpid}" ] && kill -0 "${tpid}" >/dev/null 2>&1; then
            CATALINA_BASE="${TOMCAT_BASE}" CATALINA_PID="${TOMCAT_BASE}/tomcat.pid" \
                "${CATALINA_HOME}/bin/catalina.sh" stop -force >/dev/null 2>&1 || kill -9 "${tpid}" >/dev/null 2>&1 || true
        fi
    fi
    if [ -d "${PGDATA}" ]; then
        pg_ctl -D "${PGDATA}" -m immediate stop >/dev/null 2>&1 || true
    fi
}

start_stack() {
    require_tools
    cleanup_ports_or_processes
    rm -rf "${RUNTIME_DIR}"
    mkdir -p "${PGSOCK}" "${TOMCAT_BASE}/logs" "${TOMCAT_BASE}/temp" "${TOMCAT_BASE}/work" "${TOMCAT_BASE}/webapps"

    printf '%s\n' "${DB_PASSWORD}" > "${PWFILE}"
    chmod 600 "${PWFILE}"

    echo "==> Initializing ephemeral PostgreSQL 16 instance in ${PGDATA} ..."
    initdb -D "${PGDATA}" -U "${DB_USER}" --auth=scram-sha-256 --pwfile="${PWFILE}" -E UTF8 --no-locale >/dev/null

    echo "==> Starting PostgreSQL 16 on 127.0.0.1:${LOCAL_PG_PORT} ..."
    pg_ctl -D "${PGDATA}" -l "${PGLOG}" -o "-p ${LOCAL_PG_PORT} -k ${PGSOCK} -h 127.0.0.1" -w start >/dev/null

    export PGPASSWORD="${DB_PASSWORD}"
    createdb -h 127.0.0.1 -p "${LOCAL_PG_PORT}" -U "${DB_USER}" "${POSTGRES_DB}"
    psql -h 127.0.0.1 -p "${LOCAL_PG_PORT}" -U "${DB_USER}" -d "${POSTGRES_DB}" -v ON_ERROR_STOP=1 -f "${REPO_ROOT}/docker/init.sql" >/dev/null
    echo "==> PostgreSQL schema '${DB_SCHEMA}' and tables initialized."

    cp -R "${CATALINA_HOME}/conf" "${TOMCAT_BASE}/conf"
    chmod -R u+w "${TOMCAT_BASE}/conf"
    python3 - "${TOMCAT_BASE}/conf/server.xml" "${APP_PORT}" "${TOMCAT_SHUTDOWN_PORT}" <<'PY'
import pathlib, sys
path = pathlib.Path(sys.argv[1])
app_port, shutdown_port = sys.argv[2], sys.argv[3]
xml = path.read_text(encoding="utf-8")
xml = xml.replace('port="8005"', f'port="{shutdown_port}"')
xml = xml.replace('port="8080"', f'port="{app_port}"')
path.write_text(xml, encoding="utf-8")
PY

    cp "${WAR_FILE}" "${TOMCAT_BASE}/webapps/ROOT.war"

    export CATALINA_BASE="${TOMCAT_BASE}"
    export CATALINA_PID="${TOMCAT_BASE}/tomcat.pid"
    export DB_URL="jdbc:postgresql://${LOCAL_PG_HOST}:${LOCAL_PG_PORT}/${POSTGRES_DB}"
    export DB_USER
    export DB_PASSWORD
    export DB_SCHEMA

    echo "==> Starting Apache Tomcat 10 on http://localhost:${APP_PORT}/ ..."
    "${CATALINA_HOME}/bin/catalina.sh" start >/dev/null

    local url="http://localhost:${APP_PORT}/jsp/LandingView.jsp"
    local deadline=$((SECONDS + 45))
    until curl -fsS "${url}" >/dev/null 2>&1; do
        if [ "${SECONDS}" -ge "${deadline}" ]; then
            echo "ERROR: Local Tomcat did not respond HTTP 200 within 45s." >&2
            tail -n 60 "${TOMCAT_BASE}/logs/catalina.out" >&2 || true
            tail -n 40 "${PGLOG}" >&2 || true
            exit 1
        fi
        sleep 1
    done
    echo "Local Nix stack is live at http://localhost:${APP_PORT}/ (PostgreSQL port ${LOCAL_PG_PORT})"
}

stop_stack() {
    echo "==> Stopping local Tomcat + PostgreSQL and deleting ${RUNTIME_DIR} ..."
    cleanup_ports_or_processes
    rm -rf "${RUNTIME_DIR}"
    echo "Local Nix runtime and database deleted cleanly."
}

case "${ACTION}" in
    up)
        start_stack
        ;;
    down)
        stop_stack
        ;;
    status)
        if [ -d "${PGDATA}" ]; then
            pg_ctl -D "${PGDATA}" status || true
        else
            echo "Local runtime is stopped (no ${RUNTIME_DIR})."
        fi
        ;;
    *)
        usage
        ;;
esac
