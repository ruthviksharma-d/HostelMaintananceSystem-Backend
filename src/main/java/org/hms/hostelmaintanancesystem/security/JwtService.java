package org.hms.hostelmaintanancesystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * Service responsible for JWT token generation, validation, and claim extraction.
 *
 * Uses the JJWT library (io.jsonwebtoken) for all JWT operations.
 *
 * Token structure (3 parts separated by dots):
 *   Header.Payload.Signature
 *
 *   Header:  {"alg": "HS256", "typ": "JWT"}
 *   Payload: {"sub": "john@example.com", "role": "TENANT", "userId": 1, "iat": ..., "exp": ...}
 *   Signature: HMAC-SHA256(header + payload, secretKey)
 *
 * Security notes:
 *   - The secret key must be at least 256 bits for HS256.
 *   - Tokens are stateless — the server never stores them.
 *   - Expiration is enforced on every validation.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    /**
     * Constructor: initializes the signing key from the Base64-encoded secret
     * and reads the expiration time from configuration.
     *
     * @Value -> Spring injects values from application.properties / application-dev.properties.
     *
     * Why Base64-decode the secret?
     *   The secret is stored as a Base64 string in properties for safe transport.
     *   We decode it to raw bytes, then create an HMAC-SHA key from those bytes.
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the given user.
     *
     * Claims included in the payload:
     *   - sub (subject): user's email (used as the unique identifier)
     *   - userId: database ID (useful for frontend without an extra API call)
     *   - role: user's role (TENANT or MAINTENANCE)
     *   - iat (issued at): current timestamp
     *   - exp (expiration): current time + configured expiration
     *
     * @param email  the user's email (becomes the token subject)
     * @param userId the user's database ID
     * @param role   the user's role name (e.g., "TENANT")
     * @return signed JWT token string
     */
    public String generateToken(String email, Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claims(Map.of(
                        "userId", userId,
                        "role", role
                ))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the email (subject) from a valid token.
     *
     * @param token the JWT token string
     * @return the email stored in the token's subject claim
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the userId from a valid token.
     *
     * @param token the JWT token string
     * @return the user's database ID
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * Extracts the role from a valid token.
     *
     * @param token the JWT token string
     * @return the role string (e.g., "TENANT")
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Validates a token by checking:
     *   1. Signature integrity (was it signed with our secret?)
     *   2. Expiration (is it still valid?)
     *   3. Subject matches the expected email
     *
     * If any check fails, returns false.
     *
     * @param token the JWT token string
     * @param email the expected email to match against the token's subject
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            return tokenEmail.equals(email) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            // Malformed, expired, unsupported, or tampered token
            return false;
        }
    }

    /**
     * Checks whether the token has expired.
     *
     * @param token the JWT token string
     * @return true if the token's expiration is before the current time
     */
    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * Parses and validates the token, returning all claims.
     *
     * This is the core method — all extraction methods delegate to this.
     * If the signature is invalid or the token is malformed, JJWT throws
     * a JwtException which we let propagate.
     *
     * @param token the JWT token string
     * @return the Claims object containing all payload data
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
