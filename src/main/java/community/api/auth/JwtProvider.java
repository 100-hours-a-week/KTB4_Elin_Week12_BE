package community.api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String createToken(
            String type,
            Long userId,
            Map<String, Object> claims,
            long expSeconds
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("typ", type)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith((SecretKey) key, Jwts.SIG.HS256)
                .compact();
    }

    public String createAccessToken(Long userId, String email, String nickname) {
        return createToken(
                "access",
                userId,
                Map.of("email", email, "nickname", nickname),
                jwtProperties.getAccessTokenExpSeconds()
        );
    }

    public String createRefreshToken(Long userId) {
        return createToken(
                "refresh",
                userId,
                Map.of(),
                jwtProperties.getRefreshTokenExpSeconds()
        );
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parse(token).getPayload().get("typ", String.class));
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    public Long getAccessTokenValidityInMilliseconds() {
        return jwtProperties.getAccessTokenExpSeconds() * 1000;
    }
}