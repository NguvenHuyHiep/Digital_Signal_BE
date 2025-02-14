package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.UserRoleMap;
import com.lpdev.dsd.models.entities.UserRoleMapEntity;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapMapper {

  @Lazy @Autowired private UserMapper userMapper;
  @Lazy @Autowired private RoleMapper roleMapper;

  public UserRoleMap toDTO(UserRoleMapEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                UserRoleMap.builder()
                    .id(e.getId())
                    .user(userMapper.toDTO(e.getUser()))
                    .role(roleMapper.toDTO(e.getRole()))
                    .build())
        .orElse(null);
  }

  public UserRoleMapEntity toEntity(UserRoleMap dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                UserRoleMapEntity.builder()
                    .id(e.getId())
                    .user(userMapper.toEntity(e.getUser()))
                    .role(roleMapper.toEntity(e.getRole()))
                    .build())
        .orElse(null);
  }
}
