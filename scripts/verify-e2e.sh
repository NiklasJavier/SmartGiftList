#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${SMARTGIFTLIST_URL:-http://localhost:8080}"
TIMEOUT_SECONDS="${E2E_TIMEOUT_SECONDS:-60}"
JAR="$(mktemp)"
trap 'rm -f "${JAR}"' EXIT

TEST_SUFFIX="$(date +%s)-$$"
TEST_USER="e2e_${TEST_SUFFIX}"
TEST_EMAIL="${TEST_USER}@example.test"
TEST_PASS="SmokeTest!2026"
LIST_TITLE="E2E Wishlist ${TEST_SUFFIX}"
GIFT_TITLE="E2E Mug"
CHAT_NOTE="E2E comment ${TEST_SUFFIX}"

assert_clean_html() {
    local body="$1"
    if echo "${body}" | grep -qE 'HTTP Status [45][0-9][0-9]|jakarta\.servlet\.ServletException|Exception report'; then
        echo "ERROR: Unexpected server error page detected in HTTP response." >&2
        echo "${body}" | head -n 40 >&2
        exit 1
    fi
}

echo "==> Waiting for Tomcat at ${BASE_URL}/ ..."
deadline=$((SECONDS + TIMEOUT_SECONDS))
until curl -fsSL "${BASE_URL}/" >/dev/null 2>&1; do
    if [ "${SECONDS}" -ge "${deadline}" ]; then
        echo "ERROR: SmartGiftList did not become ready within ${TIMEOUT_SECONDS}s" >&2
        exit 1
    fi
    sleep 2
done

echo "==> 1/7 Verify root redirect & English landing page"
landing_html="$(curl -fsSL "${BASE_URL}/")"
assert_clean_html "${landing_html}"
echo "${landing_html}" | grep -q "SmartGiftList"

echo "==> 2/7 Register temporary smoke-test user (${TEST_USER})"
reg_html="$(curl -fsSL -b "${JAR}" -c "${JAR}" \
    -X POST "${BASE_URL}/jsp/RegAppl.jsp" \
    --data-urlencode "username=${TEST_USER}" \
    --data-urlencode "email=${TEST_EMAIL}" \
    --data-urlencode "birthday=2000-01-15" \
    --data-urlencode "password=${TEST_PASS}" \
    --data-urlencode "confirm_password=${TEST_PASS}" \
    --data-urlencode "register=Register")"
assert_clean_html "${reg_html}"
echo "${reg_html}" | grep -q "Your account has been created"

echo "==> 3/7 Sign in and verify wishlist dashboard"
login_html="$(curl -fsSL -b "${JAR}" -c "${JAR}" \
    -X POST "${BASE_URL}/jsp/LoginAppl.jsp" \
    --data-urlencode "username=${TEST_USER}" \
    --data-urlencode "password=${TEST_PASS}" \
    --data-urlencode "login=Login")"
assert_clean_html "${login_html}"
echo "${login_html}" | grep -q "My Wishlists"

echo "==> 4/7 Create wishlist end-to-end against PostgreSQL"
create_html="$(curl -fsSL -b "${JAR}" -c "${JAR}" \
    -X POST "${BASE_URL}/jsp/OwnWishListAppl.jsp" \
    --data-urlencode "list_icon=../img/anlaesse/PaperList.webp" \
    --data-urlencode "wishlist_name=${LIST_TITLE}" \
    --data-urlencode "occasion=Verification" \
    --data-urlencode "date=2026-12-31" \
    --data-urlencode "description=Automated E2E verification run" \
    --data-urlencode "links=https://example.test" \
    --data-urlencode "create_wishlist=Create Wishlist")"
assert_clean_html "${create_html}"
echo "${create_html}" | grep -q "${LIST_TITLE}"

echo "==> 5/7 Add gift to active wishlist"
gift_html="$(curl -fsSL -b "${JAR}" -c "${JAR}" \
    -X POST "${BASE_URL}/jsp/OwnWishListAppl.jsp" \
    --data-urlencode "owlpEditListIcon=../img/anlaesse/PaperList.webp" \
    --data-urlencode "owlpEditGiftName=${GIFT_TITLE}" \
    --data-urlencode "owlpEditGroupGiftAmount=1" \
    --data-urlencode "owlpEditDescription=Smoke test gift" \
    --data-urlencode "owlpEditPrice=19.99" \
    --data-urlencode "owlpEditLink=https://example.test/gift" \
    --data-urlencode "owlpEditSubmitButton=Save Gift")"
assert_clean_html "${gift_html}"
echo "${gift_html}" | grep -q "${GIFT_TITLE}"

echo "==> 6/7 Post wishlist discussion comment"
chat_html="$(curl -fsSL -b "${JAR}" -c "${JAR}" \
    -X POST "${BASE_URL}/jsp/OwnWishListAppl.jsp" \
    --data-urlencode "ChatUserMessage=${CHAT_NOTE}" \
    --data-urlencode "ChatUserButton=sendMessage")"
assert_clean_html "${chat_html}"
echo "${chat_html}" | grep -q "${CHAT_NOTE}"

echo "==> 7/7 Sign out cleanly"
logout_html="$(curl -fsSL -b "${JAR}" -c "${JAR}" \
    "${BASE_URL}/jsp/NavAppl.jsp?logout=logout")"
assert_clean_html "${logout_html}"
echo "${logout_html}" | grep -q "You have been signed out"

echo "E2E VERIFICATION PASSED (${TEST_USER} -> ${LIST_TITLE} -> ${GIFT_TITLE})"
