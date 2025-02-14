package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.DsdFile;
import com.lpdev.dsd.models.dtos.Playlist;
import com.lpdev.dsd.models.entities.PlaylistEntity;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PlaylistMapper {
  @Lazy @Autowired DsdFileMapper dsdFileMapper;
  @Lazy @Autowired PlaylistFileMapMapper playlistFileMapMapper;
  @Lazy @Autowired DeviceGroupMapper deviceGroupMapper;

  public Playlist toDTO(PlaylistEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDto).orElse(null);
  }

  public Playlist toDTOWithFiles(PlaylistEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            p ->
                p.toBuilder()
                    .files(
                        entity.getPlayListFileMap() != null
                            ? entity.getPlayListFileMap().stream()
                                .map(
                                    pfm -> {
                                      DsdFile f = dsdFileMapper.toDTO(pfm.getFile());
                                      f.setAssignDate(pfm.getAssignDate());
                                      return f;
                                    })
                                .toList()
                            : null)
                    .build())
        .orElse(null);
  }

  public Playlist toDTOWithDeviceGroups(PlaylistEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            p ->
                p.toBuilder()
                    .deviceGroups(
                        entity.getDeviceGroups() != null
                            ? entity.getDeviceGroups().stream()
                                .map(deviceGroupMapper::toDTO)
                                .toList()
                            : null)
                    .build())
        .orElse(null);
  }

  public PlaylistEntity toEntity(Playlist dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                PlaylistEntity.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .startTime(e.getStartTime())
                    .endTime(e.getEndTime())
                    .isLoop(e.getIsLoop())
                    .status(e.getStatus())
                    .lastUpdateDate(e.getLastUpdateDate())
                    .build())
        .orElse(null);
  }

  private Playlist convertToDto(PlaylistEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                Playlist.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .startTime(e.getStartTime())
                    .endTime(e.getEndTime())
                    .isLoop(e.getIsLoop())
                    .status(e.getStatus())
                    .fileOrder(e.getFileOrder())
                    .lastUpdateDate(e.getLastUpdateDate())
                    .build())
        .orElse(null);
  }
}
