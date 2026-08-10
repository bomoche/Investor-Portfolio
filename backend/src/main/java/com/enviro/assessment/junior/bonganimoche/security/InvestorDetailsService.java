package com.enviro.assessment.junior.bonganimoche.security;

import com.enviro.assessment.junior.bonganimoche.repository.InvestorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads an investor for authentication. Spring Security calls this during login
 * and the JWT filter calls it on each authenticated request.
 */
@Service
@RequiredArgsConstructor
public class InvestorDetailsService implements UserDetailsService {

    private final InvestorRepository investorRepository;

    @Override
    @Transactional(readOnly = true)
    public InvestorDetails loadUserByUsername(String email) {
        return investorRepository.findByEmail(email)
                .map(InvestorDetails::new)
                // Message deliberately generic — confirming which emails are
                // registered would enable account enumeration.
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }
}