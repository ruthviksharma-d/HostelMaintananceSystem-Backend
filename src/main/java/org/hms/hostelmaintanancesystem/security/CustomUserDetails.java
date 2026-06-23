package org.hms.hostelmaintanancesystem.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hms.hostelmaintanancesystem.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Bridges our domain User entity to Spring Security's UserDetails interface.
 *
 * Spring Security knows NOTHING about our User entity.
 * It only understands UserDetails.
 * This class wraps our entity and translates domain concepts to security concepts.
 */
@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Spring Security uses "username" as the login identifier.
     * Our system uses email as the unique login field.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
