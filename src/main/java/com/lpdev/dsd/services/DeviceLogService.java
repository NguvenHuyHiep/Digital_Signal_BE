package com.lpdev.dsd.services;

import com.lpdev.dsd.models.dtos.Device;
import com.lpdev.dsd.models.dtos.DeviceLog;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;

public interface DeviceLogService {
  Page<DeviceLog> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword);

  Page<DeviceLog> getDeviceLogsByDeviceId(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      @NonNull Long deviceId);

  Page<DeviceLog> getLatestByDeviceAndPagingOrderByDate(@NonNull Device device);

  DeviceLog getById(Long id);

  DeviceLog save(DeviceLog deviceLog);

  DeviceLog update(DeviceLog deviceLog);

  void delete(Long id);
}
