package kh.edu.istad.platform.iam.rest.account.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.edu.istad.platform.iam.dto.user.UpdateAccountProfileCommand;
import kh.edu.istad.platform.iam.usecase.user.ManageAccountProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/account/profile")
public class AccountProfileController {
    private final ManageAccountProfileUseCase account;
    public record Request(@NotBlank @Size(max=50) String username, @NotBlank @Email @Size(max=255) String email,
            @NotBlank @Size(max=100) String givenName, @NotBlank @Size(max=100) String familyName,
            @NotNull @Size(max=100) String nickname, @NotNull @Pattern(regexp="(?:|\\+[1-9]\\d{1,14})") String phoneNumber,
            @NotNull @Size(max=500) String pictureUrl, @NotBlank @Pattern(regexp="en|km") String locale, @NotNull Instant expectedUpdatedAt) {}
    @GetMapping public ResponseEntity<ManageAccountProfileUseCase.Profile> get() { return response(account.get()); }
    @PutMapping public ResponseEntity<ManageAccountProfileUseCase.Saved> update(@Valid @RequestBody Request request) {
        return response(account.update(new UpdateAccountProfileCommand(request.username(), request.email(), request.givenName(), request.familyName(), request.nickname(),
                request.phoneNumber(), request.pictureUrl(), request.locale(), request.expectedUpdatedAt())));
    }
    private <T> ResponseEntity<T> response(T value) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value); }
}
