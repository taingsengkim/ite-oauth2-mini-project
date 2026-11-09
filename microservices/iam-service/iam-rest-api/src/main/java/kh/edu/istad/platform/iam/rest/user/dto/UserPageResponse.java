
package kh.edu.istad.platform.iam.rest.user.dto;

import java.util.List;

public record UserPageResponse(
        List<UserSummaryResponse> content,
        PageMetadata page
) {

    public UserPageResponse {
        content = List.copyOf(content);
    }
    public record PageMetadata(int number, int size, long totalElements,int totalPages
    ) {}
}
