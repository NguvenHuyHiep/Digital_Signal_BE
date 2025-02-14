package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.enums.RoleType;
import com.lpdev.dsd.components.UserMapper;
import com.lpdev.dsd.components.UserRoleMapMapper;
import com.lpdev.dsd.models.dtos.User;
import com.lpdev.dsd.models.dtos.UserRoleMap;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.models.entities.UserRoleMapEntity;
import com.lpdev.dsd.repositories.RoleRepository;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.repositories.UserRoleMapRepository;
import com.lpdev.dsd.services.UserRoleMapService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class UserRoleMapServiceImpl implements UserRoleMapService {

  @Autowired private UserRoleMapRepository userRoleMapRepository;
  @Autowired private UserRoleMapMapper userRoleMapMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private UserMapper userMapper;

  @Override
  public List<UserRoleMap> findByUser(User user) {
    return Optional.ofNullable(user)
        .map(dto -> userMapper.toEntity(dto))
        .map(
            uid ->
                userRoleMapRepository.findByUser(uid).stream()
                    .map(ur -> userRoleMapMapper.toDTO(ur))
                    .toList())
        .orElse(new ArrayList<>());
  }

  @Override
  public void setUserRoleForUser(Long userId) {
    setRoleForUserByRoleList(userId, List.of(RoleType.USER));
  }

  @Override
  public void setAdminRoleForUser(Long userId) {
    setRoleForUserByRoleList(userId, List.of(RoleType.USER, RoleType.ADMIN));
  }

  private void setRoleForUserByRoleList(Long userId, List<RoleType> roleTypes) {
    if (roleTypes == null || roleTypes.isEmpty()) {
      log.info("Role is null or empty");
      return;
    }

    if (userId == null) {
      log.info("userId is null");
      return;
    }

    UserEntity userEntity = userRepository.findById(userId).orElse(null);
    if (userEntity == null) {
      log.info("user is null");
      return;
    }

    userRoleMapRepository.deleteByUser(userEntity);
    List<UserRoleMapEntity> userRoleMapEntities =
        roleTypes.stream()
            .map(
                r ->
                    userRoleMapRepository.save(
                        UserRoleMapEntity.builder()
                            .user(userEntity)
                            .role(roleRepository.findByType(r).orElse(null))
                            .build()))
            .collect(Collectors.toList());
    log.info("set Roles with size: {}", userRoleMapEntities.size());
  }
}
