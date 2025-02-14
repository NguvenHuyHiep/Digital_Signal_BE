package com.lpdev.dsd.repositories;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.entities.DeviceGroupEntity;
import com.lpdev.dsd.models.entities.DeviceLogEntity;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DeviceGroupRepository extends JpaRepository<DeviceGroupEntity, Long> {
  @Query(
      "SELECT DISTINCT d FROM DeviceGroupEntity  d "
          + "LEFT JOIN FETCH d.user u "
          + "LEFT JOIN FETCH u.userRoleMap urm "
          + "LEFT JOIN FETCH urm.role r "
          + "WHERE (:userId is null OR u.id = :userId)"
          + "AND (:keyword is null OR UPPER(d.name) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.description) LIKE CONCAT('%', UPPER(:keyword), '%')) "
          + "AND (:status is null OR d.status = :status)")
  Page<DeviceGroupEntity> findAll(
      Status status, String keyword, Pageable pageable, @Param("userId") Long userId);

  @Query(
      "SELECT d FROM DeviceGroupEntity d WHERE d.playlist.id = :playlistId "
          + " AND (d.status = :status AND :keyword is null OR UPPER(d.name) LIKE CONCAT('%', UPPER(:keyword), '%'))")
  Page<DeviceGroupEntity> findDeviceGroupsByPlaylistId(
      Status status, String keyword, Pageable pageable, @Param("playlistId") Long playlistId);

  @Query("SELECT p FROM DeviceGroupEntity p JOIN FETCH p.devices WHERE p.id = :id")
  DeviceGroupEntity findWithDevicesById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM DeviceGroupEntity f WHERE f.id IN :ids")
  void deleteByIds(@Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query("UPDATE DeviceGroupEntity d SET d.user.id=null WHERE d.user.id in :userIds")
  void removeUserFromDeviceGroup(@Param("userIds") List<Long> userIds);

  @Modifying
  @Transactional
  @Query("UPDATE DeviceGroupEntity d SET d.playlist.id = :playlistId WHERE d.id IN :ids")
  void assignToPlaylistByPlaylistIdAndIds(
      @Param("playlistId") Long playlistId, @Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query(
      "UPDATE DeviceGroupEntity d SET d.playlist.id=null WHERE d.id in :deviceGroupIds"
          + " AND d.playlist.id in :playlistId")
  void removeDeviceGroupsFromPlaylist(
      @Param("deviceGroupIds") List<Long> deviceGroupIds, @Param("playlistId") Long playlistId);

  @Query(
      "SELECT count(dg) from DeviceGroupEntity dg "
          + "WHERE (:userId is null OR dg.user.id = :userId)")
  Long countByUserId(Long userId);

  @Query(
      "SELECT d FROM DeviceLogEntity d WHERE d.device.id = :deviceId "
          + "AND  d.date BETWEEN :startDate AND :endDate ORDER BY d.date DESC")
  List<DeviceLogEntity> findByDeviceIdAndDateRange(
      @Param("deviceId") Long deviceId,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate);
}
