package com.lpdev.dsd.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.commons.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
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
public class Playlist implements Serializable {
  @Serial private static final long serialVersionUID = 1686881765941615449L;

  @Schema(example = "1")
  Long id;

  @Schema(example = "Science")
  String name;

  @Schema(example = "Playlist Silde")
  String description;

  @Schema(example = "2023-11-03T04:08:18.635Z")
  Date startTime;

  @Schema(example = "2023-11-03T04:08:18.635Z")
  Date endTime;

  @Schema(example = "true")
  Boolean isLoop;

  LinkedHashSet<Long> fileOrder;

  Status status;

  List<DsdFile> files;

  List<DeviceGroup> deviceGroups;

  @Schema(implementation = User.class)
  User user;

  Date lastUpdateDate;
}
