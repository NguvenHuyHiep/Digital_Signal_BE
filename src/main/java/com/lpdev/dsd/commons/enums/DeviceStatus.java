package com.lpdev.dsd.commons.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(enumAsRef = true)
@RequiredArgsConstructor
@Getter
public enum DeviceStatus {
  ONLINE("ONLINE"),
  OFFLINE("OFFLINE"),
  UNDEFINED("UNDEFINED");

  private final String value;

  public static DeviceStatus parse(final String status) {
    return Stream.of(DeviceStatus.values())
        .filter(e -> e.value.equals(status))
        .findFirst()
        .orElse(DeviceStatus.UNDEFINED);
  }

  public boolean isOnline() {
    return this == ONLINE;
  }

  public boolean isOffline() {
    return this == OFFLINE;
  }

  public boolean isValid() {
    return this != UNDEFINED;
  }
}
