package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.entity.Enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@Component
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class  JwtAuthentication {

  @Value("${jwt.signing.key}")
  private  String signingKey;

  private final String className = this.getClass().getSimpleName();

//  @Override
//  protected void doFilterInternal(HttpServletRequest request,
//                                  HttpServletResponse response,
//                                  FilterChain filterChain) throws ServletException, IOException {
//    var jwt = extractJwtFromRequest(request);
//    log.info("extractJwtFromRequest " + jwt);
//    if (StringUtils.hasText(jwt) && validateToken(jwt)) {
//      var username = getUsernameFromToken(jwt);
//      var role = getRoleFromToken(jwt);
//      log.info("getRoleFromToken " + role);
//
//      List<GrantedAuthority> authorities = new ArrayList<>();
//      authorities.add(new SimpleGrantedAuthority(role));
//
//      UsernamePasswordAuthenticationToken authentication =
//          new UsernamePasswordAuthenticationToken(username, null, authorities);
//      SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//    filterChain.doFilter(request, response);
//  }

  public String generateToken(String userId,String userEmail, Role role) {
    var token = Jwts.builder()
        .setSubject(userEmail)
        .claim("role", role)
        .claim("userId", userId)
        .setIssuedAt(new Date())
        .setExpiration(new Date((new Date()).getTime() + 1000 * 60 * 60 * 10))
        .signWith(SignatureAlgorithm.HS512, signingKey)
        .compact();
    log.info("AuthServiceImp::generateToken. Role JWT to Role ({}).", role);
    log.info("AuthServiceImp::generateToken. Generate JWT to user ({}).", userEmail);
    return token;
  }

  public String getRoleFromToken(String token) {
    var claims = Jwts.parser()
        .setSigningKey(signingKey)
        .parseClaimsJws(token)
        .getBody();
    return "ROLE_" + claims.get("role", String.class);
  }

  public String getUserIdFromToken(String token) {
    var claims = Jwts.parser()
        .setSigningKey(signingKey)
        .parseClaimsJws(token)
        .getBody();
    return claims.get("userId", String.class);
  }

  public String extractJwtFromRequest(HttpServletRequest request) {
    var bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  public boolean validateToken(String token) {
    try {

      var claims = Jwts.parser()
          .setSigningKey(signingKey)
          .parseClaimsJws(token)
          .getBody();

      if (claims.getExpiration().before(new Date())) {
        return false;
      }

      var role = claims.get("role", String.class);


      return role != null && (checkRoleContains(role));

    } catch (Exception e) {

      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    return Jwts.parser().setSigningKey(signingKey).parseClaimsJws(token).getBody().getSubject();
  }

  public boolean checkRoleContains(String role) {
    return Role.getAllRoles().contains(Role.valueOf(role));
  }
}