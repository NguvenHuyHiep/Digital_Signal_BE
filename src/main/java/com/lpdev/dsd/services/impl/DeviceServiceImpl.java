package com.lpdev.dsd.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.DeviceStatus;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.commons.utils.DsdDateUtils;
import com.lpdev.dsd.commons.utils.DsdUtils;
import com.lpdev.dsd.components.DeviceGroupMapper;
import com.lpdev.dsd.components.DeviceMapper;
import com.lpdev.dsd.components.DsdFileMapper;
import com.lpdev.dsd.components.UserMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.*;
import com.lpdev.dsd.models.entities.*;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.models.responses.DeviceUpdateStatus;
import com.lpdev.dsd.repositories.*;
import com.lpdev.dsd.services.DeviceService;
import com.lpdev.dsd.services.DsdFileService;
import com.lpdev.dsd.services.LicenseService;
import com.lpdev.dsd.services.UserService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
public class DeviceServiceImpl implements DeviceService {

  @Lazy @Autowired private DeviceLogRepository deviceLogRepository;
  @Lazy @Autowired private DeviceGroupRepository deviceGroupRepository;
  @Lazy @Autowired private DeviceRepository deviceRepository;
  @Lazy @Autowired private DeviceMapper deviceMapper;

  @Lazy @Autowired private UserService userService;
  @Lazy @Autowired private UserMapper userMapper;
  @Lazy @Autowired private UserRepository userRepository;

  @Lazy @Autowired private DeviceGroupMapper deviceGroupMapper;
  @Lazy @Autowired private LicenseRepository licenseRepository;
  @Lazy @Autowired private DsdFileService dsdFileService;
  @Lazy @Autowired private ObjectMapper objectMapper;
  @Lazy @Autowired private LicenseService licenseService;
  @Lazy @Autowired private DsdFileRepository dsdFileRepository;
  @Lazy @Autowired private DsdFileMapper dsdFileMapper;

  @Override
  public Page<Device> getByPaging(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      DeviceStatus status) {
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
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

    return deviceRepository.findAll(keyword, pageable, userId, status).map(deviceMapper::toDTO);
  }

