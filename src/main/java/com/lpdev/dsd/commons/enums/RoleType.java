package com.lpdev.dsd.commons.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(enumAsRef = true)
@RequiredArgsConstructor
@Getter
public enum RoleType {
  ADMIN("ROLE_ADMIN"),
  USER("ROLE_USER"),
  UNDEFINED("UNDEFINED");

  private final String value;

  public static RoleType parse(final String role) {
    return Stream.of(RoleType.values())
        .filter(e -> e.value.equals(role))
        .findFirst()
        .orElse(RoleType.UNDEFINED);
  }

  public boolean isAdmin() {
    return this == ADMIN;
  }

  public boolean isUser() {
    return this == USER;
  }

  public boolean isValid() {
    return this != UNDEFINED;
  }
}
