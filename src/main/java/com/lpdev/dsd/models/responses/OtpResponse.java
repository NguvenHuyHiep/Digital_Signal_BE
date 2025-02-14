package com.lpdev.dsd.models.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.models.dtos.User;
import java.io.Serial;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class OtpResponse implements Serializable {
  @Serial private static final long serialVersionUID = -4144465892922141059L;
  User user;
  String token;
}
