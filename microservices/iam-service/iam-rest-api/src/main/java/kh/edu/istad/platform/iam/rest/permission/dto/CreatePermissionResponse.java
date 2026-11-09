package kh.edu.istad.platform.iam.rest.permission.dto;

import java.util.UUID;

public record CreatePermissionResponse(
        UUID uuid,
        String resource,
        String action,
        String description
){
}
