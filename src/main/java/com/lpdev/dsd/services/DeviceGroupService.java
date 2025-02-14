package com.lpdev.dsd.services;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.DeviceGroup;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;

public interface DeviceGroupService {
  Page<DeviceGroup> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword, Status status);

  Page<DeviceGroup> getDeviceGroupsByPlaylistId(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      @NonNull Long playlistId);

  DeviceGroup getById(@NonNull Long id);

  DeviceGroup save(@NonNull DeviceGroup deviceGroup);

  DeviceGroup update(@NonNull DeviceGroup deviceGroup);

  void delete(@NonNull Long id);

  DeviceGroup updateStatus(@NonNull Long id, @NonNull Status status);

  DeviceGroup assignDevices(@NonNull Long id, @NonNull List<Long> dsdGroupIds);

  DeviceGroup getWithDeviceById(@NonNull Long id);

  void deleteByIds(@NonNull List<Long> ids);

  void removeUserFromDeviceGroup(@NonNull List<Long> userIds);

  void assignToPlaylistByPlaylistIdAndIds(@NonNull Long playlistId, @NonNull List<Long> ids);

  void removeDeviceGroupsFromPlaylist(@NonNull List<Long> deviceGroupIds, @NonNull Long playlistId);

  void removeDevices(@NonNull Long id, @NonNull List<Long> deviceIds);

  DownloadFile exportDeviceStatus(@NonNull Long deviceGroupId, Date startDate, Date endDate)
      throws IOException;
}
