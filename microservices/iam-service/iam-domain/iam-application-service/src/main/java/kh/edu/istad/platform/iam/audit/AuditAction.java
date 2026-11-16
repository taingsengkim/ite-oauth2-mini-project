package kh.edu.istad.platform.iam.audit;

public enum AuditAction {
    ACCOUNT_PASSWORD_RECOVERED("account.password.recover", "Password recovered by email", "user"),
    ACCOUNT_DEACTIVATED("account.deactivate", "Account deactivated by its owner", "user"),
    ACCOUNT_IDENTITY_LINKED("account.identity.link", "Social account linked", "user"),
    ACCOUNT_IDENTITY_UNLINKED("account.identity.unlink", "Social account unlinked", "user"),
    ACCOUNT_PASSWORD_CHANGED("account.password.change", "Account password changed", "user"),
    ACCOUNT_PROFILE_UPDATED("account.profile.update", "Account profile updated", "user"),
    CONFIGURATION_UPDATED("configuration.update", "System configuration updated", "configuration"),
    USER_CREATED("user.create", "User created", "user"),
    USER_UPDATED("user.update", "User profile updated", "user"),
    USER_ENABLED("user.activate", "User activated", "user"),
    USER_DISABLED("user.deactivate", "User deactivated", "user"),
    USER_LOCKED("user.lock", "User locked", "user"),
    USER_UNLOCKED("user.unlock", "User unlocked", "user"),
    USER_DELETED("user.delete", "User deleted", "user"),
    USER_PASSWORD_RESET("user.password.reset", "User password reset", "user"),
    USER_ROLES_ASSIGNED("user.role.assign", "User roles assigned", "user"),
    USER_ROLE_UPDATED("user.role.update", "User role expiry updated", "user"),
    USER_ROLE_REVOKED("user.role.revoke", "User role revoked", "user"),
    USER_PERMISSIONS_ASSIGNED("user.permission.assign", "Direct user permissions assigned", "user"),
    USER_PERMISSION_REVOKED("user.permission.revoke", "Direct user permission revoked", "user"),
    ROLE_CREATED("role.create", "Role created", "role"),
    ROLE_UPDATED("role.update", "Role updated", "role"),
    ROLE_ACTIVATED("role.activate", "Role activated", "role"),
    ROLE_DEACTIVATED("role.deactivate", "Role deactivated", "role"),
    ROLE_DELETED("role.delete", "Role deleted", "role"),
    ROLE_PERMISSIONS_ASSIGNED("role.permission.assign", "Role permissions assigned", "role"),
    ROLE_PERMISSION_REVOKED("role.permission.revoke", "Role permission revoked", "role"),
    PERMISSION_CREATED("permission.create", "Permission created", "permission"),
    PERMISSION_UPDATED("permission.update", "Permission updated", "permission"),
    PERMISSION_ACTIVATED("permission.activate", "Permission activated", "permission"),
    PERMISSION_DEACTIVATED("permission.deactivate", "Permission deactivated", "permission"),
    PERMISSION_DELETED("permission.delete", "Permission deleted", "permission"),
    CLIENT_CREATED("client.create", "OAuth client registered", "client"),
    CLIENT_UPDATED("client.update", "OAuth client updated", "client"),
    CLIENT_SECRET_ROTATED("client.secret.rotate", "OAuth client secret rotated", "client"),
    CLIENT_DELETED("client.delete", "OAuth client deleted", "client"),
    SESSION_TERMINATED("session.terminate", "Browser session terminated", "session"),
    SESSIONS_TERMINATED("session.terminate_all", "All user browser sessions terminated", "session"),
    LOGIN_SUCCESS("login.success", "Successful sign-in", "authentication"),
    LOGIN_FAILURE("login.failure", "Failed sign-in", "authentication");

    private final String code;
    private final String label;
    private final String resource;
    AuditAction(String code, String label, String resource) { this.code = code; this.label = label; this.resource = resource; }
    public String code() { return code; }
    public String label() { return label; }
    public String resource() { return resource; }
}
