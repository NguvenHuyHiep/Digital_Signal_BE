package com.lpdev.dsd.repositories;

import com.lpdev.dsd.models.entities.LicenseEntity;
import com.lpdev.dsd.models.entities.RoleEntity;
import com.lpdev.dsd.models.entities.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUserName(String username);

  @Query(
          "SELECT u FROM UserEntity u " +
                  "WHERE u.userName LIKE %:keyword% OR u.email LIKE %:keyword% " +
                  "OR u.phone LIKE %:keyword% " +
                  "OR u.firstName LIKE %:keyword% OR u.lastName LIKE %:keyword%")
  Page<UserEntity> findByKeyword(String keyword, Pageable pageable);

  @Query("SELECT u FROM UserEntity u WHERE u.license = :licenseEntity")
  UserEntity findByLicense(LicenseEntity licenseEntity);

  @Modifying
  @Transactional
  @Query("UPDATE UserEntity f SET f.license.id = :licenseId WHERE f.id IN :id")
  void assignLicenseByLicenseIdAndId(@Param("licenseId") Long LicenseId, @Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM UserEntity u WHERE u.id IN :ids")
  void deleteByIds(@Param("ids") List<Long> ids);

  @Query(
      "SELECT role FROM UserEntity user "
          + "JOIN user.userRoleMap urm "
          + "JOIN urm.role role "
          + "WHERE user.email = :email")
  List<RoleEntity> findRolesByEmail(String email);
}
