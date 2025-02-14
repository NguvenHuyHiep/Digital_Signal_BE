package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.PLAYLIST;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.components.PlaylistFileMapMapper;
import com.lpdev.dsd.components.PlaylistMapper;
import com.lpdev.dsd.components.UserMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.dtos.Playlist;
import com.lpdev.dsd.models.entities.PlaylistEntity;
import com.lpdev.dsd.models.entities.PlaylistFileMapEntity;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.repositories.*;
import com.lpdev.dsd.services.*;
import java.util.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PlaylistServiceImpl implements PlaylistService {
  @Lazy @Autowired private PlaylistRepository playlistRepository;
  @Lazy @Autowired private PlaylistMapper playlistMapper;

  @Lazy @Autowired private DsdFileService dsdFileService;
  @Lazy @Autowired private DsdFileRepository dsdFileRepository;
  @Lazy @Autowired private PlaylistFileMapService playlistFileMapService;
  @Lazy @Autowired private DeviceGroupService deviceGroupService;
  @Lazy @Autowired private DeviceGroupRepository deviceGroupRepository;

  @Lazy @Autowired private UserService userService;
  @Lazy @Autowired private UserRoleMapRepository userRoleMapRepository;
  @Lazy @Autowired private UserMapper userMapper;
  @Lazy @Autowired private UserRepository userRepository;
  @Lazy @Autowired private PlaylistFileMapMapper playlistFileMapMapper;
  @Lazy @Autowired private PlaylistFileMapRepository playlistFileMapRepository;

  @Override
  public Playlist getById(@NonNull Long id) {
    PlaylistEntity oldPlaylistEntity =
        playlistRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));
    if (!isAllowed(oldPlaylistEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return playlistMapper.toDTO(oldPlaylistEntity);
  }

  @Override
  public Page<Playlist> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword) {
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    Long userId;
    if (userService.isAdmin()) {
      userId = null;
    } else {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUserEntity =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      userId = loggedUserEntity.getId();
    }
    return playlistRepository
        .findAll(Status.ACTIVE, keyword, pageable, userId)
        .map(playlistMapper::toDTO);
  }

  @Override
  public Playlist create(@NonNull Playlist playlist) {
    String userEmail = userService.getAuthenticatedUserEmail();
    UserEntity loggedUserEntity = userRepository.findByEmail(userEmail).orElse(null);
    PlaylistEntity playlistEntity = playlistMapper.toEntity(playlist);
    playlistEntity.setUser(loggedUserEntity);
    if (playlistEntity.getStatus() == null) {
      playlistEntity.setStatus(Status.ACTIVE);
    }
    playlistEntity.setFileOrder(new LinkedHashSet<>());
    return Optional.of(playlistEntity)
        .map(playlistRepository::save)
        .map(playlistMapper::toDTO)
        .orElse(null);
  }

  @Override
  public Playlist update(@NonNull Playlist playlist) {
    // TODO
    /** check if playlist has user = current user */
    PlaylistEntity oldPlaylistEntity =
        playlistRepository
            .findById(playlist.getId())
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));
    if (!isAllowed(oldPlaylistEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    return Optional.of(oldPlaylistEntity)
        .map(
            op ->
                op.toBuilder()
                    .name(playlist.getName())
                    .description(playlist.getDescription())
                    .startTime(playlist.getStartTime())
                    .endTime(playlist.getEndTime())
                    .isLoop(playlist.getIsLoop())
                    .status(playlist.getStatus())
                    .build())
        .map(playlistRepository::save)
        .map(playlistMapper::toDTO)
        .orElse(null);
  }

  @Override
  public void delete(@NonNull Long id) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    if (!playlistRepository.existsById(id)) {
      throw new DsdCommonException(PLAYLIST.NOT_EXIST);
    }
    playlistRepository.deleteById(id);
    playlistFileMapService.removeByPlaylistIdsAndFileIds(List.of(id), Collections.emptyList());
  }

  @Override
  public Playlist getWithFilesById(@NonNull Long id) {
    return Optional.ofNullable(playlistRepository.findWithFilesById(id))
        .map(playlistMapper::toDTOWithFiles)
        .orElse(null);
  }

  @Override
  public void assignToScheduleByScheduleIdAndIds(
      @NonNull Long scheduleId, @NonNull List<Long> ids) {
    playlistRepository.assignToScheduleByScheduleIdsAndIds(scheduleId, ids);
  }

  @Override
  public void deleteByIds(List<Long> ids) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    playlistRepository.deleteByIds(ids);
    playlistFileMapService.removeByPlaylistIdsAndFileIds(ids, Collections.emptyList());
  }

  @Override
  public void removeScheduleFromPlaylist(@NonNull List<Long> scheduleIds) {
    playlistRepository.removeScheduleFromPlaylist(scheduleIds);
  }

  @Override
  public void removeUserFromPlaylist(@NonNull List<Long> userIds) {
    playlistRepository.removeUserFromPlaylist(userIds);
  }

  @Override
  public Playlist updateStatus(@NonNull Long id, @NonNull Status status) {
    PlaylistEntity oldPlaylistEntity =
        playlistRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));
    if (!isAllowed(oldPlaylistEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return playlistRepository
        .findById(id)
        .map(p -> p.toBuilder().status(status).build())
        .map(playlistRepository::save)
        .map(playlistMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(PLAYLIST.NOT_EXIST));
  }

  @Override
  public Playlist getWithDeviceGroupsById(@NonNull Long id) {
    return Optional.ofNullable(playlistRepository.findWithDeviceGroupsById(id))
        .map(playlistMapper::toDTOWithDeviceGroups)
        .orElse(null);
  }

  @Override
  public Playlist assignDeviceGroups(@NonNull Long id, @NonNull List<Long> deviceGroupIds) {
    if (!playlistRepository.existsById(id)) {
      throw new DsdCommonException(PLAYLIST.NOT_EXIST);
    }
    deviceGroupRepository.assignToPlaylistByPlaylistIdAndIds(id, deviceGroupIds);
    return getWithDeviceGroupsById(id);
  }

  @Override
  public void removeDeviceGroups(@NonNull Long id, @NonNull List<Long> deviceGroupIds) {
    deviceGroupService.removeDeviceGroupsFromPlaylist(deviceGroupIds, id);
  }

  @Override
  public void updateLastUpdate(@NonNull List<Long> ids) {
    playlistRepository.lastUpdateDate(ids);
  }

  private boolean isAllowed(@NonNull PlaylistEntity oldPlaylistEntity) {
    if (!userService.isAdmin()) {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      return oldPlaylistEntity.getUser().getId().equals(loggedUser.getId());
    }
    return true;
  }

  @Override
  public Playlist moveFileInPlaylist(@NonNull Long playlistId, @NonNull Long fileId, boolean idUp) {
    PlaylistEntity playlist =
        playlistRepository
            .findById(playlistId)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));

    LinkedHashSet<Long> fileOrder = playlist.getFileOrder();
    if (!fileOrder.contains(fileId)) {
      throw new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST);
    }

    List<Long> fileList = new ArrayList<>(fileOrder);

    int currentIndex = fileList.indexOf(fileId);
    if (currentIndex < 0) {
      throw new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST);
    }

    int newIndex = idUp ? currentIndex - 1 : currentIndex + 1;
    if (newIndex < 0 || newIndex >= fileList.size()) {
      throw new DsdCommonException(DsdConstant.ERROR.FILE.MOVE);
    }

    Collections.swap(fileList, currentIndex, newIndex);
    LinkedHashSet<Long> updatedFileOrder = new LinkedHashSet<>(fileList);

    playlist.setFileOrder(updatedFileOrder);
    playlist.setLastUpdateDate(new Date());
    return Optional.of(playlist)
        .map(playlistRepository::save)
        .map(playlistMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(PLAYLIST.UPDATE));
  }

  @Override
  public Playlist updateFileOrder(@NonNull Long id) {
    PlaylistEntity playlist =
        playlistRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));

    List<PlaylistFileMapEntity> playlistFileMapEntities =
        playlistFileMapRepository.findByPlaylistId(id);

    LinkedHashSet<Long> fileOrder = new LinkedHashSet<>();
    for (PlaylistFileMapEntity playlistFileMap : playlistFileMapEntities) {
      fileOrder.add(playlistFileMap.getFile().getId());
    }

    playlist.setFileOrder(fileOrder);
    return Optional.of(playlist)
        .map(playlistRepository::save)
        .map(playlistMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(PLAYLIST.UPDATE));
  }
}
