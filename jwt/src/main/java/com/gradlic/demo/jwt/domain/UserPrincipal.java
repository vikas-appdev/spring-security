package com.gradlic.demo.jwt.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class UserPrincipal implements UserDetails {

    private User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public UserPrincipal() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return stream(this.user.getAuthorities())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // by passing it is as true bypassing the default, we can create logic for expiry later
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.user.isNotLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // making it true so credentials will not be checked for expiry
    }

    @Override
    public boolean isEnabled() {
        return this.user.isActive();
    }


}
