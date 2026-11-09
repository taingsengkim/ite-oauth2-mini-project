package kh.edu.istad.platform.iam.port.out;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID currentUserUuid();
}
