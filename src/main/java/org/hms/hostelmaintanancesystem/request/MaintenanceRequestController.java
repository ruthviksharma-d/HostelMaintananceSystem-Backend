package org.hms.hostelmaintanancesystem.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hms.hostelmaintanancesystem.common.ApiResponse;
import org.hms.hostelmaintanancesystem.common.dto.PageResponse;
import org.hms.hostelmaintanancesystem.request.dto.CreateRequestDTO;
import org.hms.hostelmaintanancesystem.request.dto.RequestResponse;
import org.hms.hostelmaintanancesystem.request.dto.UpdateRequestStatusDTO;
import org.hms.hostelmaintanancesystem.security.CustomUserDetails;
import org.hms.hostelmaintanancesystem.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Maintenance Requests.
 *
 * URL-level access control is defined in SecurityConfig.
 *   - POST /api/requests      -> TENANT only
 *   - GET  /api/requests/my   -> TENANT only
 *   - GET  /api/requests      -> MAINTENANCE only
 *
 * Method-level access control (ownership) is enforced in the Service layer.
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class MaintenanceRequestController {

    private final MaintenanceRequestService requestService;

    /**
     * Creates a new maintenance request.
     *
     * Endpoint: POST /api/requests
     * Access: TENANT only
     *
     * @AuthenticationPrincipal injects the UserDetails of the currently logged-in user.
     * This saves us from having to manually fetch it from SecurityContextHolder.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RequestResponse>> createRequest(
            @RequestBody @Valid CreateRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = userDetails.getUser();
        RequestResponse response = requestService.createRequest(requestDTO, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance request created successfully", response));
    }

    /**
     * Retrieves all requests created by the logged-in tenant (with pagination/filters).
     *
     * Endpoint: GET /api/requests/my
     * Access: TENANT only
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<RequestResponse>>> getMyRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        User currentUser = userDetails.getUser();
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<RequestResponse> requests = requestService.getMyRequests(currentUser, status, category, pageable);

        return ResponseEntity
                .ok(ApiResponse.success("Requests retrieved successfully", requests));
    }

    /**
     * Retrieves all requests in the system (with pagination/filters).
     *
     * Endpoint: GET /api/requests
     * Access: MAINTENANCE only
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RequestResponse>>> getAllRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<RequestResponse> requests = requestService.getAllRequests(status, category, pageable);

        return ResponseEntity
                .ok(ApiResponse.success("All requests retrieved successfully", requests));
    }

    /**
     * Retrieves a single request by its ID.
     *
     * Endpoint: GET /api/requests/{id}
     * Access: ANY AUTHENTICATED
     *         (Ownership check inside service: tenant can only view their own)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestResponse>> getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = userDetails.getUser();
        RequestResponse response = requestService.getRequestById(id, currentUser);

        return ResponseEntity
                .ok(ApiResponse.success("Request retrieved successfully", response));
    }

    /**
     * Updates the status of a request.
     *
     * Endpoint: PUT /api/requests/{id}/status
     * Access: MAINTENANCE only
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RequestResponse>> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRequestStatusDTO updateDTO) {

        RequestResponse response = requestService.updateStatus(id, updateDTO);

        return ResponseEntity
                .ok(ApiResponse.success("Request status updated successfully", response));
    }

    /**
     * Closes a request.
     *
     * Endpoint: PUT /api/requests/{id}/close
     * Access: TENANT only (Ownership check inside service)
     */
    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<RequestResponse>> closeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User currentUser = userDetails.getUser();
        RequestResponse response = requestService.closeRequest(id, currentUser);

        return ResponseEntity
                .ok(ApiResponse.success("Request closed successfully", response));
    }

}
