package com.BookMyEvent.security;

import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsServiceImpl")
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImp implements UserDetailsService {

  private final UserRepository userRepository;
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User customer = userRepository.findUserByEmail(username).orElseThrow(
        () -> new UsernameNotFoundException("User with this email doesn't exists, you can't get Authentication ")
    );
    log.info("From UserDetailsServiceImp method -loadUserByUsername- check if email exists: {} ", username);

    return new SecurityUser(customer);
  }
}
