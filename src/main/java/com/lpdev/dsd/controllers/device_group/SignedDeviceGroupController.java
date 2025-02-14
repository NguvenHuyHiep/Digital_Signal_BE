package com.lpdev.dsd.controllers.device_group;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.dtos.DeviceGroup;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.DeviceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/v1/signed/device-group")
@Tag(name = "Signed-Device_Group API")
public class SignedDeviceGroupController {

  private final DeviceGroupService deviceGroupService;

  @Operation(
      summary = "Get all device-groups with pagination",
      description = "Returns all device-groups with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  protected ResponseEntity<BaseOutput<List<DeviceGroup>>> getByPaging(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword,
      @RequestParam(required = false, defaultValue = "ACTIVE") Status status) {

    Page<DeviceGroup> deviceGroupPage =
        deviceGroupService.getByPaging(page, size, sortBy, sortDirection, keyword, status);
    BaseOutput<List<DeviceGroup>> response =
        BaseOutput.<List<DeviceGroup>>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .totalPages(deviceGroupPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(deviceGroupPage.getTotalElements())
            .data(deviceGroupPage.getContent())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get all device groups in the playlist group with pagination",
      description = "Returns all device groups in the playlist in the device group with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/playlist/{id}")
  protected ResponseEntity<BaseOutput<List<DeviceGroup>>> getDeviceGroupsByPlaylistId(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {

    Page<DeviceGroup> deviceGroupPage =
        deviceGroupService.getDeviceGroupsByPlaylistId(
            page, size, sortBy, sortDirection, keyword, id);
    BaseOutput<List<DeviceGroup>> response =
        BaseOutput.<List<DeviceGroup>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(deviceGroupPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(deviceGroupPage.getTotalElements())
            .status(ResponseStatus.SUCCESS)
            .data(deviceGroupPage.getContent())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get a device-group by id",
      description = "Returns a device-group as per the id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}")
  protected ResponseEntity<BaseOutput<DeviceGroup>> getById(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<DeviceGroup> response =
          BaseOutput.<DeviceGroup>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    DeviceGroup deviceGroup = deviceGroupService.getById(id);

    BaseOutput<DeviceGroup> response =
        BaseOutput.<DeviceGroup>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(deviceGroup)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Create a new device-group", description = "Create a new device-group")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping
  protected ResponseEntity<BaseOutput<DeviceGroup>> create(
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          DeviceGroup deviceGroup) {

    if (deviceGroup == null) {
      BaseOutput<DeviceGroup> response =
          BaseOutput.<DeviceGroup>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    DeviceGroup createdDeviceGroup = deviceGroupService.save(deviceGroup);
    BaseOutput<DeviceGroup> response =
        BaseOutput.<DeviceGroup>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(createdDeviceGroup)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update a device-group by id", description = "Return updated device-group")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update new device-group"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}")
  protected ResponseEntity<BaseOutput<DeviceGroup>> update(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          DeviceGroup deviceGroup) {
    if (id <= 0) {
      BaseOutput<DeviceGroup> response =
          BaseOutput.<DeviceGroup>builder()
              .errors(
                  List.of(
                      DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE,
                      DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    deviceGroup.setId(id);
    DeviceGroup updateDeviceGroup = deviceGroupService.update(deviceGroup);
    BaseOutput<DeviceGroup> response =
        BaseOutput.<DeviceGroup>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(updateDeviceGroup)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Update status of device group",
      description = "Update status of device group to ONLINE OR OFFLINE")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully update the device group status"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{deviceGroupId}/update-status/{status}")
  public ResponseEntity<BaseOutput<DeviceGroup>> updateStatus(
      @PathVariable("deviceGroupId")
          @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long deviceGroupId,
      @PathVariable("status") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          String status) {
    if (status == null || StringUtils.isBlank(status)) {
      BaseOutput<DeviceGroup> response =
          BaseOutput.<DeviceGroup>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    DeviceGroup updatedDeviceGroup =
        deviceGroupService.updateStatus(deviceGroupId, Status.parse(status));
    return ResponseEntity.ok(
        BaseOutput.<DeviceGroup>builder()
            .status(ResponseStatus.SUCCESS)
            .message(HttpStatus.OK.toString())
            .data(updatedDeviceGroup)
            .build());
  }

  @Operation(
      summary = "Assign devices to device group",
      description = "Return Assigned devices to device group")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully delete device"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}/devices")
  public ResponseEntity<BaseOutput<DeviceGroup>> assignDevices(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) List<Long> groupIds) {
    if (id <= 0 || groupIds == null || groupIds.isEmpty()) {
      BaseOutput<DeviceGroup> response =
          BaseOutput.<DeviceGroup>builder()
              .errors(
                  List.of(
                      DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE,
                      DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    DeviceGroup assignedDeviceGroup = deviceGroupService.assignDevices(id, groupIds);
    BaseOutput<DeviceGroup> response =
        BaseOutput.<DeviceGroup>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(assignedDeviceGroup)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Assign device group to playlist",
      description = "Return Assigned device group to playlist")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully assign device group"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{deviceGroupId}/playlist/{playlistId}")
  public ResponseEntity<BaseOutput<DeviceGroup>> assignToPlaylistByPlaylistIdAndIds(
      @PathVariable("deviceGroupId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long deviceGroupId,
      @PathVariable("playlistId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long playlistId) {
    if (deviceGroupId == null || playlistId == null) {
      BaseOutput<DeviceGroup> response =
          BaseOutput.<DeviceGroup>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    deviceGroupService.assignToPlaylistByPlaylistIdAndIds(playlistId, List.of(deviceGroupId));
    return ResponseEntity.ok(
        BaseOutput.<DeviceGroup>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(deviceGroupService.getById(deviceGroupId))
            .build());
  }

  @Operation(
      summary = "Remove device groups from a playlist ",
      description = "Return removed device group")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully removed device group"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/remove/playlist/{playlistId}")
  public ResponseEntity<BaseOutput<String>> removeDeviceGroupsFromPlaylist(
      @RequestBody @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          List<Long> deviceGroupIds,
      @PathVariable("playlistId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long playlistId) {
    if (deviceGroupIds == null || playlistId == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .message(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    deviceGroupService.removeDeviceGroupsFromPlaylist(deviceGroupIds, playlistId);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @Operation(
      summary = "Remove devices from a device group ",
      description = "Return removed devices")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully removed devices"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/remove/{id}/devices")
  public ResponseEntity<BaseOutput<String>> removeDevices(
      @RequestBody @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) List<Long> deviceIds,
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (deviceIds == null || id == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .message(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    deviceGroupService.removeDevices(id, deviceIds);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
