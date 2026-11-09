
package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.UserResult;
import kh.edu.istad.platform.iam.mapper.user.UserResultMapper;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserUseCase {

    private final UserRepository repository;
    private final UserResultMapper mapper;

    @Transactional(readOnly = true)
    public UserResult execute(UUID uuid) {
        var user = repository.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("User", uuid));
        return mapper.toResult(user);
    }
}
