package com.lpdev.dsd.commons.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(enumAsRef = true)
@RequiredArgsConstructor
@Getter
public enum ResponseStatus {
  SUCCESS("SUCCESS"),
  FAILED("FAILED");

  private final String value;

  public static ResponseStatus parse(final String status) {
    return Stream.of(ResponseStatus.values())
        .filter(e -> e.value.equals(status))
        .findFirst()
        .orElse(ResponseStatus.FAILED);
  }

  public boolean isSuccess() {
    return this == SUCCESS;
  }

  public boolean isFailed() {
    return this == FAILED;
  }
}
