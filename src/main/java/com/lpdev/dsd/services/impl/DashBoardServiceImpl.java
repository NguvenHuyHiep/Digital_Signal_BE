package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.DeviceStatus;
import com.lpdev.dsd.components.PlaylistMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.dtos.Playlist;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.models.responses.DashBoardResponse;
import com.lpdev.dsd.repositories.DeviceGroupRepository;
import com.lpdev.dsd.repositories.DeviceRepository;
import com.lpdev.dsd.repositories.PlaylistRepository;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.services.DashBoardService;
import com.lpdev.dsd.services.DeviceGroupService;
import com.lpdev.dsd.services.DeviceService;
import com.lpdev.dsd.services.PlaylistService;
import com.lpdev.dsd.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class DashBoardServiceImpl implements DashBoardService {
  @Autowired DeviceService deviceService;
  @Autowired DeviceRepository deviceRepository;
  @Autowired PlaylistService playlistService;
  @Autowired PlaylistRepository playlistRepository;
  @Autowired DeviceGroupService deviceGroupService;
  @Autowired DeviceGroupRepository deviceGroupRepository;
  @Autowired PlaylistMapper playlistMapper;
  @Autowired UserService userService;
  @Autowired UserRepository userRepository;

  @Override
  public DashBoardResponse getDashBoard() {
    Long userId;
    if (userService.isAdmin()) {
      userId = null;
    } else {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      userId = loggedUser.getId();
    }
    Long numberDeviceOffline = deviceRepository.countDeviceByStatus(DeviceStatus.OFFLINE, userId);
    Long numberDeviceOnline = deviceRepository.countDeviceByStatus(DeviceStatus.ONLINE, userId);
    Long numberPlaylist = playlistRepository.countByUserId(userId);
    Long numberDeviceGroup = deviceGroupRepository.countByUserId(userId);
    Playlist lastPlaylist =
        playlistMapper.toDTO(playlistRepository.findFirstByOrderByIdDesc(userId));
    return DashBoardResponse.builder()
        .totalPlaylists(numberPlaylist)
        .totalOnlineDevices(numberDeviceOnline)
        .totalOfflineDevices(numberDeviceOffline)
        .totalDeviceGroups(numberDeviceGroup)
        .lastPlaylist(lastPlaylist)
        .build();
  }
}