  @Override
  public Page<Device> getDevicesByDeviceGroupId(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      DeviceStatus status,
      @NonNull Long deviceGroupId) {
    if (!userService.isAdmin()) {
      DeviceGroupEntity oldDeviceGroupEntity =
          deviceGroupRepository
              .findById(deviceGroupId)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST));
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      if (!oldDeviceGroupEntity.getUser().getId().equals(loggedUser.getId())) {
        throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
      }
    }
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    return deviceRepository
        .findDevicesByDeviceGroupId(keyword, pageable, status, deviceGroupId)
        .map(deviceMapper::toDTO);
  }

  @Override
  public List<Device> getAllByStatusWithDeviceLogs(@NonNull DeviceStatus status) {
    return deviceRepository.findAllByStatus(status).stream().map(deviceMapper::toDTO).toList();
  }

  @Override
  public Device getById(Long id) {
    DeviceEntity oldDeviceEntity =
        deviceRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST));
    if (!isAllowed(oldDeviceEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return deviceMapper.toDTO(oldDeviceEntity);
  }

  @Override
  public Device create(Device device) {
    if (device == null || deviceRepository.existsByCode(device.getCode())) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.EXIST);
    }
    String userEmail = userService.getAuthenticatedUserEmail();
    UserEntity loggedUserEntity = userRepository.findByEmail(userEmail).orElse(null);
    DeviceEntity deviceEntity = deviceMapper.toEntity(device);
    return deviceMapper.toDTO(this.createByUser(deviceEntity, loggedUserEntity));
  }

  @Override
  public Device update(@NonNull Device device) {
    DeviceEntity oldDeviceEntity =
        deviceRepository
            .findById(device.getId())
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST));
    if (!isAllowed(oldDeviceEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return Optional.of(oldDeviceEntity)
        .map(
            ode ->
                ode.toBuilder()
                    .name(device.getName())
                    .information(device.getInformation())
                    .description(device.getDescription())
                    .ybs(device.getYbs())
                    .serialNo(device.getSerialNo())
                    .vehicleNumber(device.getVehicleNumber())
                    .build())
        .map(deviceRepository::save)
        .map(deviceMapper::toDTO)
        .orElse(null);
  }

  @Override
  public void delete(@NonNull Long id) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    if (!deviceRepository.existsById(id)) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST);
    }
    deviceRepository.deleteById(id);
    deviceLogRepository.deleteDeviceLogWhenDeleteDevice(id);
  }

  @Override
  public Device updateStatus(@NonNull Long id, @NonNull DeviceStatus status) {
    DeviceEntity oldDeviceEntity =
        deviceRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST));
    if (!isAllowed(oldDeviceEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return deviceRepository
        .findById(id)
        .map(d -> d.toBuilder().status(status).build())
        .map(deviceRepository::save)
        .map(deviceMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));
  }

  @Override
  public void deleteByIds(List<Long> ids) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    deviceRepository.deleteByIds(ids);
  }

  @Override
  public void assignToDeviceGroupByDeviceGroupIdAndIds(
      @NonNull Long deviceGroupId, @NonNull List<Long> ids) {
    deviceRepository.assignToDeviceGroupByDeviceIdAndIds(deviceGroupId, ids);
  }

  @Override
  public Device updateStatus(@NonNull String code, @NonNull DeviceStatus status) {
    DeviceEntity updatedDeviceEntity = updateStatusForEntity(code, status);
    return deviceMapper.toDTO(updatedDeviceEntity);
  }

  @Override
  public DeviceUpdateStatus updateStatusByLicenseAndCode(
      @NonNull String license, @NonNull String code) {
    log.info("updateStatusByLicenseAndCode with license: {}, code: {}", license, code);
    DeviceGroup deviceGroup = getDeviceGroupByDeviceCode(code);
    Date lastUpdate;
    if (deviceGroup.getPlaylist() == null) {
      lastUpdate = null;
    } else {
      lastUpdate = deviceGroup.getPlaylist().getLastUpdateDate();
    }

    if (licenseService.isExpired(license)) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.EXPIRED);
    }
    DeviceEntity oldDeviceEntity = deviceRepository.findByCode(code);
    if (oldDeviceEntity == null) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST);
    }

    DeviceEntity updatedDeviceEntity = updateStatusForEntity(code, DeviceStatus.ONLINE);
    log.info("updateStatusForEntity: code: {}, status: {}", code, DeviceStatus.ONLINE);
    return new DeviceUpdateStatus(deviceMapper.toDTO(updatedDeviceEntity), lastUpdate);
  }

  @Override
  public Device getWithLogsById(
      @NonNull Long id, DeviceStatus status, Date startDate, Date endDate) {
    Date now = new Date();
    Calendar calendar = Calendar.getInstance();
    if (startDate == null && endDate == null) {
      endDate = now;
      calendar.setTime(endDate);
      calendar.add(Calendar.DATE, -7);
      startDate = calendar.getTime();
    } else if (startDate == null) {
      calendar.setTime(endDate);
      calendar.add(Calendar.DATE, -7);
      startDate = calendar.getTime();
    } else if (endDate == null) {
      calendar.setTime(startDate);
      calendar.add(Calendar.DATE, +7);
      endDate = calendar.getTime();
      if (endDate.after(now)) {
        endDate = now;
      }
    }
    if (startDate.after(endDate) || endDate.after(now)) {
      BaseOutput<Device> response =
          BaseOutput.<Device>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PARAM))
              .status(ResponseStatus.FAILED)
              .build();
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE.INVALID_DATE);
    }
    return Optional.ofNullable(
            deviceRepository.findWithDeviceLogsInADayById(id, status, startDate, endDate))
        .map(deviceMapper::toDTOWithDeviceLog)
        .orElse(null);
  }

  @Override
  public List<Device> getDeviceLogsByDeviceGroupId(
      @NonNull Long deviceGroupId, DeviceStatus status, Date startDate, Date endDate) {
    Calendar calendar = Calendar.getInstance();
    Date now = new Date();
    if ((endDate != null && endDate.after(now))
        || (startDate != null && endDate != null && startDate.after(endDate))) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_LOG.EXPORT);
    }

    if (startDate == null && endDate == null) {
      endDate = now;
      calendar.setTime(endDate);
      calendar.add(Calendar.DATE, -7);
      startDate = calendar.getTime();
    } else if (startDate == null) {
      calendar.setTime(endDate);
      calendar.add(Calendar.DAY_OF_YEAR, -7);
      startDate = calendar.getTime();
    } else if (endDate == null) {
      calendar.setTime(startDate);
      calendar.add(Calendar.DAY_OF_YEAR, +7);
      endDate = calendar.getTime();
      if (endDate.after(now)) {
        endDate = now;
      }
    }

    return deviceRepository
        .findDeviceLogsByDeviceGroupId(deviceGroupId, status, startDate, endDate)
        .stream()
        .map(deviceMapper::toDTOWithDeviceLog)
        .toList();
  }

  @Override
  public Device register(@NonNull String license, @NonNull Device device) {
    log.info("register with device code: {}", device.getCode());
    LicenseEntity licenseEntity = licenseRepository.findByCode(license);
    if (licenseEntity == null) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_EXIST);
    }

    DeviceEntity deviceEntity = deviceRepository.findByCode(device.getCode());
    if (deviceEntity != null) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE.EXIST);
    }

    UserEntity userEntity = userRepository.findByLicense(licenseEntity);
    if (userEntity == null) {
      log.info("license does not assign to user");
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_ASSIGNED_USER);
    }

    DeviceEntity newDeviceEntity = deviceMapper.toEntity(device);
    DeviceEntity savedDeviceEntity = this.createByUser(newDeviceEntity, userEntity);
    log.info("created device with code: {}", savedDeviceEntity.getCode());

    deviceLogRepository.save(
        DeviceLogEntity.builder()
            .device(savedDeviceEntity)
            .status(savedDeviceEntity.getStatus())
            .date(new Date())
            .build());
    log.info(
        "Saved log with status: {} for new device: {}",
        savedDeviceEntity.getStatus().getValue(),
        savedDeviceEntity.getCode());

    return deviceMapper.toDTO(savedDeviceEntity);
  }

  @Override
  public void removeUserFromDevice(@NonNull List<Long> userIds) {
    deviceRepository.removeUserFromDevice(userIds);
  }

  @Override
  public DeviceGroup getDeviceGroupByDeviceCode(@NonNull String code) {
    log.info("getDeviceGroupByDeviceCode with code: {}", code);
    DeviceEntity device = deviceRepository.findByCode(code);
    if (device == null) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST);
    }
    if (device.getDeviceGroup() == null) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST);
    }
    return deviceGroupMapper.toDTOWithPlaylistWithFiles(device.getDeviceGroup());
  }

  @Override
  public DownloadFile downloadDeviceByCode(@NonNull String code, Date deviceLastUpdateDate) {
    log.info("downloadDeviceByCode with code: {}", code);
    DeviceGroup deviceGroup = getDeviceGroupByDeviceCode(code);
    if (deviceGroup == null) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.NOT_EXIST);
    }

    if (deviceGroup.getPlaylist() == null) {
      throw new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST);
    }

    List<String> paths =
        Optional.of(deviceGroup.getPlaylist())
            .map(Playlist::getFiles)
            .filter(files -> !files.isEmpty())
            .stream()
            .flatMap(Collection::stream)
            .map(
                f -> {
                  if (deviceLastUpdateDate == null
                      || f.getAssignDate() == null
                      || deviceLastUpdateDate.before(f.getAssignDate())) {
                    return f.getPath();
                  }
                  return null;
                })
            .filter(Objects::nonNull)
            .toList();

    log.info("paths size to download: {}", paths.size());

    try {
      List<DownloadFile> downloadFiles = new ArrayList<>();
      if (!paths.isEmpty()) {
        downloadFiles.addAll(dsdFileService.downloadByPaths(paths));
      }
      DeviceGroup fakeSchedule = deviceGroup.toBuilder().user(null).devices(null).build();
      String fakeScheduleJson = objectMapper.writeValueAsString(fakeSchedule);

      downloadFiles.add(
          DownloadFile.builder()
              .name("schedule.json") // TODO change back to device group later
              .resource(new ByteArrayResource(fakeScheduleJson.getBytes()))
              .build());

      return DownloadFile.builder()
          .name("schedule.zip") // TODO change back to device group later
          .resource(DsdUtils.zipDownloadFiles(downloadFiles))
          .build();
    } catch (IOException e) {
      log.error("ERROR zipping files", e);
      throw new DsdCommonException(DsdConstant.ERROR.FILE.ZIP);
    }
  }

  @Override
  public void removeDeviceGroupFromDevices(@NonNull List<Long> deviceGroupIds) {
    deviceRepository.removeDeviceGroupFromDevices(deviceGroupIds);
  }

  @Override
  public void removeDevicesFromDeviceGroup(
      @NonNull List<Long> deviceIds, @NonNull Long deviceGroupIds) {
    deviceRepository.removeDevicesFromDeviceGroup(deviceIds, deviceGroupIds);
  }

  private DeviceEntity updateStatusForEntity(@NonNull String code, @NonNull DeviceStatus status) {
    DeviceEntity oldDeviceEntity = deviceRepository.findByCode(code);
    if (oldDeviceEntity == null) {
      return null;
    }
    oldDeviceEntity.setStatus(status);
    deviceRepository.save(oldDeviceEntity);
    deviceLogRepository.save(
        DeviceLogEntity.builder()
            .date(new Date())
            .device(oldDeviceEntity)
            .status(oldDeviceEntity.getStatus())
            .build());
    return oldDeviceEntity;
  }

  private DeviceEntity createByUser(@NonNull DeviceEntity deviceEntity, UserEntity userEntity) {
    if (StringUtils.isBlank(deviceEntity.getCode())
        || deviceRepository.existsByCode(deviceEntity.getCode())) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_GROUP.EXIST);
    }

    if (userEntity != null) {
      deviceEntity.setUser(userEntity);
    }
    deviceEntity.setStatus(DeviceStatus.ONLINE);
    return deviceRepository.save(deviceEntity);
  }

  @Override
  public DownloadFile exportDeviceStatus(@NonNull Long deviceId, Date startDate, Date endDate)
      throws IOException {

    if (startDate != null && endDate != null && startDate.after(endDate)) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_LOG.EXPORT);
    }

    DeviceEntity deviceEntity =
        deviceRepository
            .findById(deviceId)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST));

    if (deviceEntity.getDeviceLogs() != null && !deviceEntity.getDeviceLogs().isEmpty()) {
      try (SXSSFWorkbook workbook = new SXSSFWorkbook();
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

        Sheet deviceSheet =
            workbook.createSheet(
                deviceEntity.getId() + "-" + this.sanitizeSheetName(deviceEntity.getName()));

        Row headerRow = deviceSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Log ID");
        headerRow.createCell(1).setCellValue("Log Date");
        headerRow.createCell(2).setCellValue("Log Status");

        List<DeviceLogEntity> deviceLogEntities = fetchDeviceLogs(deviceId, startDate, endDate);
        this.exportData(deviceSheet, deviceLogEntities);

        workbook.write(outputStream);
        return DownloadFile.builder()
            .name(deviceEntity.getName())
            .resource(new ByteArrayResource(outputStream.toByteArray()))
            .build();
      }
    }

    return null;
  }

  @Override
  public List<DeviceLogEntity> fetchDeviceLogs(Long deviceId, Date startDate, Date endDate) {
    Calendar calendar = Calendar.getInstance();
    Date now = new Date();
    if (endDate != null && endDate.after(now)) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE_LOG.EXPORT);
    }
    if (startDate == null && endDate == null) {
      return deviceLogRepository.findWithDeviceLogsInADayById(deviceId);
    }

    if (startDate == null) {
      calendar.setTime(endDate);
      calendar.add(Calendar.DAY_OF_YEAR, -7);
      startDate = calendar.getTime();
    } else if (endDate == null) {
      calendar.setTime(startDate);
      calendar.add(Calendar.DAY_OF_YEAR, +7);
      endDate = calendar.getTime();
      if (endDate.after(now)) {
        endDate = now;
      }
    }

    return deviceLogRepository.findByDeviceIdAndDateRange(deviceId, startDate, endDate);
  }

  @Override
  public void exportData(Sheet sheet, List<DeviceLogEntity> deviceLogEntities) {
    if (deviceLogEntities != null && !deviceLogEntities.isEmpty()) {
      for (int i = 0; i < deviceLogEntities.size(); i++) {
        DeviceLogEntity deviceLogEntity = deviceLogEntities.get(i);
        if (deviceLogEntity != null) {
          Row logRow = sheet.createRow(i + 1);
          logRow.createCell(0).setCellValue(deviceLogEntity.getId());
          logRow.createCell(1).setCellValue(DsdDateUtils.parseForExport(deviceLogEntity.getDate()));
          logRow.createCell(2).setCellValue(deviceLogEntity.getStatus().getValue());
        }
      }
    }
  }

  private String sanitizeSheetName(String name) {
    return name.replaceAll("[\\\\/*?:\\[\\]]", " ").substring(0, Math.min(31, name.length()));
  }

  private boolean isAllowed(@NonNull DeviceEntity oldDeviceEntity) {
    if (!userService.isAdmin()) {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      return oldDeviceEntity.getUser().getId().equals(loggedUser.getId());
    }
    return true;
  }
}
