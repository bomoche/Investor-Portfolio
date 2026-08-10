package com.enviro.assessment.junior.bonganimoche.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Creates and verifies JWTs.
 *
 * Tokens are signed with HMAC-SHA256 using a server-held secret. Signing proves
 * the token was issued by this server and has not been altered — it does not
 * encrypt the contents, so the payload carries only an identifier and never
 * anything sensitive.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Issues a token for an authenticated investor.
     *
     * The investor id goes in as a custom claim so the filter can identify the
     * caller without a database round trip on every request.
     */
    public String generateToken(InvestorDetails investor) {
        Date now = new Date();
        return Jwts.builder()
                .subject(investor.getEmail())
                .claim("investorId", investor.getInvestorId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Verifies signature and expiry in one step.
     *
     * parseSignedClaims throws on a bad signature, a malformed token or an
     * expired one, so a successful parse means the token is trustworthy.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}