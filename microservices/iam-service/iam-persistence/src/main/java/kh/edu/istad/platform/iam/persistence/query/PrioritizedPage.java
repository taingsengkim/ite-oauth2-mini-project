package kh.edu.istad.platform.iam.persistence.query;

import jakarta.persistence.criteria.*;
import kh.edu.istad.platform.iam.dto.pagination.PageQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.*;

/** Orders matching records before pagination, without changing filter/count semantics. */
public record PrioritizedPage(PageRequest requested, UUID priorityUuid) {
    public static PrioritizedPage recent(PageQuery query, Set<String> fields, UUID priorityUuid) {
        var normalized = new PageQuery(query.page(), query.size(),
                query.sort() == null || query.sort().isBlank() ? "updatedAt,desc" : query.sort());
        return new PrioritizedPage(PersistencePageQueries.page(normalized, "updatedAt", fields), priorityUuid);
    }
    public void order(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (priorityUuid == null || query.getResultType() == Long.class || query.getResultType() == long.class) return;
        var orders = new ArrayList<Order>();
        orders.add(builder.asc(builder.<Integer>selectCase().when(builder.equal(root.get("uuid"), priorityUuid), 0).otherwise(1)));
        requested.getSort().forEach(item -> orders.add(item.isAscending()
                ? builder.asc(root.get(item.getProperty())) : builder.desc(root.get(item.getProperty()))));
        query.orderBy(orders);
    }
    public PageRequest pageable() {
        return priorityUuid == null ? requested : PageRequest.of(requested.getPageNumber(), requested.getPageSize(), Sort.unsorted());
    }
}
