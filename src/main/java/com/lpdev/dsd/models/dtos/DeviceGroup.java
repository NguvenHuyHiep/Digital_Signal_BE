package com.lpdev.dsd.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.commons.enums.Status;
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
public class DeviceGroup implements Serializable {
  @Serial private static final long serialVersionUID = 3017656546336780681L;

  @Schema(example = "1")
  Long id;

  @Schema(example = "Group 1")
  String name;

  @Schema(example = "High resolution screens")
  String description;

  Status status;

  List<Device> devices;

  @Schema(implementation = User.class)
  User user;

  Playlist playlist;
}
