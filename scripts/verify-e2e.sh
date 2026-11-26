#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${SMARTGIFTLIST_URL:-http://localhost:8080}"
TIMEOUT_SECONDS="${E2E_TIMEOUT_SECONDS:-60}"
JAR_A="$(mktemp)"
JAR_B="$(mktemp)"
trap 'rm -f "${JAR_A}" "${JAR_B}"' EXIT

TEST_SUFFIX="$(date +%s)-$$"
USER_A="e2ea_${TEST_SUFFIX}"
USER_B="e2eb_${TEST_SUFFIX}"
EMAIL_A="${USER_A}@example.test"
EMAIL_B="${USER_B}@example.test"
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

assert_no_duplicate_ids() {
    local html="$1"
    local label="$2"
    printf '%s' "${html}" | python3 -c 'import re, sys; ids = re.findall(r"\bid=\"([^\"]+)\"", sys.stdin.read()); seen = set(); dups = [i for i in ids if i in seen or seen.add(i)]; assert not dups, f"Duplicate HTML ids in {sys.argv[1]}: {dups}"' "${label}"
}

extract_csrf() {
    python3 -c 'import re, sys; m = re.search(r"name=\"_csrf\"\s+value=\"([^\"]+)\"", sys.stdin.read()); print(m.group(1) if m else "")'
}

echo "==> Waiting for SmartGiftList health endpoint at ${BASE_URL}/health ..."
deadline=$((SECONDS + TIMEOUT_SECONDS))
until curl -fsS "${BASE_URL}/health" 2>/dev/null | grep -q '"status":"ok"'; do
    if [ "${SECONDS}" -ge "${deadline}" ]; then
        echo "ERROR: SmartGiftList did not report healthy within ${TIMEOUT_SECONDS}s" >&2
        exit 1
    fi
    sleep 2
done

echo "==> 1/12 Verify /health, landing page, static assets, and unique HTML IDs"
landing_html="$(curl -fsSL "${BASE_URL}/")"
assert_clean_html "${landing_html}"
echo "${landing_html}" | grep -q "SmartGiftList"
assert_no_duplicate_ids "${landing_html}" "landing"
for asset in /css/base.css /css/pages.css /js/landing.js /js/nav.js /js/auth.js /js/wishlists.js; do
    curl -fsS -o /dev/null "${BASE_URL}${asset}"
done

echo "==> 2/12 Verify unauthenticated protected routes redirect to /login"
for route in /wishlists /profile; do
    status="$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}${route}")"
    test "${status}" = "302"
    loc="$(curl -sI "${BASE_URL}${route}" | tr -d '\r' | awk 'tolower($1)=="location:"{print $2}')"
    echo "${loc}" | grep -q "/login$"
done

echo "==> 3/12 Verify CSRF negative control rejects POST without _csrf with 403"
csrf_status="$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/register" --data-urlencode "username=nocsrf")"
test "${csrf_status}" = "403"

echo "==> 4/12 Register User A (${USER_A}) and verify duplicate rejection"
reg_page="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" "${BASE_URL}/register")"
csrf_a="$(printf '%s' "${reg_page}" | extract_csrf)"
test -n "${csrf_a}"
reg_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/register" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "username=${USER_A}" \
    --data-urlencode "email=${EMAIL_A}" \
    --data-urlencode "birthday=2000-01-15" \
    --data-urlencode "password=${TEST_PASS}" \
    --data-urlencode "confirm_password=${TEST_PASS}")"
assert_clean_html "${reg_html}"
echo "${reg_html}" | grep -q "Your account has been created"

reg_dup_page="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" "${BASE_URL}/register")"
csrf_a="$(printf '%s' "${reg_dup_page}" | extract_csrf)"
dup_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/register" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "username=${USER_A}" \
    --data-urlencode "email=${EMAIL_A}" \
    --data-urlencode "birthday=2000-01-15" \
    --data-urlencode "password=${TEST_PASS}" \
    --data-urlencode "confirm_password=${TEST_PASS}")"
assert_clean_html "${dup_html}"
echo "${dup_html}" | grep -q "already taken"

echo "==> 5/12 Sign in User A and verify authenticated dashboard"
login_page="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" "${BASE_URL}/login")"
csrf_a="$(printf '%s' "${login_page}" | extract_csrf)"
dash_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/login" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "username=${USER_A}" \
    --data-urlencode "password=${TEST_PASS}")"
assert_clean_html "${dash_html}"
echo "${dash_html}" | grep -q "My Wishlists"

