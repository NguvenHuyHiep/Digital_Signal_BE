package com.lpdev.dsd.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.commons.enums.DeviceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
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
public class Device implements Serializable {
  @Serial private static final long serialVersionUID = -7623355178363917940L;

  @Schema(example = "1")
  Long id;

  @Schema(example = "Device code 1")
  String code;

  @Schema(example = "Device 1")
  String name;

  @Schema(example = "Information 1")
  String information;

  @Schema(example = "High-resolution display")
  String description;

  String serialNo;

  String vehicleNumber;

  String ybs;

  DeviceStatus status;

  @Schema(implementation = User.class)
  User user;

  @Schema(implementation = DeviceGroup.class)
  DeviceGroup deviceGroup;

  List<DeviceLog> deviceLogs;
}
