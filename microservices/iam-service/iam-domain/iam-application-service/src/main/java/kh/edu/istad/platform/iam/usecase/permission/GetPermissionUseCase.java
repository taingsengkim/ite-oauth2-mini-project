package kh.edu.istad.platform.iam.usecase.permission;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.permission.PermissionResult;
import kh.edu.istad.platform.iam.mapper.permission.PermissionApplicationMapper;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPermissionUseCase {
    private final PermissionRepository repository;
    private final PermissionApplicationMapper mapper;

    @Transactional(readOnly = true)
    public PermissionResult execute(UUID uuid) {
        return mapper.toResult(repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", uuid)));
    }
}
