package kh.edu.istad.platform.iam.rest.permission.dto;

import java.util.List;

public record PermissionPageResponse(List<PermissionResponse> content, PageMetadata page) {
    public PermissionPageResponse { content = List.copyOf(content); }
    public record PageMetadata(int number, int size, long totalElements, int totalPages) {}
}
