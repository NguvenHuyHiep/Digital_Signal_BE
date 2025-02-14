package com.lpdev.dsd.configs.inits;

import com.lpdev.dsd.models.dtos.Role;
import com.lpdev.dsd.models.dtos.User;
import com.lpdev.dsd.services.RoleService;
import com.lpdev.dsd.services.UserRoleMapService;
import com.lpdev.dsd.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
public class CommonInitializer implements ApplicationRunner {

  @Autowired private RoleService roleService;
  @Autowired private UserService userService;
  @Autowired private UserRoleMapService userRoleMapService;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    this.initRoles();
    this.initAdminUser();
  }

  private void initRoles() {

    Role userRole = roleService.getUserRole();
    if (userRole == null) {
      userRole = roleService.createUserRole();
    }
    log.info("Created role: {}", userRole.getType());

    Role adminRole = roleService.getAdminRole();
    if (adminRole == null) {
      adminRole = roleService.createAdminRole();
    }
    log.info("Created role: {}", adminRole.getType());
  }

  private void initAdminUser() {

    User oldAdminUser = userService.findLoggedInfoByEmail("dsdadmin@gmail.com");

    if (oldAdminUser != null
        && !oldAdminUser.getRoles().isEmpty()
        && oldAdminUser.getRoles().stream()
            .anyMatch(role -> role.getType() != null && role.getType().isUser())
        && oldAdminUser.getRoles().stream()
            .anyMatch(role -> role.getType() != null && role.getType().isAdmin())) {
      log.info("Admin exists");
      return;
    }

    User newAdminUser =
        User.builder()
            .email("dsdadmin@gmail.com")
            .password("Abcd1234")
            .userName("dsdadmin")
            .phone("")
            .build();
    User createdAdminUser = userService.createAdmin(newAdminUser);
    log.info(
        "Created admin user with id: {}, email: {}, username: {}",
        createdAdminUser.getId(),
        createdAdminUser.getEmail(),
        createdAdminUser.getUserName());
  }
}
