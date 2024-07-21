package com.dlnl.deliveryguard.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Value("${spring.jwt.access_exp_time}")
    private long accessExpiration;

    @Value("${spring.jwt.refresh_exp_time}")
    private long refreshExpiration;


    private String encodedSecretKey;

    @PostConstruct
    public void init() {
        this.encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String generateAccessToken(Long id) {
        return Jwts.builder()
                .claim("tokenType", "access")
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(SignatureAlgorithm.HS512, encodedSecretKey)
                .compact();
    }

    public String generateRefreshToken(Long id) {
        long expirationTime = refreshExpiration == -1 ? Long.MAX_VALUE : System.currentTimeMillis() + refreshExpiration;
        return Jwts.builder()
                .claim("id", id)
                .claim("tokenType", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(expirationTime))
                .signWith(SignatureAlgorithm.HS512, encodedSecretKey)
                .compact();
    }

    // 여기까진 괜찮은 듯

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(encodedSecretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public Long getIdFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(encodedSecretKey).parseClaimsJws(token).getBody();
        return claims.get("id", Long.class);
    }
}