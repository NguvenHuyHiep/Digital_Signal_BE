package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.components.OtpMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.dtos.Otp;
import com.lpdev.dsd.models.entities.OtpEntity;
import com.lpdev.dsd.repositories.OtpRepository;
import com.lpdev.dsd.services.OtpService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class OtpServiceImpl implements OtpService {

  private final OtpRepository otpRepository;
  private final OtpMapper otpMapper;

  @Override
  public Otp create(@NonNull String email) {
    String otpCode = generateOTP();
    LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(120);
    LocalDateTime currentTime = LocalDateTime.now();
    OtpEntity otp = otpRepository.findByEmail(email);
    if (otp != null) {
      log.info("Create OTP by update old record for email: {}", email);
      LocalDateTime lastCreateTime =
          otp.getCreateDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      if (lastCreateTime.plusSeconds(120).isAfter(currentTime)) {
        throw new DsdCommonException(DsdConstant.ERROR.OTP.WAIT);
      }
      return Optional.of(otp)
          .map(
              odg ->
                  odg.toBuilder()
                      .otp(otpCode)
                      .expiryDate(Date.from(expiryTime.atZone(ZoneId.systemDefault()).toInstant()))
                      .createDate(Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()))
                      .status(Status.ACTIVE)
                      .build())
          .map(otpRepository::save)
          .map(otpMapper::toDTO)
          .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.OTP.CREATE));
    } else {
      log.info("Create new OTP for email: {}", email);
      return Optional.of(
              OtpEntity.builder()
                  .otp(otpCode)
                  .email(email)
                  .createDate(Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant()))
                  .expiryDate(Date.from(expiryTime.atZone(ZoneId.systemDefault()).toInstant()))
                  .status(Status.ACTIVE)
                  .build())
          .map(otpRepository::save)
          .map(otpMapper::toDTO)
          .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.OTP.CREATE));
    }
  }

  @Override
  public Otp verify(@NonNull String email, @NonNull String otp) {
    OtpEntity otpEntity = otpRepository.findByEmailAndOtpAndStatus(email, otp, Status.ACTIVE);
    if (otpEntity == null) {
      log.info("otp entity is null, stop verifying");
      throw new DsdCommonException(DsdConstant.ERROR.OTP.NOT_EXIST);
    }

    if (Status.INACTIVE.equals(otpEntity.getStatus())) {
      log.info("otp status is inactive, stop verifying");
      throw new DsdCommonException(DsdConstant.ERROR.OTP.STATUS_INACTIVE);
    }

    if (!otpEntity.getExpiryDate().toInstant().isAfter(Instant.now())) {
      OtpEntity updatedOtpEntity =
          otpRepository.save(otpEntity.toBuilder().status(Status.INACTIVE).build());
      log.info("otp code is expired, set to {}", updatedOtpEntity.getStatus());
      throw new DsdCommonException(DsdConstant.ERROR.OTP.EXPIRED);
    }

    return Optional.of(otpEntity.toBuilder().status(Status.INACTIVE).build())
        .map(otpRepository::save)
        .map(otpMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.OTP.UPDATE));
  }

  @Override
  public Otp getOtpByEmail(@NonNull String email) {
    return Optional.ofNullable(otpRepository.findByEmail(email))
        .map(otpMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.OTP.NOT_EXIST));
  }

  private String generateOTP() {
    return RandomStringUtils.randomNumeric(6);
  }
}
