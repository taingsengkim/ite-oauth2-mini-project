package kh.edu.istad.platform.iam.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Forward known SPA pages only; assets, public recovery, APIs and authentication endpoints keep their own handlers. */
@Controller
public class DashboardRouteController {
    @GetMapping({"/dashboard", "/dashboard/",
            "/dashboard/{page:overview|users|roles|permissions|clients|audit-logs|configuration|about}",
            "/dashboard/{page:overview|users|roles|permissions|clients|audit-logs|configuration|about}/",
            "/dashboard/{resource:users|roles|clients}/{id}",
            "/dashboard/{resource:users|roles|clients}/{id}/",
            "/dashboard/{resource:users|roles|clients}/{id}/edit",
            "/dashboard/{resource:users|roles|clients}/{id}/edit/",
            "/dashboard/users/{id}/{action:roles|permissions|sessions|login-history|reset-password|enable|disable|lock|unlock|delete}",
            "/dashboard/users/{id}/{action:roles|permissions|sessions|login-history|reset-password|enable|disable|lock|unlock|delete}/",
            "/dashboard/account/{page:profile|security|linked-accounts|sessions|login-history|activity|deactivation}",
            "/dashboard/account/{page:profile|security|linked-accounts|sessions|login-history|activity|deactivation}/"})
    public String dashboard() { return "forward:/dashboard/index.html"; }
}
