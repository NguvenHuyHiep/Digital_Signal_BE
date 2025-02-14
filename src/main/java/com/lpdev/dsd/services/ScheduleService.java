package com.lpdev.dsd.services;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.Schedule;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ScheduleService {
  Page<Schedule> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword);

  Schedule getById(@NonNull Long id);

  Schedule create(@NonNull Schedule schedule);

  Schedule update(@NonNull Schedule schedule);

  void delete(@NonNull Long id);

  Schedule assignPlaylist(@NonNull Long id, @NonNull List<Long> playlistIds);

  Schedule getWithPlaylistsById(@NonNull Long id);

  Schedule getWithPlaylistsWithFilesById(@NonNull Long id);

  DownloadFile downloadScheduleById(@NonNull Long id);

  void deleteByIds(List<Long> ids);

  void removeUserFromSchedule(@NonNull List<Long> userIds);

  Schedule updateStatus(@NonNull Long id, @NonNull Status status);
}
