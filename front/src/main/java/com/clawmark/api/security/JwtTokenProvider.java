package com.clawmark.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Resource
    private JwtProperties jwtProperties;

    public String generateToken(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(jwtProperties.getExpireDays());

        return Jwts.builder()
                .setSubject(userId)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(toDate(now))
                .setExpiration(toDate(expireTime))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String parseUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public long getExpireSeconds() {
        return jwtProperties.getExpireDays().longValue() * 24L * 60L * 60L;
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
