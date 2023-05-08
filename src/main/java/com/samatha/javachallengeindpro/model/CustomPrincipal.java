package com.samatha.javachallengeindpro.model;

import com.samatha.javachallengeindpro.dto.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomPrincipal implements UserDetails {
    private User user;

    public CustomPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return the authorities/roles assigned to the user
        // You can customize this based on your application's requirements
        return List.of(new SimpleGrantedAuthority(user.getRole().toString()));
    }

    @Override
    public String getPassword() {
        // Return the password of the user
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Return the username of the user
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    // Implement the remaining methods of the UserDetails interface based on your requirements

    public User getUser() {
        return user;
    }
}
