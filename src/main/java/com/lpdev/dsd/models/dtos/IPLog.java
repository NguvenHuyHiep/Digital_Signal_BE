package com.lpdev.dsd.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.commons.enums.DeviceType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
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
public class IPLog implements Serializable {

  @Serial private static final long serialVersionUID = -5563252657766466266L;

  Long id;

  String ip;

  String path;

  String userInfo;

  Date actionTime;

  DeviceType deviceType;

  String deviceId;
}
