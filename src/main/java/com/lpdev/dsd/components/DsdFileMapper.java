package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.DsdFile;
import com.lpdev.dsd.models.entities.FileEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DsdFileMapper {

  public DsdFile toDTO(FileEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                DsdFile.builder()
                    .id(e.getId())
                    .fileType(e.getFileType())
                    .name(e.getName())
                    .path(e.getPath())
                    .createDate(e.getCreateDate())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }

  public FileEntity toEntity(DsdFile dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                FileEntity.builder()
                    .id(e.getId())
                    .fileType(e.getFileType())
                    .name(e.getName())
                    .path(e.getPath())
                    .createDate(e.getCreateDate())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }
}
