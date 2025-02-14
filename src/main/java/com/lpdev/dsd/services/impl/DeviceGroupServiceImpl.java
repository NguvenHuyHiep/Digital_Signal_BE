package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.components.DeviceGroupMapper;
import com.lpdev.dsd.components.UserMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.DeviceGroup;
import com.lpdev.dsd.models.entities.DeviceGroupEntity;
import com.lpdev.dsd.models.entities.DeviceLogEntity;
import com.lpdev.dsd.models.entities.PlaylistEntity;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.repositories.DeviceGroupRepository;
import com.lpdev.dsd.repositories.PlaylistRepository;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.services.DeviceGroupService;
import com.lpdev.dsd.services.DeviceService;
import com.lpdev.dsd.services.PlaylistService;
import com.lpdev.dsd.services.UserService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class DeviceGroupServiceImpl implements DeviceGroupService {

  @Lazy @Autowired private DeviceGroupRepository deviceGroupRepository;
  @Lazy @Autowired private DeviceGroupMapper deviceGroupMapper;
  @Lazy @Autowired private PlaylistRepository playlistRepository;
  @Lazy @Autowired private DeviceService deviceService;

  @Lazy @Autowired private UserService userService;
  @Lazy @Autowired private PlaylistService playlistService;
  @Lazy @Autowired private UserMapper userMapper;
  @Lazy @Autowired private UserRepository userRepository;

  @Override
  public Page<DeviceGroup> getByPaging(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      Status status) {
    Long userId;
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
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
    return deviceGroupRepository
        .findAll(status, keyword, pageable, userId)
        .map(deviceGroupMapper::toDTOWithDevices);
  }

  @Override
  public Page<DeviceGroup> getDeviceGroupsByPlaylistId(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      @NonNull Long playlistId) {
    if (!userService.isAdmin()) {
      PlaylistEntity oldPlaylistEntity =
          playlistRepository
              .findById(playlistId)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      if (!oldPlaylistEntity.getUser().getId().equals(loggedUser.getId())) {
        throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
      }
    }
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    return deviceGroupRepository
        .findDeviceGroupsByPlaylistId(Status.ACTIVE, keyword, pageable, playlistId)
        .map(deviceGroupMapper::toDTO);
  }

  @Override
  public DeviceGroup getById(@NotNull Long id) {
    DeviceGroupEntity oldDeviceGroupEntity =
        deviceGroupRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST));

    if (!isAllowed(oldDeviceGroupEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return deviceGroupMapper.toDTO(oldDeviceGroupEntity);
  }

  @Override
  public DeviceGroup save(@NotNull DeviceGroup deviceGroup) {
    DeviceGroupEntity deviceGroupEntity = deviceGroupMapper.toEntity(deviceGroup);
    String userEmail = userService.getAuthenticatedUserEmail();
    userRepository.findByEmail(userEmail).ifPresent(deviceGroupEntity::setUser);
    if (deviceGroupEntity.getStatus() == null) {
      deviceGroupEntity.setStatus(Status.ACTIVE);
    }
    return Optional.of(deviceGroupEntity)
        .map(deviceGroupRepository::save)
        .map(deviceGroupMapper::toDTO)
        .orElse(null);
  }

  @Override
  public DeviceGroup update(@NonNull DeviceGroup deviceGroup) {
    DeviceGroupEntity oldDeviceGroupEntity =
        deviceGroupRepository
            .findById(deviceGroup.getId())
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST));
    if (!isAllowed(oldDeviceGroupEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return Optional.of(oldDeviceGroupEntity)
        .map(
            odg ->
                odg.toBuilder()
                    .name(deviceGroup.getName())
                    .description(deviceGroup.getDescription())
                    .build())
        .map(deviceGroupRepository::save)
        .map(deviceGroupMapper::toDTO)
        .orElse(null);
  }

  @Override
  public void delete(@NotNull Long id) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    if (!deviceGroupRepository.existsById(id)) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST);
    }
    deviceGroupRepository.deleteById(id);
    deviceService.removeDeviceGroupFromDevices(List.of(id));
  }

  @Override
  public DeviceGroup updateStatus(@NonNull Long id, @NonNull Status status) {
    DeviceGroupEntity oldDeviceGroupEntity =
        deviceGroupRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST));
    if (!isAllowed(oldDeviceGroupEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return deviceGroupRepository
        .findById(id)
        .map(d -> d.toBuilder().status(status).build())
        .map(deviceGroupRepository::save)
        .map(deviceGroupMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));
  }

  @Override
  public DeviceGroup assignDevices(@NonNull Long id, @NonNull List<Long> dsdDeviceIds) {
    if (!deviceGroupRepository.existsById(id)) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST);
    }
    deviceService.assignToDeviceGroupByDeviceGroupIdAndIds(id, dsdDeviceIds);

    return getWithDeviceById(id);
  }

  @Override
  public DeviceGroup getWithDeviceById(@NonNull Long id) {
    return Optional.ofNullable(deviceGroupRepository.findWithDevicesById(id))
        .map(deviceGroupMapper::toDTOWithDevices)
        .orElse(null);
  }

  @Override
  public void deleteByIds(@NotNull List<Long> ids) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    deviceGroupRepository.deleteByIds(ids);
    deviceService.removeDeviceGroupFromDevices(ids);
  }

  @Override
  public void removeUserFromDeviceGroup(@NonNull List<Long> userIds) {
    deviceGroupRepository.removeUserFromDeviceGroup(userIds);
  }

  @Override
  public void assignToPlaylistByPlaylistIdAndIds(
      @NonNull Long playlistId, @NonNull List<Long> ids) {
    deviceGroupRepository.assignToPlaylistByPlaylistIdAndIds(playlistId, ids);
  }

  @Override
  public void removeDeviceGroupsFromPlaylist(
      @NonNull List<Long> deviceGroupIds, @NonNull Long playlistId) {
    deviceGroupRepository.removeDeviceGroupsFromPlaylist(deviceGroupIds, playlistId);
  }

  @Override
  public void removeDevices(@NonNull Long id, @NonNull List<Long> deviceIds) {
    if (!deviceGroupRepository.existsById(id)) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST);
    }
    deviceService.removeDevicesFromDeviceGroup(deviceIds, id);
  }

  private boolean isAllowed(@NonNull DeviceGroupEntity oldDeviceGroupEntity) {
    if (!userService.isAdmin()) {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      return oldDeviceGroupEntity.getUser().getId().equals(loggedUser.getId());
    }
    return true;
  }

  @Override
  public DownloadFile exportDeviceStatus(@NonNull Long deviceGroupId, Date startDate, Date endDate)
      throws IOException {

    if (startDate != null && endDate != null && startDate.after(endDate)) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_LOG.EXPORT);
    }

    DeviceGroupEntity deviceGroupEntity =
        deviceGroupRepository
            .findById(deviceGroupId)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST));

    if (deviceGroupEntity.getDevices() != null && !deviceGroupEntity.getDevices().isEmpty()) {
      try (SXSSFWorkbook workbook = new SXSSFWorkbook();
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        deviceGroupEntity
            .getDevices()
            .forEach(
                deviceEntity -> {
                  Sheet deviceSheet =
                      workbook.createSheet(
                          deviceEntity.getId()
                              + "-"
                              + this.sanitizeSheetName(deviceEntity.getName()));

                  Row headerRow = deviceSheet.createRow(0);
                  headerRow.createCell(0).setCellValue("Log ID");
                  headerRow.createCell(1).setCellValue("Log Date");
                  headerRow.createCell(2).setCellValue("Log Status");

                  List<DeviceLogEntity> deviceLogEntities =
                      deviceService.fetchDeviceLogs(deviceEntity.getId(), startDate, endDate);
                  deviceService.exportData(deviceSheet, deviceLogEntities);
                });
        workbook.write(outputStream);
        return DownloadFile.builder()
            .name(deviceGroupEntity.getName())
            .resource(new ByteArrayResource(outputStream.toByteArray()))
            .build();
      }
    }

    return null;
  }

  // Excel sheet names cannot contain certain characters and must be limited in length
  private String sanitizeSheetName(String name) {
    return name.replaceAll("[\\\\/*?:\\[\\]]", " ").substring(0, Math.min(31, name.length()));
  }
}
