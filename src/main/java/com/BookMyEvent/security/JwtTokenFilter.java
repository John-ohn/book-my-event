package com.BookMyEvent.security;

import com.BookMyEvent.service.serviceImp.JwtAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtAuthentication jwtAuthentication;
  private final String className = this.getClass().getSimpleName();

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    var jwt = jwtAuthentication.extractJwtFromRequest(request);
    log.info("{}::doFilterInternal. extractJwtFromRequest {}", className, jwt);
    if (StringUtils.hasText(jwt) && jwtAuthentication.validateToken(jwt)) {
      var username = jwtAuthentication.getUsernameFromToken(jwt);
      var role = jwtAuthentication.getRoleFromToken(jwt);
      var userId = jwtAuthentication.getUserIdFromToken(jwt);
      log.info("{}::doFilterInternal.  getRoleFromToken {}", className, role);

      List<GrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority(role));

      Map<String, Object> principal = new HashMap<>();
      principal.put("username", username);
      principal.put("id", userId);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, null, authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }
}
