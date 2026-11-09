package kh.edu.istad.platform.iam.rest.user.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.rest.user.dto.RegisterUserRequest;
import kh.edu.istad.platform.iam.rest.user.dto.UserResponse;
import kh.edu.istad.platform.iam.rest.user.mapper.UserApiMapper;
import kh.edu.istad.platform.iam.usecase.user.RegisterUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterUserUseCase registerUserUseCase;
    private final UserApiMapper mapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        var result = registerUserUseCase.execute(mapper.toCommand(request));
        return ResponseEntity
                .created(URI.create("/api/users/" + result.uuid()))
                .body(mapper.toResponse(result));
    }
}
