package com.lpdev.dsd.components;

import com.lpdev.dsd.models.dtos.License;
import com.lpdev.dsd.models.entities.LicenseEntity;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LicenseMapper {
  public License toDTO(LicenseEntity entity) {
    return Optional.ofNullable(entity)
        .map(
            e ->
                License.builder()
                    .id(e.getId())
                    .code(e.getCode())
                    .token(e.getToken())
                    .activationDate(e.getActiveDate())
                    .expirationDate(e.getExpireDate())
                    .privateKey(e.getPrivateKey())
                    .publicKey(e.getPublicKey())
                    .description(e.getDescription())
                    .build())
        .orElse(null);
  }

  public LicenseEntity toEntity(License dto) {
    return Optional.ofNullable(dto)
        .map(
            e ->
                LicenseEntity.builder()
                    .id(e.getId())
                    .code(e.getCode())
                    .token(e.getToken())
                    .activeDate(e.getActivationDate())
                    .expireDate(e.getExpirationDate())
                    .privateKey(e.getPrivateKey())
                    .publicKey(e.getPublicKey())
                    .description(e.getDescription())
                    .build())
        .orElse(null);
  }
}
