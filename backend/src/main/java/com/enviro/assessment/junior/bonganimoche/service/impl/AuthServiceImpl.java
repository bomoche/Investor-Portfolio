package com.enviro.assessment.junior.bonganimoche.service.impl;

import com.enviro.assessment.junior.bonganimoche.dto.request.LoginRequest;
import com.enviro.assessment.junior.bonganimoche.dto.response.AuthResponse;
import com.enviro.assessment.junior.bonganimoche.entity.Investor;
import com.enviro.assessment.junior.bonganimoche.repository.InvestorRepository;
import com.enviro.assessment.junior.bonganimoche.security.InvestorDetails;
import com.enviro.assessment.junior.bonganimoche.security.JwtService;
import com.enviro.assessment.junior.bonganimoche.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final InvestorRepository investorRepository;
    private final JwtService jwtService;

    /**
     * Delegates credential checking to the AuthenticationManager, which loads
     * the investor via InvestorDetailsService and compares the submitted
     * password against the stored BCrypt hash. Doing the comparison by hand
     * would risk a timing-unsafe implementation.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()));
        } catch (Exception ex) {
            log.warn("Failed login attempt for {}", request.email());
            // One message for both unknown email and wrong password, so the
            // response cannot be used to discover registered addresses.
            throw new BadCredentialsException("Invalid email or password");
        }

        Investor investor = investorRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtService.generateToken(new InvestorDetails(investor));
        log.info("Investor {} authenticated", investor.getId());

        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(),
                investor.getId(), investor.getFullName(), investor.getEmail());
    }
}