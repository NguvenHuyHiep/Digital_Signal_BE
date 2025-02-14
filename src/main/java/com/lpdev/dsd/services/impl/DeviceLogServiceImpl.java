package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.components.DeviceLogMapper;
import com.lpdev.dsd.components.DeviceMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.dtos.Device;
import com.lpdev.dsd.models.dtos.DeviceLog;
import com.lpdev.dsd.models.entities.DeviceEntity;
import com.lpdev.dsd.models.entities.DeviceLogEntity;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.repositories.DeviceLogRepository;
import com.lpdev.dsd.repositories.DeviceRepository;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.services.DeviceLogService;
import com.lpdev.dsd.services.UserService;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceLogServiceImpl implements DeviceLogService {
  @Lazy @Autowired DeviceLogRepository deviceLogRepository;
  @Lazy @Autowired DeviceLogMapper deviceLogMapper;
  @Lazy @Autowired private DeviceMapper deviceMapper;
  @Lazy @Autowired private UserService userService;
  @Lazy @Autowired private DeviceRepository deviceRepository;
  @Lazy @Autowired private UserRepository userRepository;

  @Override
  public Page<DeviceLog> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword) {
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    return deviceLogRepository.findAll(keyword, pageable).map(deviceLogMapper::toDTO);
  }

  @Override
  public Page<DeviceLog> getDeviceLogsByDeviceId(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      @NonNull Long deviceId) {
    if (!userService.isAdmin()) {
      DeviceEntity oldDeviceEntity =
          deviceRepository
              .findById(deviceId)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST));
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      if (!oldDeviceEntity.getUser().getId().equals(loggedUser.getId())) {
        throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
      }
    }
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    return deviceLogRepository
        .findDeviceLogsByDeviceId(keyword, pageable, deviceId)
        .map(deviceLogMapper::toDTO);
  }

  @Override
  public Page<DeviceLog> getLatestByDeviceAndPagingOrderByDate(@NonNull Device device) {
    DeviceEntity deviceEntity = deviceMapper.toEntity(device);
    Pageable pageable = PageRequest.of(0, 1);
    return deviceLogRepository
        .findLatestByDeviceAndPagingOrderByDate(deviceEntity, pageable)
        .map(deviceLogMapper::toDTO);
  }

  @Override
  public DeviceLog getById(Long id) {
    return Optional.ofNullable(id)
        .flatMap(e -> deviceLogRepository.findById(id))
        .map(deviceLogMapper::toDTO)
        .orElse(null);
  }

  @Override
  public DeviceLog save(DeviceLog deviceLog) {
    return Optional.of(deviceLog)
        .map(deviceLogMapper::toEntity)
        .map(deviceLogRepository::save)
        .map(deviceLogMapper::toDTO)
        .orElse(null);
  }

  @Override
  public DeviceLog update(@NonNull DeviceLog deviceLog) {
    DeviceLogEntity oldDeviceLog = deviceLogRepository.findById(deviceLog.getId()).orElse(null);

    if (oldDeviceLog == null) {
      throw new DsdCommonException(DsdConstant.ERROR.DEVICE.NOT_EXIST);
    }
    return Optional.of(oldDeviceLog)
        .map(odg -> odg.toBuilder().date(deviceLog.getDate()).build())
        .map(deviceLogRepository::save)
        .map(deviceLogMapper::toDTO)
        .orElse(null);
  }

  @Override
  public void delete(Long id) {
    if (StringUtils.isBlank(id.toString())) {
      return;
    }
    deviceLogRepository.deleteById(id);
  }
}
