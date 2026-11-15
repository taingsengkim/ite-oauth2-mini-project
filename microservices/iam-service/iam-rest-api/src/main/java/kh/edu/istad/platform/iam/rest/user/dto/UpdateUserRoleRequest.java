package kh.edu.istad.platform.iam.rest.user.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateUserRoleRequest(@Future Instant expiresAt, @Size(max = 255) String reason) {}
