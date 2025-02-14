package com.lpdev.dsd.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.PLAYLIST;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.SCHEDULE;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.commons.utils.DsdUtils;
import com.lpdev.dsd.components.ScheduleMapper;
import com.lpdev.dsd.components.UserMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.DsdFile;
import com.lpdev.dsd.models.dtos.Playlist;
import com.lpdev.dsd.models.dtos.Schedule;
import com.lpdev.dsd.models.entities.PlaylistEntity;
import com.lpdev.dsd.models.entities.ScheduleEntity;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.repositories.PlaylistRepository;
import com.lpdev.dsd.repositories.ScheduleRepository;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.services.DsdFileService;
import com.lpdev.dsd.services.PlaylistService;
import com.lpdev.dsd.services.ScheduleService;
import com.lpdev.dsd.services.UserService;
import java.io.IOException;
import java.util.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
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
public class ScheduleServiceImpl implements ScheduleService {
  private final ObjectMapper objectMapper;

  private final ScheduleRepository scheduleRepository;
  private final ScheduleMapper scheduleMapper;

  private final PlaylistService playlistService;
  private final PlaylistRepository playlistRepository;

  private final DsdFileService dsdFileService;

  private final UserService userService;
  private final UserMapper userMapper;
  private final UserRepository userRepository;

  @Override
  public Page<Schedule> getByPaging(
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
    return scheduleRepository
        .findAll(Status.ACTIVE, keyword, pageable, userId)
        .map(scheduleMapper::toDTO);
  }

  @Override
  public Schedule getById(@NonNull Long id) {
    ScheduleEntity oldScheduleEntity =
        scheduleRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(SCHEDULE.NOT_EXIST));
    if (!isAllowed(oldScheduleEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return scheduleMapper.toDTO(oldScheduleEntity);
  }

  @Override
  public Schedule create(@NonNull Schedule schedule) {
    if (schedule == null) {
      throw new DsdCommonException(SCHEDULE.NOT_EXIST);
    }

    String userEmail = userService.getAuthenticatedUserEmail();
    UserEntity loggedUser = userRepository.findByEmail(userEmail).orElse(null);
    ScheduleEntity scheduleEntity = scheduleMapper.toEntity(schedule);
    if (scheduleEntity.getStatus() == null) {
      scheduleEntity.setStatus(Status.ACTIVE);
    }
    return Optional.of(scheduleEntity)
        .map(e -> e.setUser(loggedUser))
        .map(scheduleRepository::save)
        .map(scheduleMapper::toDTO)
        .orElse(null);
  }

  @Override
  public Schedule update(@NonNull Schedule schedule) {
    ScheduleEntity oldScheduleEntity =
        scheduleRepository
            .findById(schedule.getId())
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.SCHEDULE.NOT_EXIST));
    if (!isAllowed(oldScheduleEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    return Optional.of(oldScheduleEntity)
        .map(
            os ->
                os.toBuilder()
                    .name(schedule.getName())
                    .description(schedule.getDescription())
                    .days(schedule.getDays())
                    .build())
        .map(scheduleRepository::save)
        .map(scheduleMapper::toDTO)
        .orElse(null);
  }

  @Override
  public void delete(@NonNull Long id) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    scheduleRepository.deleteById(id);
    playlistService.removeScheduleFromPlaylist(List.of(id));
  }

  @Override
  public Schedule assignPlaylist(@NonNull Long id, @NonNull List<Long> playlistIds) {
    if (!scheduleRepository.existsById(id)) {
      throw new DsdCommonException(DsdConstant.ERROR.SCHEDULE.NOT_EXIST);
    }
    playlistService.assignToScheduleByScheduleIdAndIds(id, playlistIds);
    return getWithPlaylistsById(id);
  }

  @Override
  public Schedule getWithPlaylistsById(@NonNull Long id) {
    return Optional.ofNullable(scheduleRepository.findWithPlaylistsById(id))
        .map(scheduleMapper::toDTO)
        .orElse(null);
  }

  @Override
  public Schedule getWithPlaylistsWithFilesById(@NonNull Long id) {
    log.info("getWithPlaylistsWithFilesById : {}", id);
    return Optional.of(scheduleRepository.findById(id))
        .flatMap(
            scheduleEntityOptional -> {
              ScheduleEntity scheduleEntity = scheduleEntityOptional.orElse(null);
              if (scheduleEntity != null) {
                List<PlaylistEntity> playlistEntities =
                    playlistRepository.findWithFilesBySchedule(scheduleEntity);
                scheduleEntity.setPlaylists(playlistEntities);
              }
              return scheduleEntityOptional;
            })
        .map(scheduleMapper::toDTOWithPlayListsWithFiles)
        .orElse(null);
  }

  @Override
  public DownloadFile downloadScheduleById(@NonNull Long id) {
    log.info("downloadScheduleById: {}", id);
    Schedule schedule = getWithPlaylistsWithFilesById(id);
    if (schedule == null) {
      throw new DsdCommonException(DsdConstant.ERROR.SCHEDULE.NOT_EXIST);
    }

    if (schedule.getPlaylists() == null || schedule.getPlaylists().isEmpty()) {
      throw new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST);
    }

    List<String> paths =
        schedule.getPlaylists().stream()
            .filter(Objects::nonNull)
            .map(Playlist::getFiles)
            .filter(Objects::nonNull)
            .filter(dsdFiles -> !dsdFiles.isEmpty())
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .map(DsdFile::getPath)
            .filter(Objects::nonNull)
            .toList();

    log.info("files size to download: {}", paths.size());
    if (paths.isEmpty()) {
      throw new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST);
    }

    try {
      List<DownloadFile> downloadFiles = dsdFileService.downloadByPaths(paths);
      String scheduleJson = objectMapper.writeValueAsString(schedule);
      downloadFiles.add(
          DownloadFile.builder()
              .name("schedule.json")
              .resource(new ByteArrayResource(scheduleJson.getBytes()))
              .build());

      return DownloadFile.builder()
          .name("schedule.zip")
          .resource(DsdUtils.zipDownloadFiles(downloadFiles))
          .build();
    } catch (IOException e) {
      log.error("ERROR zipping files", e);
      throw new DsdCommonException(DsdConstant.ERROR.FILE.ZIP);
    }
  }

  @Override
  public void deleteByIds(List<Long> ids) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    scheduleRepository.deleteByIds(ids);
    playlistService.removeScheduleFromPlaylist(ids);
  }

  @Override
  public void removeUserFromSchedule(@NonNull List<Long> userIds) {
    scheduleRepository.removeUserFromSchedule(userIds);
  }

  @Override
  public Schedule updateStatus(@NonNull Long id, @NonNull Status status) {
    ScheduleEntity oldScheduleEntity =
        scheduleRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(SCHEDULE.NOT_EXIST));
    if (!isAllowed(oldScheduleEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return scheduleRepository
        .findById(id)
        .map(p -> p.toBuilder().status(status).build())
        .map(scheduleRepository::save)
        .map(scheduleMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(PLAYLIST.NOT_EXIST));
  }

  private boolean isAllowed(@NonNull ScheduleEntity oldScheduleEntity) {
    if (!userService.isAdmin()) {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      return oldScheduleEntity.getUser().getId().equals(loggedUser.getId());
    }
    return true;
  }
}
