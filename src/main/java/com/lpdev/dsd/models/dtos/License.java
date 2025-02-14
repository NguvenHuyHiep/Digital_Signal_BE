package com.lpdev.dsd.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
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
public class License implements Serializable {
  @Serial private static final long serialVersionUID = 5170712423877544807L;

  @Schema(example = "1")
  Long id;

  @Schema(example = "46598fa3-e32e-449c-9df7-9e44eb352029")
  String code;

  @Schema(
      example =
          "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJkc2RhZG1pbkBnbWFpbC5jb20iLCJleHAiOjE2OTk0MDg1MTB9.THL8bfhMoylG66wddBmoEisr7lJToM5tQXOYLaGpnIPVFfwIwek3mE4gTixtK6Ql5cX2fUaTV1YOib-BQb1quFKiJC_kxoSocKJDHoYs8Lgtl8lbSZHZ-Hh7zPyfrrXxwHDnIM_5D86GvHSogfamgtkAMJcmGYaQkT1AI-OtpPWrug5PSG88IozDXIpltkALIggHtlVZBRMVTmTLq6n4yoVmvC3UDpJYMh0o0bj19t1Plfp2TuxBDuI2Nv9NhjgQnDYcvMAFV68cK0XPhf6IGx_qXBoThGYJP9KHIL0ERUeQ9JvJbPZyUFV7C5AHbZGJrerNqWaiKTTCatySYeZN8A")
  String token;

  @Schema(
      example =
          "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAppD+7b6vgIJkHBz/mOweyEhsQUF2BQZd/HzIznolLmE2516skTeE4inKUeDDoZ92KuhaUJBYD0tH7r2YHiFkAbyWnWhOzaxjLOvackglBAoPSE5ArIIcXP2+XG/jNVMEnMMYfY/JLj65qo4IMif82CDnNexjUB6RxvI85qaTJ8CjiVxsk9rvDvFTjB/ytjfrCOHjlfWuWkEAH5Zk3Nn+iwjBVXUfJGuIPX5JM6JHpdy+yFU8vtXEYhth6o9jApJhkQRCLEOhxKt5z22QK/a/PH6DIL2ElARGMzeX/YLpbkvB/W1Q4ftyhMWnxT/Jx2ITSMsZu3SSr62WE/Xn6wHtAQIDAQAB")
  String publicKey;

  @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  String privateKey;

  @Schema(example = "2023-11-03T04:08:18.635Z")
  Date activationDate;

  @Schema(example = "2023-12-03T04:08:18.635Z")
  Date expirationDate;

  @Schema(example = "VVIP")
  String description;
}
