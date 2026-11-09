package kh.edu.istad.platform.iam.rest.role.dto;
import java.util.List;

public record RolePageResponse(List<RoleSummaryResponse> content, PageMetadata page) {
    public RolePageResponse { content = List.copyOf(content); }
    public record PageMetadata(int number, int size, long totalElements, int totalPages) {}
}
