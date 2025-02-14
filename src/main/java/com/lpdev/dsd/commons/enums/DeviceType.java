package com.lpdev.dsd.commons.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(enumAsRef = true)
@RequiredArgsConstructor
@Getter
public enum DeviceType {
  WEB("WEB"),
  APP("APP"),
  UNDEFINED("UNDEFINED");

  private final String value;

  public static DeviceType parse(final String status) {
    return Stream.of(DeviceType.values())
        .filter(e -> e.value.equals(status))
        .findFirst()
        .orElse(DeviceType.UNDEFINED);
  }

  public boolean isWeb() {
    return this == WEB;
  }

  public boolean isApp() {
    return this == APP;
  }
}
