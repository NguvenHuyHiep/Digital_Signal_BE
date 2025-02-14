package com.lpdev.dsd.services;

import com.lpdev.dsd.commons.enums.DeviceStatus;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.Device;
import com.lpdev.dsd.models.dtos.DeviceGroup;
import com.lpdev.dsd.models.entities.DeviceLogEntity;
import com.lpdev.dsd.models.responses.DeviceUpdateStatus;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.data.domain.Page;

public interface DeviceService {
  Page<Device> getByPaging(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      DeviceStatus status);

  Page<Device> getDevicesByDeviceGroupId(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      DeviceStatus status,
      @NonNull Long deviceGroupId);

  @Deprecated
  List<Device> getAllByStatusWithDeviceLogs(@NonNull DeviceStatus status); // TODO for test only

  Device getById(Long id);

  Device create(Device device);

  Device update(@NonNull Device device);

  void delete(@NonNull Long id);

  Device updateStatus(@NonNull Long id, @NonNull DeviceStatus status);

  void deleteByIds(List<Long> ids);

  void assignToDeviceGroupByDeviceGroupIdAndIds(
      @NonNull Long deviceGroupId, @NonNull List<Long> ids);

  Device updateStatus(@NonNull String code, @NonNull DeviceStatus status);

  DeviceUpdateStatus updateStatusByLicenseAndCode(@NonNull String license, @NonNull String code);

  Device getWithLogsById(@NonNull Long id, DeviceStatus status, Date startDate, Date endDate);

  List<Device> getDeviceLogsByDeviceGroupId(
      @NonNull Long deviceGroupId, DeviceStatus status, Date startDate, Date endDate);

  Device register(@NonNull String license, @NonNull Device device);

  void removeDeviceGroupFromDevices(@NonNull List<Long> deviceGroupIds);

  void removeDevicesFromDeviceGroup(@NonNull List<Long> ids, @NonNull Long deviceGroupId);

  void removeUserFromDevice(@NonNull List<Long> userIds);

  DeviceGroup getDeviceGroupByDeviceCode(@NonNull String code);

  DownloadFile downloadDeviceByCode(@NonNull String code, Date deviceLastUpdateDate);

  DownloadFile exportDeviceStatus(@NonNull Long deviceId, Date startDate, Date endDate)
      throws IOException;

  List<DeviceLogEntity> fetchDeviceLogs(Long deviceId, Date startDate, Date endDate);

  void exportData(Sheet sheet, List<DeviceLogEntity> deviceLogEntities);
}