echo "==> 6/12 Create wishlist and extract generated SGL NetID"
csrf_a="$(printf '%s' "${dash_html}" | extract_csrf)"
create_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=create-wishlist" \
    --data-urlencode "list_icon=img/anlaesse/PaperList.webp" \
    --data-urlencode "wishlist_name=${LIST_TITLE}" \
    --data-urlencode "occasion=Verification" \
    --data-urlencode "date=2026-12-31" \
    --data-urlencode "description=Automated E2E verification run" \
    --data-urlencode "links=https://example.test")"
assert_clean_html "${create_html}"
echo "${create_html}" | grep -q "${LIST_TITLE}"
assert_no_duplicate_ids "${create_html}" "wishlists"
NET_ID="$(printf '%s' "${create_html}" | python3 -c 'import re, sys; m = re.search(r"id=\"owlu-net-id\"[^>]*?value=\"(SGL-[A-Z0-9]+-[A-Z0-9]+)\"", sys.stdin.read(), flags=re.S); print(m.group(1) if m else "")')"
test -n "${NET_ID}"

echo "==> 7/12 Update active wishlist details"
csrf_a="$(printf '%s' "${create_html}" | extract_csrf)"
upd_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=update-wishlist" \
    --data-urlencode "occasion=Updated E2E Occasion" \
    --data-urlencode "eventDate=2027-01-15" \
    --data-urlencode "description=Updated E2E wishlist details" \
    --data-urlencode "coverIcon=img/anlaesse/SilverList.webp" \
    --data-urlencode "links=https://example.test/updated")"
assert_clean_html "${upd_html}"
echo "${upd_html}" | grep -q "Updated E2E Occasion"
echo "${upd_html}" | grep -q "Updated E2E wishlist details"

echo "==> 8/12 Add group gift (2 participants) and extract GiftID"
csrf_a="$(printf '%s' "${upd_html}" | extract_csrf)"
gift_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=add-gift" \
    --data-urlencode "giftIcon=img/anlaesse/GoldenList.webp" \
    --data-urlencode "giftName=${GIFT_TITLE}" \
    --data-urlencode "giftType=group" \
    --data-urlencode "numberOfPeople=2" \
    --data-urlencode "description=Group coffee gift" \
    --data-urlencode "price=29.95" \
    --data-urlencode "link=https://example.test/gift")"
assert_clean_html "${gift_html}"
echo "${gift_html}" | grep -q "${GIFT_TITLE}"
echo "${gift_html}" | grep -q ">GG<"
GIFT_ID="$(printf '%s' "${gift_html}" | python3 -c 'import re, sys; m = re.search(r"name=\"selectedGiftId\"\s+value=\"([0-9]+)\"", sys.stdin.read()); print(m.group(1) if m else "")')"
test -n "${GIFT_ID}"

echo "==> 9/12 Open gift details, claim (1/2), release, and re-claim"
csrf_a="$(printf '%s' "${gift_html}" | extract_csrf)"
det_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=gift-details" \
    --data-urlencode "selectedGiftId=${GIFT_ID}")"
assert_clean_html "${det_html}"
echo "${det_html}" | grep -q 'data-auto-open="true"'
echo "${det_html}" | grep -q "${GIFT_TITLE}"

claim1_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=claim-gift" \
    --data-urlencode "giftId=${GIFT_ID}")"
assert_clean_html "${claim1_html}"
echo "${claim1_html}" | grep -q "You have claimed this gift."
echo "${claim1_html}" | grep -q "${USER_A}"

rel_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=release-gift" \
    --data-urlencode "giftId=${GIFT_ID}")"
assert_clean_html "${rel_html}"
echo "${rel_html}" | grep -q "You have released your claim on this gift."

claim2_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=claim-gift" \
    --data-urlencode "giftId=${GIFT_ID}")"
assert_clean_html "${claim2_html}"
echo "${claim2_html}" | grep -q "You have claimed this gift."

echo "==> 10/12 Post wishlist comment and withdraw last message"
chat_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=send-comment" \
    --data-urlencode "commentText=${CHAT_NOTE}")"
assert_clean_html "${chat_html}"
echo "${chat_html}" | grep -q "${CHAT_NOTE}"

wd_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=withdraw-comment")"
assert_clean_html "${wd_html}"
echo "${wd_html}" | grep -q "This message was withdrawn by the participant."
! echo "${wd_html}" | grep -q "${CHAT_NOTE}"

