package org.hms.hostelmaintanancesystem.request;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building dynamic JPA Specifications for MaintenanceRequests.
 *
 * This allows us to dynamically construct SQL queries depending on which
 * filters the client provides (e.g., status, category).
 */
public class RequestSpecification {

    /**
     * Builds a specification that dynamically filters by status and category.
     * If a filter is null, it is simply ignored in the WHERE clause.
     */
    public static Specification<MaintenanceRequest> filterBy(RequestStatus status, RequestCategory category) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                // Generates: WHERE status = ?
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (category != null) {
                // Generates: WHERE category = ? (AND status = ?)
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
