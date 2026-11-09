package kh.edu.istad.platform.iam.dto.user;

import java.util.UUID;

public record UpdateUserProfileCommand(
        UUID uuid,
        String givenName,
        String familyName,
        String nickname,
        String locale,
        String phoneNumber
) {}