echo "==> 11/12 Multi-user IDOR guard + NetID join + full group claim (${USER_B})"
reg_b_page="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" "${BASE_URL}/register")"
csrf_b="$(printf '%s' "${reg_b_page}" | extract_csrf)"
curl -fsSL -b "${JAR_B}" -c "${JAR_B}" \
    "${BASE_URL}/register" \
    --data-urlencode "_csrf=${csrf_b}" \
    --data-urlencode "username=${USER_B}" \
    --data-urlencode "email=${EMAIL_B}" \
    --data-urlencode "birthday=2001-03-20" \
    --data-urlencode "password=${TEST_PASS}" \
    --data-urlencode "confirm_password=${TEST_PASS}" >/dev/null

login_b_page="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" "${BASE_URL}/login")"
csrf_b="$(printf '%s' "${login_b_page}" | extract_csrf)"
dash_b="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" \
    "${BASE_URL}/login" \
    --data-urlencode "_csrf=${csrf_b}" \
    --data-urlencode "username=${USER_B}" \
    --data-urlencode "password=${TEST_PASS}")"
csrf_b="$(printf '%s' "${dash_b}" | extract_csrf)"

idor_claim="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_b}" \
    --data-urlencode "action=claim-gift" \
    --data-urlencode "giftId=${GIFT_ID}")"
echo "${idor_claim}" | grep -q "Unable to access or claim this gift."

idor_delete="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_b}" \
    --data-urlencode "action=delete-gift" \
    --data-urlencode "selectedGiftId=${GIFT_ID}")"
echo "${idor_delete}" | grep -q "Only the wishlist owner or gift creator can remove this gift."

conn_b="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_b}" \
    --data-urlencode "action=connect-wishlist" \
    --data-urlencode "netIdConnect=${NET_ID}")"
assert_clean_html "${conn_b}"
echo "${conn_b}" | grep -q "Wishlist connected to your account."
echo "${conn_b}" | grep -q "${LIST_TITLE}"

claim_b="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" \
    "${BASE_URL}/wishlists" \
    --data-urlencode "_csrf=${csrf_b}" \
    --data-urlencode "action=claim-gift" \
    --data-urlencode "giftId=${GIFT_ID}")"
assert_clean_html "${claim_b}"
echo "${claim_b}" | grep -q "You have claimed this gift."
echo "${claim_b}" | grep -q 'data-status="claimed"'
echo "${claim_b}" | grep -q "GG, CL"

echo "==> 12/12 Profile optional-password update, logout, and User B account deletion"
prof_a="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" "${BASE_URL}/profile")"
assert_no_duplicate_ids "${prof_a}" "profile"
csrf_a="$(printf '%s' "${prof_a}" | extract_csrf)"
prof_a_upd="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/profile" \
    --data-urlencode "_csrf=${csrf_a}" \
    --data-urlencode "action=save" \
    --data-urlencode "listIcon=Novaterra" \
    --data-urlencode "profilUsername=${USER_A}_v2" \
    --data-urlencode "password=" \
    --data-urlencode "confirm_password=")"
assert_clean_html "${prof_a_upd}"
echo "${prof_a_upd}" | grep -q "Profile updated."
echo "${prof_a_upd}" | grep -q "${USER_A}_v2"
echo "${prof_a_upd}" | grep -q "Novaterra.webp"

csrf_a="$(printf '%s' "${prof_a_upd}" | extract_csrf)"
logout_html="$(curl -fsSL -b "${JAR_A}" -c "${JAR_A}" \
    "${BASE_URL}/logout" \
    --data-urlencode "_csrf=${csrf_a}")"
assert_clean_html "${logout_html}"
echo "${logout_html}" | grep -q "You have been signed out."
post_logout_status="$(curl -s -o /dev/null -w "%{http_code}" -b "${JAR_A}" "${BASE_URL}/wishlists")"
test "${post_logout_status}" = "302"

prof_b="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" "${BASE_URL}/profile")"
csrf_b="$(printf '%s' "${prof_b}" | extract_csrf)"
del_b="$(curl -fsSL -b "${JAR_B}" -c "${JAR_B}" \
    "${BASE_URL}/profile" \
    --data-urlencode "_csrf=${csrf_b}" \
    --data-urlencode "action=delete")"
assert_clean_html "${del_b}"
echo "${del_b}" | grep -q "Your account has been deleted."

echo "E2E VERIFICATION PASSED (${USER_A}_v2 -> ${NET_ID} -> ${GIFT_TITLE} [GG, CL] + ${USER_B} deleted)"
