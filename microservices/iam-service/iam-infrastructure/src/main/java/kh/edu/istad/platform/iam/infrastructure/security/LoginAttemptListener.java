package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.usecase.user.RecordLoginAttemptUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginAttemptListener {

    private final RecordLoginAttemptUseCase recordLoginAttempt;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        var authentication = event.getAuthentication();

        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            return;
        }

        recordLoginAttempt.execute(authentication.getName(), true);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        var authentication = event.getAuthentication();

        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            return;
        }

        recordLoginAttempt.execute(authentication.getName(), false);
    }
}