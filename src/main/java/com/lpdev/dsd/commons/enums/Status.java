package com.lpdev.dsd.commons.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(enumAsRef = true)
@RequiredArgsConstructor
@Getter
public enum Status {
  ACTIVE("ACTIVE"),
  INACTIVE("INACTIVE");

  private final String value;

  public static Status parse(final String status) {
    return Stream.of(Status.values())
        .filter(e -> e.value.equals(status))
        .findFirst()
        .orElse(Status.INACTIVE);
  }

  public boolean isActive() {
    return this == ACTIVE;
  }

  public boolean isInactive() {
    return this == INACTIVE;
  }
}
