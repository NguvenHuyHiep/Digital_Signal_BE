package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.USER;
import com.lpdev.dsd.commons.enums.RoleType;
import com.lpdev.dsd.components.RoleMapper;
import com.lpdev.dsd.components.UserMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.dtos.Role;
import com.lpdev.dsd.models.dtos.User;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.models.entities.UserRoleMapEntity;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.repositories.UserRoleMapRepository;
import com.lpdev.dsd.services.*;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {

  @Lazy @Autowired UserRepository userRepository;
  @Lazy @Autowired UserMapper userMapper;

  @Lazy @Autowired RoleMapper roleMapper;

  @Lazy @Autowired UserRoleMapService userRoleMapService;
  @Lazy @Autowired UserRoleMapRepository userRoleMapRepository;

  @Lazy @Autowired DeviceService deviceService;
  @Lazy @Autowired DeviceGroupService deviceGroupService;
  @Lazy @Autowired DsdFileService dsdFileService;
  @Lazy @Autowired PlaylistService playlistService;
  @Lazy @Autowired ScheduleService scheduleService;
  @Lazy @Autowired LicenseService licenseService;

  @Override
  public String getAuthenticatedUserEmail() throws UsernameNotFoundException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      if (userDetails != null) {
        String loggedEmail = userDetails.getUsername();
        log.info("LOGGED user: {}", loggedEmail);
        return loggedEmail;
      } else {
        throw new UsernameNotFoundException(DsdConstant.ERROR.AUTH.NOT_FOUND);
      }
    }
    throw new UsernameNotFoundException(DsdConstant.ERROR.AUTH.NOT_FOUND);
  }

  @Override
  public Page<User> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword) {
    /** TODO Only allow admin user to call this func */
    if (!isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    return userRepository.findByKeyword(keyword, pageable).map(userMapper::toDTOWithLicense);
  }

  @Override
  public User getById(Long id) {
    /** TODO Only allow admin user to call this func */
    if (!isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return Optional.ofNullable(id)
        .flatMap(e -> userRepository.findById(id))
        .map(userMapper::toDTOWithLicense)
        .orElse(null);
  }

  @Override
  public User create(User user) {
    /** TODO Only allow admin user to call this func */
    if (!isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    if (user == null
        || userRepository.existsByEmail(user.getEmail())
        || userRepository.existsByUserName(user.getUserName())) {
      throw new DsdCommonException(USER.EXIST);
    }
    return Optional.of(user)
        .map(userMapper::toEntity)
        .map(userRepository::save)
        .map(
            e -> {
              userRoleMapService.setUserRoleForUser(e.getId());
              return e;
            })
        .map(userMapper::toDTO)
        .orElse(null);
  }

  @Override
  public User update(User user) {
    /** TODO Only allow admin user to update other user Normal user can update their account only */
    User toUpdateUser = user.toBuilder().build();
    if (!isAdmin()) {
      String loggedEmail = this.getAuthenticatedUserEmail();
      toUpdateUser = this.getByEmail(loggedEmail);
    }

    if (!userRepository.existsById(toUpdateUser.getId())) {
      throw new DsdCommonException(USER.NOT_EXIST);
    }
    return Optional.of(toUpdateUser)
        .flatMap(u -> userRepository.findById(u.getId()))
        .map(
            ue ->
                ue.toBuilder()
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phone(user.getPhone())
                    .build())
        .map(userRepository::save)
        .map(userMapper::toDTO)
        .orElse(null);
  }

  @Override
  public void delete(@NonNull Long id) {
    /** TODO Only allow admin user to delete other user */
    if (!isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    if (!userRepository.existsById(id)) {
      throw new DsdCommonException(USER.NOT_EXIST);
    }
    // TODO should delete license as well
    userRepository.deleteById(id);
    deviceService.removeUserFromDevice(List.of(id));
    deviceGroupService.removeUserFromDeviceGroup(List.of(id));
    dsdFileService.removeUserFromFile(List.of(id));
    playlistService.removeUserFromPlaylist(List.of(id));
    scheduleService.removeUserFromSchedule(List.of(id));
  }

  @Deprecated
  @Override
  public User findLoggedInfoByEmail(String email) {
    if (!StringUtils.hasText(email)) {
      return null;
    }

    UserEntity userEntity = userRepository.findByEmail(email).orElse(null);
    if (userEntity == null || userEntity.getUserRoleMap() == null) {
      return null;
    }

    List<UserRoleMapEntity> userRoleMapEntities = userRoleMapRepository.findByUser(userEntity);
    log.info("User role's size: {}", userRoleMapEntities.size());

    userEntity.setUserRoleMap(userRoleMapEntities);
    return userMapper.toDTO(userEntity);
  }

  @Override
  public User createAdmin(User user) {
    return Optional.ofNullable(user)
        .map(userMapper::toEntity)
        .map(userRepository::save)
        .map(
            e -> {
              userRoleMapService.setAdminRoleForUser(e.getId());
              return e;
            })
        .map(userMapper::toDTO)
        .orElse(null);
  }

  @Override
  public void assignLicenseByLicenseIdAndId(@NonNull Long licenseId, @NonNull Long id) {
    if (!userRepository.existsById(id)) {
      throw new DsdCommonException(ERROR.LICENSE.UPDATE);
    }
    userRepository.assignLicenseByLicenseIdAndId(licenseId, id);
  }

  @Override
  public User getByEmail(@NonNull String email) {
    // TODO check admin, if is admin, can getByEmail of other user, while not, can only get current
    return userRepository.findByEmail(email).map(userMapper::toDTOWithLicense).orElse(null);
  }

  @Override
  public void deleteByIds(List<Long> ids) {
    /** TODO Only allow admin user to call this func */
    if (!isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    userRepository.deleteByIds(ids);
  }

  @Override
  public boolean isAdmin() {
    String userEmail = this.getAuthenticatedUserEmail();
    return userRoleMapRepository.existsByEmailAndRole(userEmail, RoleType.ADMIN);
  }

  @Override
  public List<Role> findRolesByEmail(@NonNull String email) {
    return userRepository.findRolesByEmail(email).stream().map(r -> roleMapper.toDTO(r)).toList();
  }
}
