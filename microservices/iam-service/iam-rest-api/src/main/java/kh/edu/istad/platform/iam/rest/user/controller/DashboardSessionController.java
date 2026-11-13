package kh.edu.istad.platform.iam.rest.user.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;

@RestController
@RequiredArgsConstructor
public class DashboardSessionController {
    private final CurrentUserProvider currentUser;
    @GetMapping("/dashboard-api/session")
    public ResponseEntity<SessionResponse> session(Authentication authentication, CsrfToken csrf) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new SessionResponse(
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                csrf.getHeaderName(), csrf.getToken(), currentUser.currentUserUuid()));
    }

    public record SessionResponse(List<String> authorities, String csrfHeaderName, String csrfToken, UUID userUuid) {}
}
