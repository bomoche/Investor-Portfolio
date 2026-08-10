package com.enviro.assessment.junior.bonganimoche.security;

import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapts an Investor to Spring Security's UserDetails contract.
 *
 * A wrapper rather than making Investor itself implement UserDetails: that
 * would put security concerns onto a JPA entity and drag framework interfaces
 * into the domain model. This keeps the two layers separable.
 *
 * Retains the investor id so controllers can resolve the current investor
 * without a second database lookup on every request.
 */
@Getter
public class InvestorDetails implements UserDetails {

    private final Long investorId;
    private final String email;
    private final String passwordHash;
    private final String role;

    public InvestorDetails(Investor investor) {
        this.investorId = investor.getId();
        this.email = investor.getEmail();
        this.passwordHash = investor.getPasswordHash();
        this.role = investor.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security's convention: roles carry a ROLE_ prefix internally.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    // Account state flags — no lockout or expiry model in scope.
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}