package com.lpdev.dsd.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User implements Serializable {
  @Serial private static final long serialVersionUID = 587129066277401871L;

  @Schema(example = "1")
  Long id;

  @Schema(example = "joindone")
  String userName;

  @Schema(example = "exmapleemail@gmail.com")
  String email;

  @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  String password;

  @Schema(type = "string", example = "1900 01XX")
  String phone;

  @Schema(example = "Join")
  String firstName;

  @Schema(example = "Done")
  String lastName;

  transient List<Role> roles;

  @Schema(implementation = License.class)
  License license;
}
