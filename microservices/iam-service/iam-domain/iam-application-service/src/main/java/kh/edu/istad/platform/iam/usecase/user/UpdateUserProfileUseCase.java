package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.dto.user.UserResult;
import kh.edu.istad.platform.iam.mapper.user.UserResultMapper;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileUseCase {

    private final UserRepository repository;
    private final CurrentActorProvider actorProvider;
    private final UserResultMapper mapper;
    private final Clock clock;

    @Transactional
    public UserResult execute(UpdateUserProfileCommand command) {
        var user = repository.findByUuidForUpdate(command.uuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", command.uuid()));

        user.updateProfile(
                command.givenName(),
                command.familyName(),
                command.nickname(),
                command.locale(),
                command.phoneNumber(),
                actorProvider.currentActor(),
                clock.instant()
        );

        var saved = repository.updateProfile(user);

        return mapper.toResult(saved);
    }
}
