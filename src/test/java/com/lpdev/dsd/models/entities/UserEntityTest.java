package com.lpdev.dsd.models.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.lpdev.dsd.DsdApplication;
import com.lpdev.dsd.commons.utils.DsdTestPostgreSQLContainer;
import com.lpdev.dsd.commons.utils.TestUtils;
import com.lpdev.dsd.configs.SecurityConfig;
import com.lpdev.dsd.repositories.UserRepository;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DsdApplication.class)
@Import(SecurityConfig.class)
@ActiveProfiles("local")
@Testcontainers
class UserEntityTest {

  @Container
  public static PostgreSQLContainer<DsdTestPostgreSQLContainer> postgreSQLContainer =
      DsdTestPostgreSQLContainer.getInstance();

  @Autowired UserRepository userRepository;

  @Autowired PasswordEncoder passwordEncoder;

  @Test
  @Transactional
  void shouldInitAdminUser() {
    UserEntity adminUserEntity = userRepository.findByEmail("dsdadmin@gmail.com").orElse(null);
    assertNotNull(adminUserEntity);
    assertNotNull(adminUserEntity.getId());
    assertNotNull(adminUserEntity.getUserRoleMap());
    // admin has ROLE_ADMIN and ROLE_USER
    assertEquals(2, adminUserEntity.getUserRoleMap().size());
    // admin should have user role
    assertNotNull(
        adminUserEntity.getUserRoleMap().stream()
            .filter(Objects::nonNull)
            .filter(urm -> urm.getRole() != null)
            .filter(urm -> urm.getRole().getType().isUser())
            .findFirst());
    // admin should have admin role
    assertNotNull(
        adminUserEntity.getUserRoleMap().stream()
            .filter(Objects::nonNull)
            .filter(urm -> urm.getRole() != null)
            .filter(urm -> urm.getRole().getType().isAdmin())
            .findFirst());
  }

  @Test
  @Transactional
  void shouldBePersistedWhenCreatingNewRecord() {
    UserEntity userEntity = TestUtils.initTestUserEntity(passwordEncoder);

    UserEntity savedUserEntity = userRepository.save(userEntity);
    assertNotNull(savedUserEntity.getId());
    assertTrue(passwordEncoder.matches("Abcd1234", savedUserEntity.getPassword()));
    assertEquals("phanquan2401", savedUserEntity.getUserName());
    assertEquals("phanquan2401@gmail.com", savedUserEntity.getEmail());
    assertEquals("0971025493", savedUserEntity.getPhone());
    assertEquals("phan", savedUserEntity.getFirstName());
    assertEquals("quan", savedUserEntity.getLastName());
  }
}
