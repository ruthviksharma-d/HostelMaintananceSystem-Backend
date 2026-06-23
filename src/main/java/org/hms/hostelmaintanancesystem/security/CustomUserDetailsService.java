package org.hms.hostelmaintanancesystem.security;

import lombok.RequiredArgsConstructor;
import org.hms.hostelmaintanancesystem.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details from the database for Spring Security authentication.
 *
 * Implements UserDetailsService — the ONLY interface Spring Security needs
 * to look up users by their login identifier (in our case, email).
 *
 * Authentication flow:
 *   AuthenticationManager.authenticate()
 *        ↓
 *   DaoAuthenticationProvider
 *        ↓
 *   THIS CLASS: loadUserByUsername(email)
 *        ↓
 *   UserRepository.findByEmail(email)
 *        ↓
 *   Returns CustomUserDetails (wrapping our User entity)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security calls this method during login.
     *
     * @param email the user's email (Spring calls it "username" by convention)
     * @return CustomUserDetails wrapping the found User entity
     * @throws UsernameNotFoundException if no user with this email exists
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));
    }

}
