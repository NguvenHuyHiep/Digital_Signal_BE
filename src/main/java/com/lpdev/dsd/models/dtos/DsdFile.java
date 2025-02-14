package com.lpdev.dsd.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.commons.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
public class DsdFile implements Serializable {
  @Serial private static final long serialVersionUID = 4912021903635258688L;

  @Schema(type = "number", example = "12")
  Long id;

  @Schema(type = "string", example = "example-image-name")
  String name;

  @Schema(type = "string", example = "jpg")
  String fileType;

  @Schema(type = "string", example = "folder/path")
  String path;

  @Schema(example = "2023-12-03T04:08:18.635Z")
  Date createDate;

  Status status;

  Date assignDate;

  List<Playlist> playlists;

  @Schema(implementation = User.class)
  User user;

  @Schema(implementation = Category.class)
  Category category;
}
