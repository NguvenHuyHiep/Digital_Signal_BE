package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.Role;
import com.lpdev.dsd.models.entities.RoleEntity;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {
  public Role toDTO(RoleEntity entity) {
    return Optional.ofNullable(entity)
        .map(e -> Role.builder().id(e.getId()).name(e.getName()).type(e.getType()).build())
        .orElse(null);
  }

  public RoleEntity toEntity(Role dto) {
    return Optional.ofNullable(dto)
        .map(e -> RoleEntity.builder().id(e.getId()).name(e.getName()).type(e.getType()).build())
        .orElse(null);
  }
}
