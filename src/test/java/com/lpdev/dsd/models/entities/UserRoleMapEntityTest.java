package com.lpdev.dsd.models.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.lpdev.dsd.DsdApplication;
import com.lpdev.dsd.commons.enums.RoleType;
import com.lpdev.dsd.commons.utils.DsdTestPostgreSQLContainer;
import com.lpdev.dsd.commons.utils.TestUtils;
import com.lpdev.dsd.configs.SecurityConfig;
import com.lpdev.dsd.repositories.RoleRepository;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.repositories.UserRoleMapRepository;
import java.util.List;
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
class UserRoleMapEntityTest {

  @Container
  public static PostgreSQLContainer<DsdTestPostgreSQLContainer> postgreSQLContainer =
      DsdTestPostgreSQLContainer.getInstance();

  @Autowired PasswordEncoder passwordEncoder;

  @Autowired UserRoleMapRepository userRoleMapRepository;
  @Autowired UserRepository userRepository;
  @Autowired RoleRepository roleRepository;

  @Test
  @Transactional
  void shouldInitMappingUserRole() {
    UserEntity adminUserEntity = userRepository.findByEmail("dsdadmin@gmail.com").orElse(null);
    assertNotNull(adminUserEntity);
    List<UserRoleMapEntity> adminRoleMapEntities =
        userRoleMapRepository.findByUser(adminUserEntity);
    assertNotNull(adminRoleMapEntities);
    // admin should have user role
    assertNotNull(
        adminRoleMapEntities.stream()
            .filter(Objects::nonNull)
            .filter(urm -> urm.getRole() != null)
            .filter(urm -> urm.getRole().getType().isUser())
            .findFirst());
    // admin should have admin role
    assertNotNull(
        adminRoleMapEntities.stream()
            .filter(Objects::nonNull)
            .filter(urm -> urm.getRole() != null)
            .filter(urm -> urm.getRole().getType().isAdmin())
            .findFirst());
  }

  @Test
  @Transactional
  void shouldBePersistedWhenCreatingNewRecord() {
    RoleEntity roleEntity =
        RoleEntity.builder().name(RoleType.UNDEFINED.getValue()).type(RoleType.UNDEFINED).build();
    RoleEntity savedRoleEntity = roleRepository.save(roleEntity);
    assertNotNull(savedRoleEntity);
    assertNotNull(savedRoleEntity.getId());
    assertEquals(RoleType.UNDEFINED.getValue(), savedRoleEntity.getName());
    assertEquals(RoleType.UNDEFINED, savedRoleEntity.getType());

    UserEntity userEntity = TestUtils.initTestUserEntity(passwordEncoder);
    UserEntity savedUserEntity = userRepository.save(userEntity);
    assertNotNull(savedUserEntity.getId());
    assertTrue(passwordEncoder.matches("Abcd1234", savedUserEntity.getPassword()));
    assertEquals("phanquan2401", savedUserEntity.getUserName());
    assertEquals("phanquan2401@gmail.com", savedUserEntity.getEmail());
    assertEquals("0971025493", savedUserEntity.getPhone());
    assertEquals("phan", savedUserEntity.getFirstName());
    assertEquals("quan", savedUserEntity.getLastName());

    UserRoleMapEntity userRoleMapEntity =
        TestUtils.initTestUserRoleMapEntity(savedUserEntity, savedRoleEntity);
    UserRoleMapEntity createdUserRoleMapEntity = userRoleMapRepository.save(userRoleMapEntity);
    assertNotNull(createdUserRoleMapEntity.getId());
    assertEquals("phanquan2401@gmail.com", createdUserRoleMapEntity.getUser().getEmail());
    assertEquals(RoleType.UNDEFINED, createdUserRoleMapEntity.getRole().getType());
  }
}
