package org.hms.hostelmaintanancesystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for returning paginated data.
 *
 * Wraps Spring Data JPA's Page object into a cleaner, standardized
 * format for the frontend to consume.
 *
 * Example JSON:
 * {
 *   "content": [ ... ],
 *   "pageNo": 0,
 *   "pageSize": 10,
 *   "totalElements": 45,
 *   "totalPages": 5,
 *   "last": false
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /**
     * Helper to easily create our PageResponse from Spring's Page object.
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
