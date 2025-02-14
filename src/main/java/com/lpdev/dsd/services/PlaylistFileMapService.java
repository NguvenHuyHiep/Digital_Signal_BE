package com.lpdev.dsd.services;

import com.lpdev.dsd.models.dtos.Playlist;
import java.util.List;
import lombok.NonNull;

public interface PlaylistFileMapService {
  Playlist assignByPlaylistIdsAndFileIds(
      @NonNull List<Long> playlistIds, @NonNull List<Long> fileIds);

  Playlist assignByPlaylistIdsCategoryIds(
      @NonNull List<Long> playlistIds, @NonNull List<Long> categoryIds);

  void removeByPlaylistIdsAndFileIds(@NonNull List<Long> playlistIds, @NonNull List<Long> fileIds);
}
