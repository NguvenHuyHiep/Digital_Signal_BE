package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.IPLog;
import com.lpdev.dsd.models.entities.IPLogEntity;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class IPLogMapper {
  public IPLog toDTO(IPLogEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDTO).orElse(null);
  }

  public IPLogEntity toEntity(IPLog dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                IPLogEntity.builder()
                    .id(e.getId())
                    .ip(e.getIp())
                    .path(e.getPath())
                    .userInfo(e.getUserInfo())
                    .actionTime(e.getActionTime())
                    .deviceType(e.getDeviceType())
                    .deviceId(e.getDeviceId())
                    .build())
        .orElse(null);
  }

  private IPLog convertToDTO(IPLogEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                IPLog.builder()
                    .id(e.getId())
                    .ip(e.getIp())
                    .path(e.getPath())
                    .userInfo(e.getUserInfo())
                    .actionTime(e.getActionTime())
                    .deviceType(e.getDeviceType())
                    .deviceId(e.getDeviceId())
                    .build())
        .orElse(null);
  }
}
