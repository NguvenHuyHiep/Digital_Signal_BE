package com.lpdev.dsd.models.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.models.dtos.Playlist;
import java.io.Serial;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class DashBoardResponse implements Serializable {
  @Serial private static final long serialVersionUID = 3927746803213997225L;
  Long totalOfflineDevices;
  Long totalOnlineDevices;
  Long totalDeviceGroups;
  Long totalPlaylists;
  transient Playlist lastPlaylist;
}
