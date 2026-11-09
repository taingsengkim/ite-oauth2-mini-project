package kh.edu.istad.platform.iam.persistence.query;

import kh.edu.istad.platform.iam.dto.pagination.PageQuery;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Locale;
import java.util.Set;

public final class PersistencePageQueries {
    private PersistencePageQueries() {}

    public static PageRequest page(PageQuery query, String defaultField, Set<String> allowedFields) {
        String value = query.sort() == null || query.sort().isBlank() ? defaultField + ",asc" : query.sort();
        String[] parts = value.split(",", -1);
        if (parts.length > 2 || !allowedFields.contains(parts[0])
                || (parts.length == 2 && !Set.of("asc", "desc").contains(parts[1]))) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR,
                    "Invalid sort field or direction");
        }
        var direction = parts.length == 2 && parts[1].equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(query.page(), query.size(), Sort.by(direction, parts[0]).and(Sort.by("id")));
    }

    public static String contains(String value) {
        return "%" + value.toLowerCase(Locale.ROOT).replace("\\", "\\\\")
                .replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
