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
public class OtpRequest implements Serializable {
  @Serial private static final long serialVersionUID = -8545968616146196795L;

  @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PARAM_OTP)
  String otp;

  @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PARAM_EMAIL)
  @Email(message = DsdConstant.ERROR.REQUEST.INVALID_PARAM_EMAIL)
  String email;
}
