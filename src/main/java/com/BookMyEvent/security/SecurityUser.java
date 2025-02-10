package com.BookMyEvent.security;

import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
public class SecurityUser implements UserDetails {

  private final User user;

  public SecurityUser(User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(user.getRole().toString()));
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return user.getStatus().equals(Status.ACTIVE);
  }

  @Override
  public boolean isAccountNonLocked() {
    return user.getStatus().equals(Status.ACTIVE);
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return user.getStatus().equals(Status.ACTIVE);
  }

  @Override
  public boolean isEnabled() {
    return user.getStatus().equals(Status.ACTIVE);
  }

  public String getId(){
    return user.getId().toHexString();
  }
}
