package com.lpdev.dsd.models.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.lpdev.dsd.DsdApplication;
import com.lpdev.dsd.commons.enums.RoleType;
import com.lpdev.dsd.commons.utils.DsdTestPostgreSQLContainer;
import com.lpdev.dsd.commons.utils.TestUtils;
import com.lpdev.dsd.configs.SecurityConfig;
import com.lpdev.dsd.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
class RoleEntityTest {

  @Container
  public static PostgreSQLContainer<DsdTestPostgreSQLContainer> postgreSQLContainer =
      DsdTestPostgreSQLContainer.getInstance();

  @Autowired RoleRepository roleRepository;

  @Test
  @Transactional
  void shouldInitUserRole() {
    RoleEntity userRoleEntity = roleRepository.findByType(RoleType.USER).orElse(null);
    assertNotNull(userRoleEntity);
    assertNotNull(userRoleEntity.getId());
    assertNotNull(userRoleEntity.getType());
    assertTrue(userRoleEntity.getType().isUser());
  }

  @Test
  @Transactional
  void shouldInitAdminRole() {
    RoleEntity adminRoleEntity = roleRepository.findByType(RoleType.ADMIN).orElse(null);
    assertNotNull(adminRoleEntity);
    assertNotNull(adminRoleEntity.getId());
    assertNotNull(adminRoleEntity.getType());
    assertTrue(adminRoleEntity.getType().isAdmin());
  }

  @Test
  @Transactional
  void shouldBePersistedWhenCreatingNewRecord() {
    RoleEntity newRoleEntity = TestUtils.initTestRoleEntity(RoleType.UNDEFINED);

    RoleEntity savedRoleEntity = roleRepository.save(newRoleEntity);
    assertNotNull(savedRoleEntity);
    assertNotNull(savedRoleEntity.getId());
    assertEquals(RoleType.UNDEFINED.getValue(), savedRoleEntity.getName());
    assertEquals(RoleType.UNDEFINED, savedRoleEntity.getType());
  }
}
