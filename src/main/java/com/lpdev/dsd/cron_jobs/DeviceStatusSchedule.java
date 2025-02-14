package com.lpdev.dsd.cron_jobs;

import com.lpdev.dsd.commons.enums.DeviceStatus;
import com.lpdev.dsd.models.dtos.Device;
import com.lpdev.dsd.models.dtos.DeviceLog;
import com.lpdev.dsd.services.DeviceLogService;
import com.lpdev.dsd.services.DeviceService;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeviceStatusSchedule {
  private static final long EXPIRED_IN_MILLISECOND = 1800000L;
  private final DeviceService deviceService;
  private final DeviceLogService deviceLogService;

  @Scheduled(fixedRate = EXPIRED_IN_MILLISECOND)
  public void deviceStatusTask() {
    log.info("===== START DEVICE CHECKING SCHEDULE TASK =====");
    List<Device> devices = deviceService.getAllByStatusWithDeviceLogs(DeviceStatus.ONLINE);
    if (devices == null || devices.isEmpty()) {
      log.info("list devices is null or is empty, stop schedule task");
      log.info("===== END DEVICE CHECKING SCHEDULE TASK =====");
      return;
    }

    log.info("devices to check: {}", devices.size());

    Date dateToCheck = new Date(System.currentTimeMillis() - EXPIRED_IN_MILLISECOND);
    for (Device device : devices) {
      try {
        if (device == null) {
          log.info("device is null, continue");
          continue;
        }
        log.info("checking log of device: {}", device.getCode());
        Page<DeviceLog> latestDeviceLogPage =
            deviceLogService.getLatestByDeviceAndPagingOrderByDate(device);
        if (latestDeviceLogPage == null || latestDeviceLogPage.isEmpty()) {
          log.info("device doesn't have logs, continue");
          continue;
        }
        DeviceLog latestDeviceLog = latestDeviceLogPage.getContent().get(0);

        if (latestDeviceLog == null
            || latestDeviceLog.getDate() == null
            || latestDeviceLog.getDate().after(dateToCheck)) {
          log.info(
              "latestDeviceLog or it's date or it's status is invalid or it's date is after time to check (30m), continue");
          continue;
        }

        Device updatedDevice = deviceService.updateStatus(device.getCode(), DeviceStatus.OFFLINE);
        log.info("Set OFFLINE for device: {}", device.getCode());
        if (updatedDevice != null) {
          log.info("updated status of device: {} to OFFLINE", updatedDevice.getCode());
        } else {
          log.info("cannot update device");
        }
      } catch (Exception e) {
        log.error("ERROR update status of device by logged date, continue");
      }
    }
    log.info("===== END DEVICE CHECKING SCHEDULE TASK =====");
  }
}
