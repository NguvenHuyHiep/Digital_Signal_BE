package com.lpdev.dsd.controllers.device_log;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.dtos.DeviceLog;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.DeviceLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/v1/admin/device-log")
@SecurityRequirement(name = "Authorization")
public class AdminDeviceLogController {
  private final DeviceLogService deviceLogService;

  @Operation(
      summary = "Get all DeviceLog with pagination",
      description = "Returns all DeviceLog with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  protected ResponseEntity<BaseOutput<List<DeviceLog>>> getByPaging(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {
    Page<DeviceLog> deviceLogPage =
        deviceLogService.getByPaging(page, size, sortBy, sortDirection, keyword);

    BaseOutput<List<DeviceLog>> response =
        BaseOutput.<List<DeviceLog>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(deviceLogPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(deviceLogPage.getTotalElements())
            .status(ResponseStatus.SUCCESS)
            .data(deviceLogPage.getContent())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get all DeviceLog in the device with pagination",
      description = "Returns all DeviceLog in the device with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/device/{id}")
  protected ResponseEntity<BaseOutput<List<DeviceLog>>> getDeviceLogsByDeviceId(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {
    Page<DeviceLog> deviceLogPage =
        deviceLogService.getDeviceLogsByDeviceId(page, size, sortBy, sortDirection, keyword, id);

    BaseOutput<List<DeviceLog>> response =
        BaseOutput.<List<DeviceLog>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(deviceLogPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(deviceLogPage.getTotalElements())
            .status(ResponseStatus.SUCCESS)
            .data(deviceLogPage.getContent())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get DeviceLog with pagination by id",
      description = "Returns DeviceLog by ID")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/{id}")
  protected ResponseEntity<BaseOutput<DeviceLog>> getById(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<DeviceLog> response =
          BaseOutput.<DeviceLog>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    DeviceLog deviceLog = deviceLogService.getById(id);
    BaseOutput<DeviceLog> response =
        BaseOutput.<DeviceLog>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(deviceLog)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Create DeviceLog", description = "Returns created DeviceLog")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @PostMapping
  protected ResponseEntity<BaseOutput<DeviceLog>> create(
      @RequestBody @NotNull(message = "error.request.body.invalid") DeviceLog deviceLog) {

    if (deviceLog == null) {
      BaseOutput<DeviceLog> response =
          BaseOutput.<DeviceLog>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    DeviceLog createdDeviceLog = deviceLogService.save(deviceLog);
    BaseOutput<DeviceLog> response =
        BaseOutput.<DeviceLog>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(createdDeviceLog)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update DeviceLog", description = "Returns updated DeviceLog")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @PutMapping("/{id}")
  protected ResponseEntity<BaseOutput<DeviceLog>> update(
      @PathVariable("id") @NotBlank(message = "error.id.invalid") Long id,
      @RequestBody @NotNull(message = "error.request.body.invalid") DeviceLog deviceLog) {
    if (id <= 0) {
      BaseOutput<DeviceLog> response =
          BaseOutput.<DeviceLog>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    deviceLog.setId(id);
    DeviceLog createdDeviceLog = deviceLogService.update(deviceLog);
    BaseOutput<DeviceLog> response =
        BaseOutput.<DeviceLog>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(createdDeviceLog)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete a Device Log by id", description = "Return deleted Device Log")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully delete Device Log",
            content = {@Content(examples = @ExampleObject(value = "{\"message\": \"200 OK\"}"))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @DeleteMapping("/{id}")
  protected ResponseEntity<BaseOutput<String>> delete(
      @PathVariable("id") @NotBlank(message = "error.id.invalid") Long id) {
    if (id <= 0) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    deviceLogService.delete(id);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
