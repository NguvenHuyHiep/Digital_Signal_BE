package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.DeviceLog;
import com.lpdev.dsd.models.entities.DeviceLogEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceLogMapper {
  @Lazy private final DeviceMapper deviceMapper;

  public DeviceLog toDTO(DeviceLogEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDTO).orElse(null);
  }

  public DeviceLogEntity toEntity(DeviceLog dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                DeviceLogEntity.builder()
                    .id(e.getId())
                    .date(e.getDate())
                    .device(e.getDevice() != null ? deviceMapper.toEntity(e.getDevice()) : null)
                    .build())
        .orElse(null);
  }

  private DeviceLog convertToDTO(DeviceLogEntity entity) {
    return Optional.ofNullable(entity)
        .map(e -> DeviceLog.builder().id(e.getId()).date(e.getDate()).status(e.getStatus()).build())
        .orElse(null);
  }
}
