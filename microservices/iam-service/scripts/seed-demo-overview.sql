-- Opt-in development data. Never included in Liquibase or application startup.
-- Synthetic identities use reserved example.test addresses and demo_ usernames.
-- Re-running inserts missing records only; it does not reset edited accounts.
-- Execute the whole file in one transaction.

INSERT INTO permissions (uuid,name,resource,action,description,is_system,is_active,is_deleted,
 created_at,created_by,updated_at,updated_by,version)
SELECT gen_random_uuid(), resource || ':' || action, resource, action, description,
 FALSE,TRUE,FALSE,CURRENT_TIMESTAMP,'demo-overview-v1',CURRENT_TIMESTAMP,'demo-overview-v1',0
FROM (VALUES
 ('report','read','View application reports'),
 ('report','export','Export application reports'),
 ('project','read','View application projects'),
 ('project','create','Create application projects'),
 ('project','update','Update application projects'),
 ('project','archive','Archive application projects')
) AS p(resource,action,description)
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (uuid,name,description,is_system,is_active,is_deleted,
 created_at,created_by,updated_at,updated_by,version)
SELECT gen_random_uuid(), name, description,FALSE,TRUE,FALSE,
 CURRENT_TIMESTAMP,'demo-overview-v1',CURRENT_TIMESTAMP,'demo-overview-v1',0
FROM (VALUES
 ('SUPPORT_AGENT','Demo: read-only account support'),
 ('SECURITY_AUDITOR','Demo: read-only security and audit review'),
 ('REPORT_VIEWER','Demo: view application reports'),
 ('PROJECT_MANAGER','Demo: manage application projects'),
 ('DEVELOPER','Demo: view and update application projects')
) AS r(name,description)
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id,permission_id,assigned_at,assigned_by,reason,
 created_at,created_by,updated_at,updated_by,version)
SELECT r.id,p.id,CURRENT_TIMESTAMP,'demo-overview-v1','Development demonstration assignment',
 CURRENT_TIMESTAMP,'demo-overview-v1',CURRENT_TIMESTAMP,'demo-overview-v1',0
FROM (VALUES
 ('SUPPORT_AGENT','overview:read'),('SUPPORT_AGENT','user:read'),
 ('SECURITY_AUDITOR','overview:read'),('SECURITY_AUDITOR','audit:read'),
 ('SECURITY_AUDITOR','role:read'),('SECURITY_AUDITOR','permission:read'),
 ('REPORT_VIEWER','report:read'),
 ('PROJECT_MANAGER','project:read'),('PROJECT_MANAGER','project:create'),
 ('PROJECT_MANAGER','project:update'),('PROJECT_MANAGER','project:archive'),
 ('DEVELOPER','project:read'),('DEVELOPER','project:update')
) AS a(role_name,permission_name)
JOIN roles r ON r.name=a.role_name AND r.created_by='demo-overview-v1'
JOIN permissions p ON p.name=a.permission_name AND NOT p.is_deleted AND p.is_active
ON CONFLICT DO NOTHING;

INSERT INTO users (uuid,username,email,email_verified,family_name,given_name,gender,
 picture_url,birthdate,phone_number,password_hash,phone_number_verified,
 account_non_expired,account_non_locked,credentials_non_expired,enabled,nickname,
 zoneinfo,locale,address_street_address,address_locality,address_region,
 address_postal_code,address_country,failed_login_attempts,password_changed_at,
 is_deleted,created_at,created_by,updated_at,updated_by,version)
