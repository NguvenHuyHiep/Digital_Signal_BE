package com.lpdev.dsd.models.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.models.dtos.User;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse implements Serializable {
  @Serial private static final long serialVersionUID = -2308534825902669665L;
  transient User user;
  String token;
}
