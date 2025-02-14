package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.DeviceGroup;
import com.lpdev.dsd.models.entities.DeviceGroupEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceGroupMapper {
  @Lazy private final DeviceMapper deviceMapper;
  @Lazy private final PlaylistMapper playlistMapper;

  public DeviceGroup toDTO(DeviceGroupEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDto).orElse(null);
  }

  public DeviceGroup toDTOWithDevices(DeviceGroupEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            dg ->
                dg.toBuilder()
                    .devices(
                        entity.getDevices() != null
                            ? entity.getDevices().stream().map(deviceMapper::toDTO).toList()
                            : null)
                    .build())
        .orElse(null);
  }

  public DeviceGroup toDTOWithPlaylist(DeviceGroupEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            dg ->
                dg.toBuilder()
                    .playlist(
                        entity.getPlaylist() != null
                            ? playlistMapper.toDTO(entity.getPlaylist())
                            : null)
                    .build())
        .orElse(null);
  }

  public DeviceGroup toDTOWithPlaylistWithFiles(DeviceGroupEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            dg ->
                dg.toBuilder()
                    .playlist(
                        entity.getPlaylist() != null
                            ? playlistMapper.toDTOWithFiles(entity.getPlaylist())
                            : null)
                    .build())
        .orElse(null);
  }

  public DeviceGroupEntity toEntity(DeviceGroup dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                DeviceGroupEntity.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }

  private DeviceGroup convertToDto(DeviceGroupEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                DeviceGroup.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }
}
