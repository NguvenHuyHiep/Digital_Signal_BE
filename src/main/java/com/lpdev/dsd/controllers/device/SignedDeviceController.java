package com.lpdev.dsd.controllers.device;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.DeviceStatus;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.dtos.Device;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/signed/device")
@Tag(name = "Signed-Device API")
public class SignedDeviceController {

  private final DeviceService deviceService;

  @Operation(
      summary = "Get all devices with pagination",
      description = "Returns all devices with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  protected ResponseEntity<BaseOutput<List<Device>>> getByPaging(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword,
      @RequestParam(required = false, defaultValue = "") DeviceStatus status) {
    Page<Device> devicePage =
        deviceService.getByPaging(page, size, sortBy, sortDirection, keyword, status);
    BaseOutput<List<Device>> response =
        BaseOutput.<List<Device>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(devicePage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(devicePage.getTotalElements())
            .status(ResponseStatus.SUCCESS)
            .data(devicePage.getContent())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get all devices in the device group with pagination",
      description = "Returns all devices in the device group with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/device-group/{id}")
  protected ResponseEntity<BaseOutput<List<Device>>> getDevicesByDeviceGroupId(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword,
      @RequestParam(required = false, defaultValue = "UNDEFINED") DeviceStatus status) {
    if (DeviceStatus.UNDEFINED.equals(status)) {
      status = null;
    }
    Page<Device> devicePage =
        deviceService.getDevicesByDeviceGroupId(
            page, size, sortBy, sortDirection, keyword, status, id);
    BaseOutput<List<Device>> response =
        BaseOutput.<List<Device>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(devicePage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(devicePage.getTotalElements())
            .data(devicePage.getContent())
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get device logs in the device by id",
      description = "Returns  device logs from device")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}/logs")
  public ResponseEntity<BaseOutput<Device>> getByIdWithLogs(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestParam(required = false, defaultValue = "") DeviceStatus status,
      @RequestParam(required = false) Date startDate,
      @RequestParam(required = false) Date endDate) {
    if (id <= 0) {
      BaseOutput<Device> response =
              BaseOutput.<Device>builder()
                      .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
                      .status(ResponseStatus.FAILED)
                      .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    if (DeviceStatus.UNDEFINED.equals(status)) {
      status = null;
    }
    Device device = deviceService.getWithLogsById(id, status, startDate, endDate);
    BaseOutput<Device> response =
        BaseOutput.<Device>builder()
            .message(HttpStatus.OK.toString())
            .data(device)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get a device by id", description = "Returns a device as per the id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}")
  protected ResponseEntity<BaseOutput<Device>> getById(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<Device> response =
          BaseOutput.<Device>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    Device device = deviceService.getById(id);

    BaseOutput<Device> response =
        BaseOutput.<Device>builder()
            .message(HttpStatus.OK.toString())
            .data(device)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Create a new device", description = "Create a new device")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping
  protected ResponseEntity<BaseOutput<Device>> create(
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) Device device) {

    if (device == null) {
      BaseOutput<Device> response =
          BaseOutput.<Device>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Device createdDevice = deviceService.create(device);
    BaseOutput<Device> response =
        BaseOutput.<Device>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(createdDevice)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update a device by id", description = "Return updated device")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update new device"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}")
  protected ResponseEntity<BaseOutput<Device>> update(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Device device) {
    if (id <= 0) {
      BaseOutput<Device> response =
          BaseOutput.<Device>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    device.setId(id);
    Device updateDevice = deviceService.update(device);
    BaseOutput<Device> response =
        BaseOutput.<Device>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(updateDevice)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Update status of device",
      description = "Update status of device to ONLINE OR OFFLINE")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update the device status"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{deviceId}/update-status/{status}")
  public ResponseEntity<BaseOutput<Device>> updateStatus(
      @PathVariable("deviceId") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long deviceId,
      @PathVariable("status") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          String status) {
    if (status == null || StringUtils.isBlank(status)) {
      BaseOutput<Device> response =
          BaseOutput.<Device>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Device updatedDevice = deviceService.updateStatus(deviceId, DeviceStatus.parse(status));
    return ResponseEntity.ok(
        BaseOutput.<Device>builder()
            .status(ResponseStatus.SUCCESS)
            .message(HttpStatus.OK.toString())
            .data(updatedDevice)
            .build());
  }

  @Operation(
      summary = "Assign device to device group",
      description = "Return Assigned device to device group by id and group id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully delete device"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{deviceId}/device-group/{groupId}")
  public ResponseEntity<BaseOutput<Device>> assignToDeviceGroupByIdAndGroupId(
      @PathVariable("deviceId") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long deviceId,
      @PathVariable("groupId") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long groupId) {
    if (deviceId == null || groupId == null) {
      BaseOutput<Device> response =
          BaseOutput.<Device>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    deviceService.assignToDeviceGroupByDeviceGroupIdAndIds(groupId, List.of(deviceId));
    return ResponseEntity.ok(
        BaseOutput.<Device>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(deviceService.getById(deviceId))
            .build());
  }

  @Operation(
      summary = "remove devices from device group",
      description = "remove devices from a device group")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully remove devices from device group"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/remove/device-group/{deviceGroupId}")
  protected ResponseEntity<BaseOutput<String>> removeDevicesFromDeviceGroup(
      @PathVariable("deviceGroupId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long deviceGroupId,
      @RequestBody @NotBlank List<Long> ids) {
    if (ids == null || deviceGroupId == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    deviceService.removeDevicesFromDeviceGroup(ids, deviceGroupId);

    BaseOutput<String> response =
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }
}
