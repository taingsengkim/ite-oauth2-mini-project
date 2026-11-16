package kh.edu.istad.platform.iam.dto.user;

import java.time.Instant;

public record UpdateAccountProfileCommand(String username, String email, String givenName, String familyName,
        String nickname, String phoneNumber, String pictureUrl, String locale, Instant expectedUpdatedAt) {}