SELECT gen_random_uuid(), 'demo_' || lower(family) || '_' || lower(given) || '_' || lpad(n::text,2,'0'),
 lower(given) || '.' || lower(family) || '.' || n || '@example.test',FALSE,family,given,
 CASE WHEN n%2=0 THEN 'female' ELSE 'male' END,
 'https://static.vecteezy.com/system/resources/thumbnails/009/292/244/small/default-avatar-icon-of-social-media-user-vector.jpg',
 DATE '1988-01-01' + ((n*193)%6200),NULL,
 -- Reuse the existing development seed password hash without exposing its value.
 (SELECT password_hash FROM users WHERE created_by='seed-100-users-20261113'
  AND password_hash IS NOT NULL ORDER BY id LIMIT 1),FALSE,
 n<=58, n NOT BETWEEN 49 AND 58, n NOT BETWEEN 35 AND 40, n NOT BETWEEN 41 AND 50,
 given,'Asia/Phnom_Penh',CASE WHEN n%3=0 THEN 'km' ELSE 'en' END,
 'No. ' || (10+n) || ', Street ' || (100+n),
 (ARRAY['Phnom Penh','Siem Reap','Battambang','Kampot','Kampong Cham'])[((n-1)%5)+1],
 (ARRAY['Phnom Penh','Siem Reap','Battambang','Kampot','Kampong Cham'])[((n-1)%5)+1],
 (ARRAY['120000','170000','020000','070000','030000'])[((n-1)%5)+1],'KH',
 CASE WHEN n BETWEEN 49 AND 58 THEN 5 ELSE 0 END,CURRENT_TIMESTAMP,
 FALSE,CURRENT_TIMESTAMP,'demo-overview-v1',CURRENT_TIMESTAMP,'demo-overview-v1',0
FROM generate_series(1,60) AS series(n)
CROSS JOIN LATERAL (SELECT
 (ARRAY['Sok','Chan','Chea','Keo','Seng','Lim','Heng','Vann','Kong','Phan'])[((n-1)%10)+1] AS family,
 (ARRAY['Dara','Sophea','Vichea','Sreyneang','Piseth','Bopha','Rithy','Sreypov','Sovann','Nary','Veasna','Malis'])[((n-1)%12)+1] AS given
) AS names
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id,role_id,assigned_at,assigned_by,reason,
 created_at,created_by,updated_at,updated_by,version)
SELECT u.id,r.id,CURRENT_TIMESTAMP,'demo-overview-v1','Development team assignment',
 CURRENT_TIMESTAMP,'demo-overview-v1',CURRENT_TIMESTAMP,'demo-overview-v1',0
FROM users u JOIN roles r ON r.name=(ARRAY['SUPPORT_AGENT','SECURITY_AUDITOR','REPORT_VIEWER','PROJECT_MANAGER','DEVELOPER'])
 [((substring(u.username from '([0-9]+)$')::int-1)%5)+1]
WHERE u.created_by='demo-overview-v1' AND r.created_by='demo-overview-v1'
ON CONFLICT DO NOTHING;

-- Public localhost clients: authorization-code + PKCE, consent required, no secrets.
INSERT INTO oauth2_registered_client (id,client_id,client_name,client_authentication_methods,
 authorization_grant_types,redirect_uris,post_logout_redirect_uris,scopes,client_settings,token_settings)
SELECT gen_random_uuid()::text,client_id,client_name,'none','authorization_code',
 'http://localhost:' || port || '/callback','http://localhost:' || port || '/',
 'openid,profile,email',
 '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":true,"settings.client.require-authorization-consent":true}',
 '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.x509-certificate-bound-access-tokens":false,"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration","PT1H"],"settings.token.device-code-time-to-live":["java.time.Duration","PT5M"],"settings.token.access-token-time-to-live":["java.time.Duration","PT10M"],"settings.token.authorization-code-time-to-live":["java.time.Duration","PT3M"],"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"]}'
FROM (VALUES ('demo-student-portal','Student Portal (Demo)',3101),
 ('demo-learning-platform','Learning Platform (Demo)',3102),
 ('demo-inventory-console','Inventory Console (Demo)',3103)) AS c(client_id,client_name,port)
WHERE NOT EXISTS (SELECT 1 FROM oauth2_registered_client existing WHERE existing.client_id=c.client_id);
