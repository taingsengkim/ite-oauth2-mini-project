package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.ProfileDetailsResult;
import kh.edu.istad.platform.iam.mapper.user.UserResultMapper;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetProfileDetailsUseCase {
    private final UserRepository users;
    private final UserResultMapper mapper;

    @Transactional(readOnly = true)
    public ProfileDetailsResult execute(UUID uuid) {
        var user = users.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("User", uuid));
        return new ProfileDetailsResult(mapper.toResult(user), user.getNickname(), user.getPhoneNumber(),
                user.getPasswordHash() != null && !user.getPasswordHash().isBlank());
    }
}
