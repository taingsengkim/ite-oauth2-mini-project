# Email password recovery

Delivery uses Spring's auto-configured `JavaMailSender`. Set these environment variables in the IAM run configuration, then restart IAM:

```dotenv
IAM_RECOVERY_ENABLED=true
IAM_PUBLIC_BASE_URL=https://your-iam.example
SMTP_HOST=your-smtp-host
SMTP_PORT=587
SMTP_USERNAME=your-smtp-username
SMTP_PASSWORD=your-smtp-password-or-app-password
SMTP_FROM=no-reply@your-domain.example
SMTP_AUTH=true
SMTP_STARTTLS=true
```

Use values supplied by your mail service. Store credentials in your local environment or secret manager; do not commit them. `SMTP_FROM` must be an address your service allows you to send from. The example uses STARTTLS; the mail connection, read, and write timeouts are each five seconds.

For a local SMTP capture server, use `SMTP_HOST=localhost`, `SMTP_PORT=1025`, `SMTP_AUTH=false`, `SMTP_STARTTLS=false`, `IAM_PUBLIC_BASE_URL=http://localhost:9000`, and a local sender address. Recovery is disabled by default, so existing installations can start without an SMTP service.

The public page is `/dashboard/recovery.html`. Login and Security & Password link to it. The Vite development server proxies `/recovery-api`; emailed links use the configured public IAM origin, never the request Host header. HTTPS is required except for a loopback development origin.

Liquibase migration 024 creates token and rate-limit tables and a credential-change invalidation trigger. Reset secrets are 32 random bytes, stored as SHA-256 hashes, expire after 15 minutes, and are replaced by newer requests. Email addresses must belong to enabled local-password accounts. Social-only and disabled accounts receive no reset link.

Requests return the same HTTP 202 message before asynchronous account lookup and delivery. Limits are 10 requests per IP and 3 per email per 15 minutes, with a 60-second issuance cooldown. Limit keys are hashed. The mail queue is bounded and held in memory; retry later if delivery fails or IAM restarts before dispatch. SMTP failures revoke the newly issued link and are logged without recipient addresses, message bodies, or reset secrets.

The secret travels in the email URL fragment, is removed from browser history on page load, and is kept only in memory. Refreshing that page requires reopening the email link. Opening a link does not consume it; only a successful reset does. Successful reset enforces the current password policy, clears temporary login lockouts, revokes application grants, and ends IAM browser sessions. It does not reactivate disabled accounts or remove administrative account locks. Already issued access tokens may remain valid until expiry.

Expired tokens and rate-limit rows are removed hourly. Multiple IAM instances share the database limits and tokens; browser-session termination still uses the project's current per-instance session tracker.
