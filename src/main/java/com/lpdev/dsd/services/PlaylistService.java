package com.lpdev.dsd.services;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.dtos.Playlist;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PlaylistService {
  Playlist getById(@NonNull Long id); // TODO list playlist containt user info, the same as file

  Page<Playlist> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword);

  Playlist create(@NonNull Playlist playlist);

  Playlist update(@NonNull Playlist playlist);

  void delete(@NonNull Long id);

  Playlist getWithFilesById(@NonNull Long id);

  void assignToScheduleByScheduleIdAndIds(@NonNull Long scheduleId, @NonNull List<Long> ids);

  void deleteByIds(List<Long> ids);

  void removeScheduleFromPlaylist(@NonNull List<Long> scheduleIds);

  void removeUserFromPlaylist(@NonNull List<Long> userIds);

  Playlist updateStatus(@NonNull Long id, @NonNull Status status);

  Playlist getWithDeviceGroupsById(@NonNull Long id);

  Playlist assignDeviceGroups(@NonNull Long id, @NonNull List<Long> deviceGroupIds);

  void removeDeviceGroups(@NonNull Long id, @NonNull List<Long> deviceGroupIds);

  void updateLastUpdate(@NonNull List<Long> ids);

  Playlist moveFileInPlaylist(@NonNull Long playlistId, @NonNull Long fileId, boolean idUp);

  Playlist updateFileOrder(@NonNull Long id);
}
