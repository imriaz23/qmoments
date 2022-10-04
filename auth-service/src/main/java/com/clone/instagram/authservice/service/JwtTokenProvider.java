package com.clone.instagram.authservice.service;

import com.clone.instagram.authservice.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtTokenProvider {

  private final JwtConfig jwtConfig;

  public JwtTokenProvider(JwtConfig jwtConfig) {
    this.jwtConfig = jwtConfig;
  }

  public String generateToken(Authentication authentication) {

    Long now = System.currentTimeMillis();
    return Jwts.builder()
        .setSubject(authentication.getName())
        .claim("authorities", authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + jwtConfig.getExpiration() * 1000))  // in milliseconds
        .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret().getBytes())
        .compact();
  }

  public Claims getClaimsFromJWT(String token) {
    return Jwts.parser()
        .setSigningKey(jwtConfig.getSecret().getBytes())
        .parseClaimsJws(token)
        .getBody();
  }

  public boolean validateToken(String authToken) {
    try {
      Jwts.parser()
          .setSigningKey(jwtConfig.getSecret().getBytes())
          .parseClaimsJws(authToken);

      return true;
    } catch (MalformedJwtException ex) {
      log.error("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty.");
    }
    return false;
  }
}
