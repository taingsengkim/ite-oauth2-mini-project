package kh.edu.istad.platform.iam.rest.account.controller;

import kh.edu.istad.platform.iam.dto.identity.IdentityResult;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.usecase.user.GetProfileDetailsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/account/identities")
public class AccountIdentityController {
    private final CurrentUserProvider currentUser;
    private final ListUserIdentitiesUseCase identities;
    private final UnlinkIdentityUseCase unlink;
    private final GetProfileDetailsUseCase profile;
    public record Accounts(boolean hasPassword, List<IdentityResult> identities) {}
    @GetMapping public ResponseEntity<Accounts> get() {
        var uuid = currentUser.currentUserUuid();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Accounts(profile.execute(uuid).hasPassword(), identities.execute(uuid)));
    }
    @DeleteMapping("/{identityUuid}") public ResponseEntity<Void> unlink(@PathVariable("identityUuid") UUID identityUuid) {
        unlink.execute(currentUser.currentUserUuid(), identityUuid);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
