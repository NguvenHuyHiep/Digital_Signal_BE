package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.Device;
import com.lpdev.dsd.models.entities.DeviceEntity;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

  @Lazy @Autowired private DeviceGroupMapper deviceGroupMapper;
  @Lazy @Autowired private DeviceLogMapper deviceLogMapper;

  public Device toDTO(DeviceEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDto).orElse(null);
  }

  public Device toDTOWithDeviceGroup(DeviceEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            d ->
                d.toBuilder().deviceGroup(deviceGroupMapper.toDTO(entity.getDeviceGroup())).build())
        .orElse(null);
  }

  public Device toDTOWithDeviceLog(DeviceEntity entity) {
    return Optional.ofNullable(entity)
        .map(this::convertToDto)
        .map(
            d ->
                d.toBuilder()
                    .deviceLogs(
                        entity.getDeviceLogs().stream().map(deviceLogMapper::toDTO).toList())
                    .build())
        .orElse(null);
  }

  public DeviceEntity toEntity(Device dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                DeviceEntity.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .code(e.getCode())
                    .information(e.getInformation())
                    .description(e.getDescription())
                    .serialNo(e.getSerialNo())
                    .vehicleNumber(e.getVehicleNumber())
                    .ybs(e.getYbs())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }

  private Device convertToDto(DeviceEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                Device.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .code(e.getCode())
                    .information(e.getInformation())
                    .description(e.getDescription())
                    .serialNo(e.getSerialNo())
                    .vehicleNumber(e.getVehicleNumber())
                    .ybs(e.getYbs())
                    .status(e.getStatus())
                    .build())
        .orElse(null);
  }
}
