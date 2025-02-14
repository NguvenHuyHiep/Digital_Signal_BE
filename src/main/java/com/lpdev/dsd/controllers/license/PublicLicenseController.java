package com.lpdev.dsd.controllers.license;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.dtos.License;
import com.lpdev.dsd.models.requests.LicenseVerifyRequest;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.LicenseService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/public/license")
public class PublicLicenseController {

  private final LicenseService licenseService;

  @PostMapping("/verify")
  public ResponseEntity<BaseOutput<License>> verify(
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          LicenseVerifyRequest request) {
    if (request == null) {
      BaseOutput<License> response =
          BaseOutput.<License>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    License validLicense = licenseService.verify(request.getCode());
    return ResponseEntity.ok(
        BaseOutput.<License>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(validLicense)
            .build());
  }
}
