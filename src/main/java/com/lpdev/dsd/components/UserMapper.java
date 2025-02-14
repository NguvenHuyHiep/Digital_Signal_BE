package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.User;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.models.entities.UserRoleMapEntity;
import com.lpdev.dsd.services.RoleService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  @Autowired @Lazy private RoleService roleService;
  @Autowired @Lazy private PasswordEncoder passwordEncoder;
  @Autowired @Lazy private UserRoleMapMapper userRoleMapMapper;
  @Autowired @Lazy private RoleMapper roleMapper;
  @Autowired @Lazy private LicenseMapper licenseMapper;

  public User toDTO(UserEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDto).orElse(null);
  }

  public User toDTOWithLicense(UserEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            u ->
                u.toBuilder()
                    .license(
                        entity.getLicense() != null
                            ? licenseMapper.toDTO(entity.getLicense())
                            : null)
                    .build())
        .orElse(null);
  }

  public UserEntity toEntity(User dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                UserEntity.builder()
                    .id(e.getId())
                    .userName(e.getUserName())
                    .email(e.getEmail())
                    .password(
                        e.getPassword() != null ? passwordEncoder.encode(e.getPassword()) : null)
                    .phone(e.getPhone())
                    .firstName(e.getFirstName())
                    .lastName(e.getLastName())
                    .build())
        .orElse(null);
  }

  private User convertToDto(UserEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                User.builder()
                    .id(e.getId())
                    .userName(e.getUserName())
                    .email(e.getEmail())
                    .password(e.getPassword())
                    .phone(e.getPhone())
                    .firstName(e.getFirstName())
                    .lastName(e.getLastName())
                    .roles(
                        e.getUserRoleMap() != null
                            ? e.getUserRoleMap().stream()
                                .map(UserRoleMapEntity::getRole)
                                .map(urm -> roleMapper.toDTO(urm))
                                .toList()
                            : null)
                    .build())
        .orElse(null);
  }
}
