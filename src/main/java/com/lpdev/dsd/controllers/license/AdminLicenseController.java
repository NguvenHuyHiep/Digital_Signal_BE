package com.lpdev.dsd.controllers.license;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.dtos.License;
import com.lpdev.dsd.models.requests.LicenseExpandRequest;
import com.lpdev.dsd.models.requests.LicenseGenerateRequest;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.LicenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/license")
@Tag(name = "Admin-License API")
@SecurityRequirement(name = "Authorization")
public class AdminLicenseController {
  private final LicenseService licenseService;

  @Operation(
      summary = "Get all License with pagination",
      description = "Returns all License with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  public ResponseEntity<BaseOutput<List<License>>> getByPaging(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {
    Page<License> playlistsPage = licenseService.getByPaging(page, size, sortBy, sortDirection);

    BaseOutput<List<License>> response =
        BaseOutput.<List<License>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(playlistsPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(playlistsPage.getTotalElements())
            .status(ResponseStatus.SUCCESS)
            .data(playlistsPage.getContent())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get License by Id", description = "Returns License by id")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/{id}")
  public ResponseEntity<BaseOutput<License>> getById(
      @PathVariable("id") @NotBlank(message = "error.id.invalid") Long id) {
    if (id <= 0) {
      BaseOutput<License> response =
          BaseOutput.<License>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    License license = licenseService.getById(id);

    BaseOutput<License> response =
        BaseOutput.<License>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(license)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Generate License", description = "Returns created License")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @PostMapping("/generate")
  public ResponseEntity<BaseOutput<License>> generateByEmail(
      @RequestBody LicenseGenerateRequest request) {
    if (request == null
        || StringUtils.isBlank(request.getEmail())
        || request.getDuration() == null) {
      BaseOutput<License> response =
          BaseOutput.<License>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    License createdLicense =
        licenseService.generateByEmail(request.getEmail(), request.getDuration());
    BaseOutput<License> response =
        BaseOutput.<License>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(createdLicense)
            .build();
    return ResponseEntity.ok(response);
  }

  @PutMapping("/expand")
  public ResponseEntity<BaseOutput<License>> expand(@RequestBody LicenseExpandRequest request) {
    if (request == null
        || StringUtils.isBlank(request.getCode())
        || request.getDuration() == null) {
      BaseOutput<License> response =
          BaseOutput.<License>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    License expandedLicense = licenseService.expand(request.getCode(), request.getDuration());
    BaseOutput<License> response =
        BaseOutput.<License>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(expandedLicense)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete a license by id", description = "Return deleted license")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully delete license",
            content = {@Content(examples = @ExampleObject(value = "{\"message\": \"200 OK\"}"))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseOutput<String>> delete(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    licenseService.delete(id);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .status(ResponseStatus.SUCCESS)
            .data(HttpStatus.OK.toString())
            .build());
  }

  @PutMapping("/{licenseId}/user/{userId}")
  public ResponseEntity<BaseOutput<License>> assignLicenseByLicenseIdAndId(
      @PathVariable("userId") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long userId,
      @PathVariable("licenseId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long licenseId) {
    if (userId <= 0 || licenseId <= 0) {
      BaseOutput<License> response =
          BaseOutput.<License>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    licenseService.assignUserByUserIdAndId(licenseId, userId);
    return ResponseEntity.ok(
        BaseOutput.<License>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(licenseService.getById(licenseId))
            .build());
  }
}
