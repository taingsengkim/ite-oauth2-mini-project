package kh.edu.istad.platform.iam.dto.user;

public record ProfileDetailsResult(
        UserResult user,
        String nickname,
        String phoneNumber,
        boolean hasPassword) {}
