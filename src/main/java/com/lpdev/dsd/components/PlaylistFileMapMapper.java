package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.PlaylistFileMap;
import com.lpdev.dsd.models.entities.PlaylistFileMapEntity;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class PlaylistFileMapMapper {

  @Lazy @Autowired private PlaylistMapper playlistMapper;
  @Lazy @Autowired private DsdFileMapper dsdFileMapper;

  public PlaylistFileMap toDTO(PlaylistFileMapEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                PlaylistFileMap.builder()
                    .id(e.getId())
                    .playlist(playlistMapper.toDTO(e.getPlaylist()))
                    .file(dsdFileMapper.toDTO(e.getFile()))
                    .build())
        .orElse(null);
  }

  public PlaylistFileMapEntity toEntity(PlaylistFileMap dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                PlaylistFileMapEntity.builder()
                    .id(e.getId())
                    .playlist(playlistMapper.toEntity(e.getPlaylist()))
                    .file(dsdFileMapper.toEntity(e.getFile()))
                    .build())
        .orElse(null);
  }
}
