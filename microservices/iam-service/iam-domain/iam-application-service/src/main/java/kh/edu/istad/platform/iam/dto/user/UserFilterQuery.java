package kh.edu.istad.platform.iam.dto.user;

import kh.edu.istad.platform.iam.dto.pagination.PageQuery;
import kh.edu.istad.platform.iam.exception.IamDomainException;

public record UserFilterQuery(int page, int size, String sort, String search, Boolean enabled) {
    public UserFilterQuery {
        new PageQuery(page, size, sort);
        if (search != null && search.length() > 255) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR,
                    "Search must not exceed 255 characters");
        }
        search = search == null || search.isBlank() ? null : search.strip();
    }

    public PageQuery pageQuery() {
        return new PageQuery(page, size, sort);
    }
}
