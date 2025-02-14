package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.Schedule;
import com.lpdev.dsd.models.entities.ScheduleEntity;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {
  @Lazy @Autowired PlaylistMapper playlistMapper;

  public Schedule toDTO(ScheduleEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDto).orElse(null);
  }

  public Schedule toDTOWithPlayLists(ScheduleEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            s ->
                s.toBuilder()
                    .playlists(
                        entity.getPlaylists() != null
                            ? entity.getPlaylists().stream().map(playlistMapper::toDTO).toList()
                            : Collections.emptyList())
                    .build())
        .orElse(null);
  }

  public Schedule toDTOWithPlayListsWithFiles(ScheduleEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            s ->
                s.toBuilder()
                    .playlists(
                        entity.getPlaylists() != null
                            ? entity.getPlaylists().stream()
                                .map(playlistMapper::toDTOWithFiles)
                                .toList()
                            : Collections.emptyList())
                    .build())
        .orElse(null);
  }

  public ScheduleEntity toEntity(Schedule dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                ScheduleEntity.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .days(e.getDays())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }

  private Schedule convertToDto(ScheduleEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                Schedule.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .days(e.getDays())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }
}
