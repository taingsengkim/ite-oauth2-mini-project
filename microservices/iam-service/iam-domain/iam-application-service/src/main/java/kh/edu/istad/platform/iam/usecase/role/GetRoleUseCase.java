package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.role.RoleResult;
import kh.edu.istad.platform.iam.mapper.role.RoleApplicationMapper;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRoleUseCase {
    private final RoleRepository repository;
    private final RoleApplicationMapper mapper;
    @Transactional(readOnly = true)
    public RoleResult execute(UUID uuid) {
        return mapper.toResult(repository.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("Role", uuid)));
    }
}
