# Email verification for password registration

Password registration now creates a pending request instead of an IAM user.
Only a valid email code creates the enabled account, marks its email verified,
and assigns the default USER role in one transaction. Existing users, admin-created
accounts, and existing social login/provisioning behavior are unchanged. This is
mailbox verification for password signup, not proof of a person's identity or a
complete bot/disposable-email prevention system.

## Configuration

Restart IAM to apply Liquibase migration `026-pending-registration.sql` and load
the new code. It does not modify existing accounts.

The feature uses Spring `JavaMailSender`, the existing `SMTP_*` configuration,
and `SMTP_FROM`. Set `IAM_REGISTRATION_VERIFICATION_ENABLED=true` to enable it
explicitly. If that variable is absent, it follows `IAM_RECOVERY_ENABLED`, so
working password-recovery email settings can be reused. Disabled email
registration fails closed; it does not create an unverified active account.

## Browser flow

1. Open `/register` and submit a new username, accessible email, and a password
   that satisfies the configured policy.
2. The browser redirects to `/register/verify`. The opaque challenge stays in
   the anonymous HTTP session; only the masked address is displayed.
3. Enter the six-digit email code. Success redirects to `/login?registered`.
4. Sign in with the new credentials. Admin Users should now show the user with
   a verified email and the USER role. No user exists before verification.

Resend is available after 60 seconds. Only the latest code works. Codes expire
10 minutes after the initial request; resend does not extend that expiry or
reset the five-attempt budget. Use “start again” to cancel a pending request
and correct the email/details. Invalid/expired/exhausted challenges cannot
activate an account. Browser POSTs retain CSRF protection.

## Public REST contract change

`POST /api/auth/register` now returns **202 Accepted**, not an immediately
created user. Its existing registration JSON fields remain the same. The body
contains a generic message and `challenge` with `token`, `maskedEmail`, and
`expiresAt`. Keep the opaque token private; do not put it in URLs or logs.

`POST /api/auth/register/verify` accepts `{ "token": "<challenge>", "code": "<six digits>" }`.
Only success returns a verified outcome and new user UUID. Invalid codes return
400; throttling returns 429; disabled email registration returns 503.

`POST /api/auth/register/resend` accepts `{ "token": "<challenge>" }` and returns
202 with a generic response. Requests that are expired, unknown, exhausted, or
inside the cooldown do not send mail. The public API remains stateless; these
endpoints require the secret challenge for verification/resend. They cannot call
the user-provisioning operation directly.

## Abuse controls and storage

- Six-digit codes generated with `SecureRandom`; one-time redemption with a DB row lock.
- Five incorrect attempts per challenge, persisted even when verification fails.
- Registration requests: 10 per IP per 15 minutes.
- Registration/resend email budget: 20 per IP per hour, 5 per email per hour,
  plus a 60-second per-email cooldown. IP checks use the servlet's remote address,
  as password recovery does; do not trust arbitrary forwarded headers.
- Duplicate existing accounts and pending requests get a generic accepted
  response. They do not send registration mail or overwrite another request.
- The database stores a password hash, SHA-256 challenge identifier, and an
  HMAC-SHA-256 code hash keyed by the random challenge. It does not store the
  plaintext password, code, or raw challenge.
- SMTP runs after the pending transaction commits. Delivery/queue failures
  invalidate only the matching code; a later resend can recover safely.
- Expired pending data and rate-limit entries are cleaned periodically and
  during registration. Nothing automatically backfills or verifies old users.
- Forms/API responses use `no-store`; language-switch links exclude submitted
  form fields, so passwords and codes cannot be copied into URLs.

For broader abuse prevention, a later enhancement can add risk-based CAPTCHA
or a maintained disposable-domain policy. Social providers that do not supply
verified email still follow the existing social-onboarding rules; this change
does not add an email-code challenge to that separate flow.

## Validation performed

Integration checks used a temporary PostgreSQL schema and real transaction,
repository, password-hashing, user-provisioning and role-assignment code. Email
messages were captured locally; no external test emails were sent. Checks covered
pending-only creation, verified provisioning, wrong attempts, expiry, resend
rotation/cooldown/budgets, replay and concurrent redemption, uniqueness conflicts,
mail/queue failure recovery, cancellation, disabled mode and throttling. The test
schema and temporary test source files were removed.

Template/controller checks covered English and Khmer, browser session binding,
preservation of saved OAuth authorization requests, API statuses, no-store
responses and prevention of POST secrets in language-switch URLs.
