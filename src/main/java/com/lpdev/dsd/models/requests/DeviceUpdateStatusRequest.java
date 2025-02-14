package com.lpdev.dsd.models.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.commons.enums.DeviceStatus;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceUpdateStatusRequest implements Serializable {
  @Serial private static final long serialVersionUID = -102135677939454798L;
  String license;
  String code;
  DeviceStatus status;
}
