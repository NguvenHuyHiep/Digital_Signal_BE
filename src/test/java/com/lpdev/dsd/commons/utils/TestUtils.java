package com.lpdev.dsd.commons.utils;

import com.lpdev.dsd.commons.enums.RoleType;
import com.lpdev.dsd.models.entities.RoleEntity;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.models.entities.UserRoleMapEntity;
import lombok.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestUtils {
  public static UserEntity initTestUserEntity(@NonNull PasswordEncoder passwordEncoder) {
    return UserEntity.builder()
        .userName("phanquan2401")
        .email("phanquan2401@gmail.com")
        .phone("0971025493")
        .firstName("phan")
        .lastName("quan")
        .password(passwordEncoder.encode("Abcd1234"))
        .build();
  }

  public static RoleEntity initTestRoleEntity(@NonNull RoleType type) {
    return RoleEntity.builder().name(type.getValue()).type(type).build();
  }

  public static UserRoleMapEntity initTestUserRoleMapEntity(
      @NonNull UserEntity user, @NonNull RoleEntity role) {
    return UserRoleMapEntity.builder().user(user).role(role).build();
  }
}
