package com.alcegory.mescloud.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.alcegory.mescloud.security.constant.SecurityConstant.*;

@Service
public class JwtTokenService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String extractUsername(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }

    public <T> T extractClaim(String jwtToken, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwtToken);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails);
    }

    public boolean isTokenRefreshable(String token) {
        final Date expiration = extractExpiration(token);
        final Date now = new Date();
        final long refreshThreshold = expiration.getTime() - (REFRESH_JWT_EXPIRATION * 1000);

        return now.getTime() > refreshThreshold;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(getExpirationDate())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String jwtToken) {
        return Jwts
                .parserBuilder()
                .setAllowedClockSkewSeconds(ALLOWED_CLOCK_SKEW)
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getJwtTokenFromCookie(Cookie[] cookies, String tokenName) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(tokenName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public boolean isTokenInCookie(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(COOKIE_TOKEN_NAME) || cookie.getName().equals(COOKIE_REFRESH_TOKEN_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void removeCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath(PATH);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    public void setJwtTokenCookie(HttpServletResponse response, String jwtToken) {
        Cookie cookie = new Cookie(COOKIE_TOKEN_NAME, jwtToken);
        cookie.setMaxAge(JWT_EXPIRATION);
        cookie.setPath(PATH);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(COOKIE_REFRESH_TOKEN_NAME, refreshToken);
        refreshTokenCookie.setMaxAge(REFRESH_JWT_EXPIRATION);
        refreshTokenCookie.setPath(PATH);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);
    }
}
