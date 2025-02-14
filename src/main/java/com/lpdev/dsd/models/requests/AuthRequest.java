package com.lpdev.dsd.models.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.commons.constants.DsdConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class AuthRequest implements Serializable {
  @Serial private static final long serialVersionUID = -8075627584991337938L;

  @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY_EMAIL)
  @Email(message = DsdConstant.ERROR.REQUEST.INVALID_BODY_EMAIL)
  String email;

  @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY_PASSWORD)
  String password;

  Boolean isNotSendingEmail;
}
