package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JwtUserDetailsServiceImpl implements UserDetailsService {

  @Autowired private UserService userService;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    com.lpdev.dsd.models.dtos.User loggInUser = userService.getByEmail(email);
    if (loggInUser != null && !loggInUser.getRoles().isEmpty()) {
      return new User(
          loggInUser.getEmail(),
          loggInUser.getPassword(),
          loggInUser.getRoles().stream()
              .map(r -> new SimpleGrantedAuthority(r.getType().getValue()))
              .toList());
    } else {
      throw new UsernameNotFoundException(DsdConstant.ERROR.AUTH.NOT_FOUND);
    }
  }
}
