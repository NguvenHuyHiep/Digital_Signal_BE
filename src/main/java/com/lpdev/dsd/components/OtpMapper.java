package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.Otp;
import com.lpdev.dsd.models.entities.OtpEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpMapper {

  public Otp toDTO(OtpEntity entity) {
    return Optional.ofNullable(entity).map(this::convertToDTO).orElse(null);
  }

  public OtpEntity toEntity(Otp dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                OtpEntity.builder()
                    .id(e.getId())
                    .otp(e.getOtp())
                    .email(e.getEmail())
                    .expiryDate(e.getExpiryDate())
                    .createDate(e.getCreateDate())
                    .build())
        .orElse(null);
  }

  private Otp convertToDTO(OtpEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                Otp.builder()
                    .id(e.getId())
                    .otp(e.getOtp())
                    .email(e.getEmail())
                    .expiryDate(e.getExpiryDate())
                    .createDate(e.getCreateDate())
                    .build())
        .orElse(null);
  }
}
