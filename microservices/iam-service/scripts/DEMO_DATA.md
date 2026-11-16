# Overview demo data

`seed-demo-overview.sql` is an opt-in development dataset, not an application migration.
It was applied to the local IAM database on 2026-11-16. Run the complete SQL file in a
single transaction if recreating it. Re-running only inserts missing records and
preserves existing records and later edits.

The dataset adds 60 synthetic users (`demo_` usernames, reserved `example.test`
emails, `created_by = demo-overview-v1`). Names and addresses are illustrative;
email and phone are not marked verified, phone numbers are omitted, and login
history and online sessions are not fabricated.

Account states: 40 active, 10 disabled, 10 locked (including two disabled),
2 expired accounts, and 6 active accounts with expired credentials. Active follows
the existing overview definition: enabled, unlocked, unexpired account; credential
expiry is a separate security state. No existing accounts are modified.

Demo users reuse the existing `seed-100-users-20261113` development password hash.
The original seed must exist. Do not use these accounts or shared development
credentials in a production database.

Five demo team roles receive specific read-only IAM or application permissions;
none receives ADMIN. Six new project/report permission definitions illustrate
application access policies; they do not add project/report endpoints to IAM.
Each demo user has one team role. Three demo OAuth clients use localhost callback
ports 3101–3103, authorization code with PKCE, and mandatory consent, without
client secrets. No applications are deployed by this seed.

Verified totals after insertion: 163 users, 139 active, 11 disabled, 13 locked,
8 roles, 29 permissions, 6 OAuth clients. Existing activity can change these totals.
