package org.hms.hostelmaintanancesystem.request;

import lombok.RequiredArgsConstructor;
import org.hms.hostelmaintanancesystem.common.dto.PageResponse;
import org.hms.hostelmaintanancesystem.common.exception.ResourceNotFoundException;
import org.hms.hostelmaintanancesystem.common.exception.UnauthorizedAccessException;
import org.hms.hostelmaintanancesystem.request.dto.CreateRequestDTO;
import org.hms.hostelmaintanancesystem.request.dto.RequestResponse;
import org.hms.hostelmaintanancesystem.request.dto.UpdateRequestStatusDTO;
import org.hms.hostelmaintanancesystem.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling business logic for Maintenance Requests.
 *
 * Implements CRUD operations with ownership validation.
 */
@Service
@RequiredArgsConstructor
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository requestRepository;

    /**
     * Creates a new maintenance request.
     *
     * @param dto  the input data from the client
     * @param user the currently authenticated user (the creator)
     * @return the created request, mapped to a DTO
     */
    public RequestResponse createRequest(CreateRequestDTO dto, User user) {
        MaintenanceRequest request = MaintenanceRequest.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .status(RequestStatus.OPEN) // Always start as OPEN
                .createdBy(user)
                .build();

        MaintenanceRequest savedRequest = requestRepository.save(request);

        return mapToResponse(savedRequest);
    }

    /**
     * Retrieves a single request by ID.
     *
     * Includes ownership validation:
     *   - MAINTENANCE staff can view ANY request.
     *   - TENANT can only view requests they created.
     *
     * @param id   the request ID
     * @param user the currently authenticated user
     * @return the request DTO
     * @throws ResourceNotFoundException if ID doesn't exist
     * @throws UnauthorizedAccessException if a tenant tries to view someone else's request
     */
    public RequestResponse getRequestById(Long id, User user) {
        MaintenanceRequest request = findRequestEntityById(id);

        // Ownership validation for tenants
        if (user.getRole().name().equals("TENANT") && !request.getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only view your own maintenance requests.");
        }

        return mapToResponse(request);
    }

    /**
     * Retrieves requests created by the given user, with optional filters and pagination.
     * Primarily used by tenants for their dashboard.
     *
     * @param user     the currently authenticated user
     * @param status   optional status filter
     * @param category optional category filter
     * @param pageable pagination parameters (page, size, sort)
     * @return paginated list of their requests
     */
    public PageResponse<RequestResponse> getMyRequests(User user, RequestStatus status, RequestCategory category, Pageable pageable) {
        // Base specification: must belong to the current user
        Specification<MaintenanceRequest> spec = (root, query, cb) ->
                cb.equal(root.get("createdBy"), user);

        // Add dynamic filters
        Specification<MaintenanceRequest> filters = RequestSpecification.filterBy(status, category);
        spec = spec.and(filters);

        Page<MaintenanceRequest> page = requestRepository.findAll(spec, pageable);

        return PageResponse.of(page.map(this::mapToResponse));
    }

    /**
     * Retrieves all requests in the system, with optional filters and pagination.
     * Primarily used by maintenance staff.
     *
     * @param status   optional status filter
     * @param category optional category filter
     * @param pageable pagination parameters
     * @return paginated list of all requests
     */
    public PageResponse<RequestResponse> getAllRequests(RequestStatus status, RequestCategory category, Pageable pageable) {
        Specification<MaintenanceRequest> spec = RequestSpecification.filterBy(status, category);

        Page<MaintenanceRequest> page = requestRepository.findAll(spec, pageable);

        return PageResponse.of(page.map(this::mapToResponse));
    }

    /**
     * Updates the status of a request (by MAINTENANCE staff).
     *
     * If status is set to RESOLVED, a resolution note can be added,
     * and the resolvedAt timestamp is set.
     *
     * @param id   the request ID
     * @param dto  the status update payload
     * @return the updated request DTO
     */
    public RequestResponse updateStatus(Long id, UpdateRequestStatusDTO dto) {
        MaintenanceRequest request = findRequestEntityById(id);

        request.setStatus(dto.getStatus());

        if (dto.getStatus() == RequestStatus.RESOLVED) {
            request.setResolutionNote(dto.getResolutionNote());
            request.setResolvedAt(LocalDateTime.now());
        }

        MaintenanceRequest updatedRequest = requestRepository.save(request);
        return mapToResponse(updatedRequest);
    }

    /**
     * Closes a request (by TENANT).
     *
     * Tenants can only close their own requests, and typically only
     * after they are resolved (or if they want to cancel an open one).
     *
     * @param id   the request ID
     * @param user the currently authenticated tenant
     * @return the updated request DTO
     */
    public RequestResponse closeRequest(Long id, User user) {
        MaintenanceRequest request = findRequestEntityById(id);

        // Ownership validation
        if (!request.getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("You can only close your own maintenance requests.");
        }

        // Optional logic: we could enforce that they can only close if it's RESOLVED,
        // but often tenants cancel requests that are OPEN too. For now, just close it.
        request.setStatus(RequestStatus.CLOSED);
        request.setClosedAt(LocalDateTime.now());

        MaintenanceRequest updatedRequest = requestRepository.save(request);
        return mapToResponse(updatedRequest);
    }

    /**
     * Helper method to fetch the raw entity, used internally for updates/lookups.
     */
    protected MaintenanceRequest findRequestEntityById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found with id: " + id));
    }

    /**
     * Maps the JPA entity to our safe, flattened DTO.
     */
    private RequestResponse mapToResponse(MaintenanceRequest request) {
        return RequestResponse.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(request.getStatus())
                .createdById(request.getCreatedBy().getId())
                .createdByName(request.getCreatedBy().getName())
                .createdByEmail(request.getCreatedBy().getEmail())
                .resolutionNote(request.getResolutionNote())
                .resolvedAt(request.getResolvedAt())
                .closedAt(request.getClosedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

}
