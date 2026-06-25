package org.hms.hostelmaintanancesystem.user;

import org.hms.hostelmaintanancesystem.common.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Paginated list of users filtered by role + approval status. */
    Page<User> findByRoleAndApprovalStatus(Role role, ApprovalStatus approvalStatus, Pageable pageable);

    /** Count users by role + approval status (used for pending badge). */
    long countByRoleAndApprovalStatus(Role role, ApprovalStatus approvalStatus);
}
