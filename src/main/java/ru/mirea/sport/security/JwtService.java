package ru.mirea.sport.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import ru.mirea.sport.domain.User;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secretBase64()));
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = Date.from(LocalDateTime.now().plusMinutes(properties.accessMinutes())
                .atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiration(expiry)
                .claim("uid", user.getId().toString())
                .claim("roles", user.getRoles().stream().map(r -> r.getName().name()).toList())
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}