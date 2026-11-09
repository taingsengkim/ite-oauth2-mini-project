package kh.edu.istad.platform.iam.dto.pagination;

import kh.edu.istad.platform.iam.exception.IamDomainException;

public record PageQuery(
        int page, int size, String sort
) {

    public PageQuery {
        if (page < 0) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Size must be between 1 and 100");
        }
    }
}
