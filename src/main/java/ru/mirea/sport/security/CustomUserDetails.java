package ru.mirea.sport.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.mirea.sport.domain.User;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public UUID getId() { return user.getId(); }
    public boolean isPasswordChangeRequired() { return user.isPasswordChangeRequired(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }

    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { 
        return user.getLockedUntil() == null || user.getLockedUntil().isBefore(java.time.LocalDateTime.now()); 
    }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.isActive(); }
}