package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.Category;
import com.lpdev.dsd.models.entities.CategoryEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

  @Lazy private final DsdFileMapper dsdFileMapper;

  public Category toDTO(CategoryEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDTO).orElse(null);
  }

  public Category toDTOWithFiles(CategoryEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDTO)
        .map(
            p ->
                p.toBuilder()
                    .files(
                        entity.getFiles() != null
                            ? entity.getFiles().stream().map(dsdFileMapper::toDTO).toList()
                            : null)
                    .build())
        .orElse(null);
  }

  public CategoryEntity toEntity(Category dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                CategoryEntity.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }

  private Category convertToDTO(CategoryEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                Category.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }
}
