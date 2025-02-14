package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.utils.DsdDateUtils;
import com.lpdev.dsd.commons.utils.DsdUtils;
import com.lpdev.dsd.components.LicenseMapper;
import com.lpdev.dsd.components.UserMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.dtos.License;
import com.lpdev.dsd.models.entities.LicenseEntity;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.repositories.LicenseRepository;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.services.LicenseService;
import com.lpdev.dsd.services.UserService;
import io.jsonwebtoken.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
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
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class LicenseServiceImpl implements LicenseService {
  private final LicenseRepository licenseRepository;
  private final LicenseMapper licenseMapper;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  @Lazy @Autowired UserService userService;

  @Override
  public License getById(@NonNull Long id) {
    LicenseEntity oldLicenseEntity =
        licenseRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_EXIST));
    if (!isAllowed(oldLicenseEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return licenseMapper.toDTO(oldLicenseEntity);
  }

  @Override
  public Page<License> getByPaging(int pageNo, int pageSize, String sortBy, String sortDirection) {
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    return licenseRepository.findAll(pageable).map(licenseMapper::toDTO);
  }

  @Override
  @Transactional
  public License generateByEmail(@NonNull String email, @NonNull Long activeDuration) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    UserEntity assignedUser = userRepository.findByEmail(email).orElse(null);
    if (assignedUser == null) {
      throw new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST);
    }

    if (assignedUser.getLicense() != null) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.EXIST_BY_USER);
    }

    KeyPair keyPair = DsdUtils.generateRsaKeyPair();
    if (keyPair == null) {
      throw new DsdCommonException(DsdConstant.ERROR.SECURITY.GENERATE_KEY_PAIR);
    }

    Date exprirationDate;
    if (activeDuration <= 0) {
      exprirationDate = DsdDateUtils.getMaxDateForPostgreSQL();
    } else {
      exprirationDate = new Date(System.currentTimeMillis() + activeDuration);
    }

    String jwt =
        Jwts.builder()
            .setSubject(assignedUser.getEmail())
            .setExpiration(exprirationDate)
            .signWith(keyPair.getPrivate())
            .compact();

    String publicKey = DsdUtils.encodeKeyToString(keyPair.getPublic());
    String privateKey = DsdUtils.encodeKeyToString(keyPair.getPrivate());
    if (StringUtils.isAllBlank(privateKey, publicKey)) {
      throw new DsdCommonException(DsdConstant.ERROR.SECURITY.DECODE_KEY);
    }

    LicenseEntity toSaveLicenseEntity =
        LicenseEntity.builder()
            .code(this.generateUniqueLicense())
            .token(jwt)
            .activeDate(new Date())
            .expireDate(exprirationDate)
            .privateKey(privateKey)
            .publicKey(publicKey)
            .description("Assigned by Admin")
            .user(assignedUser)
            .build();

    LicenseEntity savedLicenseEntity = licenseRepository.save(toSaveLicenseEntity);

    assignedUser.setLicense(toSaveLicenseEntity);
    userRepository.save(assignedUser);

    return licenseMapper.toDTO(savedLicenseEntity);
  }

  @Override
  public License expand(@NonNull String code, @NonNull Long duration) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    LicenseEntity oldLicenseEntity = licenseRepository.findByCode(code);
    if (oldLicenseEntity == null) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_EXIST);
    }

    UserEntity assignToUserEntity = oldLicenseEntity.getUser();
    if (assignToUserEntity == null) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_ASSIGNED_USER);
    }

    PrivateKey privateKey = DsdUtils.getPrivateFromString(oldLicenseEntity.getPrivateKey());
    if (privateKey == null) {
      throw new DsdCommonException(DsdConstant.ERROR.SECURITY.DECODE_KEY);
    }

    try {
      Date newExpirationDate;
      if (duration == 0) {
        newExpirationDate = DsdDateUtils.getMaxDateForPostgreSQL();
      } else {
        newExpirationDate = new Date(System.currentTimeMillis() + duration);
      }

      String newToken =
          Jwts.builder()
              .setSubject(oldLicenseEntity.getUser().getEmail())
              .setExpiration(newExpirationDate)
              .signWith(privateKey, SignatureAlgorithm.RS256)
              .compact();

      oldLicenseEntity.setToken(newToken);
      oldLicenseEntity.setExpireDate(newExpirationDate);
      licenseRepository.save(oldLicenseEntity);
      return licenseMapper.toDTO(oldLicenseEntity);
    } catch (Exception e) {
      log.error("ERROR expand code", e);
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.EXPAND_FAILED);
    }
  }

  @Override
  public void delete(@NonNull Long id) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    log.info("delete license by id: {}", id);
    if (id <= 0) {
      log.info("license with id <= 0");
      return;
    }
    LicenseEntity licenseEntity = licenseRepository.findById(id).orElse(null);
    if (licenseEntity == null) {
      log.info("license by id not exist");
      return;
    }

    UserEntity userEntityByLicense = userRepository.findByLicense(licenseEntity);
    if (userEntityByLicense == null) {
      log.info("license is not unassigned to user");
    } else {
      log.info("set license of user to null then update user");
      userEntityByLicense.setLicense(null);
      userRepository.save(userEntityByLicense);
    }
    licenseRepository.deleteById(id);
    log.info("deleted license with id: {}", id);
  }

  @Override
  public void assignUserByUserIdAndId(@NonNull Long licenseId, @NonNull Long id) {
    if (!licenseRepository.existsById(licenseId)) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_EXIST);
    }
    userService.assignLicenseByLicenseIdAndId(licenseId, id);
  }

  @Override
  public boolean isExpired(@NonNull String code) {
    LicenseEntity oldLicenseEntity = licenseRepository.findByCode(code);
    if (oldLicenseEntity == null) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_EXIST);
    }

    if (oldLicenseEntity.getUser() == null) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_ASSIGNED_USER);
    }

    PublicKey publicKey = DsdUtils.getPublicFromString(oldLicenseEntity.getPublicKey());
    if (publicKey == null) {
      throw new DsdCommonException(DsdConstant.ERROR.SECURITY.DECODE_KEY);
    }

    try {
      Jws<Claims> jwt =
          Jwts.parserBuilder()
              .setSigningKey(publicKey)
              .build()
              .parseClaimsJws(oldLicenseEntity.getToken());

      Claims claims = jwt.getBody();
      String subject = claims.getSubject();
      if (subject == null || !subject.equals(oldLicenseEntity.getUser().getEmail())) {
        throw new DsdCommonException(DsdConstant.ERROR.LICENSE.UNMATCHED);
      }

      return System.currentTimeMillis() > oldLicenseEntity.getExpireDate().getTime();
    } catch (ExpiredJwtException e) {
      log.error("ERROR license token expire: {}", e.getMessage(), e);
      return false;
    } catch (JwtException e) {
      log.error("ERROR parsing license token: {}", e.getMessage(), e);
      return false;
    } catch (Exception e) {
      log.error("ERROR check expire token: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  @Transactional
  public License verify(@NonNull String code) {
    LicenseEntity oldLicenseEntity = licenseRepository.findByCode(code);
    if (oldLicenseEntity == null) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_EXIST);
    }

    if (oldLicenseEntity.getUser() == null) {
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.NOT_ASSIGNED_USER);
    }

    PublicKey publicKey = DsdUtils.getPublicFromString(oldLicenseEntity.getPublicKey());
    if (publicKey == null) {
      throw new DsdCommonException(DsdConstant.ERROR.SECURITY.DECODE_KEY);
    }

    try {
      Jws<Claims> jwt =
          Jwts.parserBuilder()
              .setSigningKey(publicKey)
              .build()
              .parseClaimsJws(oldLicenseEntity.getToken());

      Claims claims = jwt.getBody();
      String subject = claims.getSubject();
      if (subject == null || !subject.equals(oldLicenseEntity.getUser().getEmail())) {
        throw new DsdCommonException(DsdConstant.ERROR.LICENSE.UNMATCHED);
      }

      if (System.currentTimeMillis() > oldLicenseEntity.getExpireDate().getTime()) {
        throw new DsdCommonException(DsdConstant.ERROR.LICENSE.EXPIRED);
      }

      return Optional.of(oldLicenseEntity).map(licenseMapper::toDTO).orElse(null);
    } catch (ExpiredJwtException e) {
      log.error("ERROR license token expire: {}", e.getMessage(), e);
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.EXPIRED);
    } catch (JwtException e) {
      log.error("ERROR parsing license token: {}", e.getMessage(), e);
      throw new DsdCommonException(DsdConstant.ERROR.LICENSE.INVALID);
    }
  }

  private String generateUniqueLicense() {
    String newLicenseCode = DsdUtils.randomLicenseGenerate();
    if (licenseRepository.existsByCode(newLicenseCode)) {
      return this.generateUniqueLicense();
    }
    return newLicenseCode;
  }

  private boolean isAllowed(@NonNull LicenseEntity oldLicenseEntity) {
    if (!userService.isAdmin()) {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      return oldLicenseEntity.getUser().getId().equals(loggedUser.getId());
    }
    return true;
  }
}
