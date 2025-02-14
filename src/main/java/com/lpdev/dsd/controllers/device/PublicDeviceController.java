package com.lpdev.dsd.controllers.device;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.commons.utils.DsdDateUtils;
import com.lpdev.dsd.commons.utils.DsdUtils;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.Device;
import com.lpdev.dsd.models.requests.DeviceUpdateStatusRequest;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.models.responses.DeviceUpdateStatus;
import com.lpdev.dsd.services.DeviceService;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/public/device")
public class PublicDeviceController {

  private final DeviceService deviceService;

  @PostMapping("/{license}/register")
  public ResponseEntity<BaseOutput<Device>> register(
      @PathVariable("license") String license,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) Device device) {
    if (device == null || StringUtils.isAllBlank(license, device.getCode())) {
      BaseOutput<Device> response =
          BaseOutput.<Device>builder()
              .errors(
                  List.of(
                      DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE,
                      DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Device registeredDevice = deviceService.register(license, device);
    return ResponseEntity.ok(
        BaseOutput.<Device>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(registeredDevice)
            .build());
  }

  @PostMapping("/update-status")
  public ResponseEntity<BaseOutput<DeviceUpdateStatus>> updateStatus(
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          DeviceUpdateStatusRequest request) {
    if (request == null || StringUtils.isAllBlank(request.getCode())) {
      BaseOutput<DeviceUpdateStatus> response =
          BaseOutput.<DeviceUpdateStatus>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    DeviceUpdateStatus device =
        deviceService.updateStatusByLicenseAndCode(request.getLicense(), request.getCode());

    return ResponseEntity.ok(
        BaseOutput.<DeviceUpdateStatus>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(device)
            .build());
  }

  @GetMapping("/download")
  public ResponseEntity<Resource> download(
      @RequestParam("code") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          String code,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Date deviceLastUpdateDate) {
    // date example: 2024-02-27T10:10:42.477Z
    if (StringUtils.isBlank(code)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    DownloadFile downloadFile = deviceService.downloadDeviceByCode(code, deviceLastUpdateDate);
    ByteArrayResource resource = downloadFile.getResource();
    return ResponseEntity.ok()
        .headers(
            DsdUtils.getHeadersForDownload(
                DsdDateUtils.parse(new Date()) + "-" + downloadFile.getName()))
        .contentLength(resource.contentLength())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }
}
