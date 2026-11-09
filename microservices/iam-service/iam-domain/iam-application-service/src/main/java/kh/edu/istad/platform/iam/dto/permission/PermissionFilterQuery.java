package kh.edu.istad.platform.iam.dto.permission;

import kh.edu.istad.platform.iam.dto.pagination.PageQuery;
import kh.edu.istad.platform.iam.exception.IamDomainException;

public record PermissionFilterQuery(int page, int size, String sort, String search, String resource, String action, Boolean active) {
    public PermissionFilterQuery {
        new PageQuery(page, size, sort);
        if (search != null && search.length() > 255) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR,
                    "Search must not exceed 255 characters");
        }
        if ((resource != null && resource.length() > 50) || (action != null && action.length() > 50)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR,
                    "Resource and action filters must not exceed 50 characters");
        }
        resource = resource == null || resource.isBlank() ? null : resource.strip();
        action = action == null || action.isBlank() ? null : action.strip();
        search = search == null || search.isBlank() ? null : search.strip();
    }

    public PageQuery pageQuery() {
        return new PageQuery(page, size, sort);
    }
}
