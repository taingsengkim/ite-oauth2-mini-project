package kh.edu.istad.platform.iam.mapper.user;

import kh.edu.istad.platform.iam.dto.user.RoleReference;
import kh.edu.istad.platform.iam.dto.user.UserResult;
import kh.edu.istad.platform.iam.dto.user.UserSummary;
import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.usecase.user.UserAuthorityResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResultMapper {
    private final UserApplicationMapper mapper;
    private final UserAuthorityResolver authorities;

    public UserResult toResult(User user) {
        var roles = authorities.resolveDetails(user.getUuid()).roles().stream()
                .map(role -> new RoleReference(role.uuid(), role.name())).toList();
        return mapper.toResult(user, roles);
    }

    public UserSummary toSummary(User user) {
        return mapper.toSummary(toResult(user));
    }
}
