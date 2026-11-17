package kh.edu.istad.platform.iam.dto.user;

import kh.edu.istad.platform.iam.dto.pagination.PageQuery;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import java.util.UUID;

public record UserFilterQuery(int page, int size, String sort, String search, Boolean enabled,
                              Boolean locked, Boolean emailVerified, UUID priorityUuid) {
    public UserFilterQuery(int page, int size, String sort, String search, Boolean enabled,
                           Boolean locked, Boolean emailVerified) {
        this(page, size, sort, search, enabled, locked, emailVerified, null);
    }
    public UserFilterQuery(int page, int size, String sort, String search, Boolean enabled) {
        this(page, size, sort, search, enabled, null, null, null);
    }
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
