package com.lpdev.dsd.repositories;

import com.lpdev.dsd.commons.enums.DeviceStatus;
import com.lpdev.dsd.models.entities.DeviceEntity;
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
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {

  @Query(
      "SELECT d FROM DeviceEntity  d "
          + "LEFT JOIN FETCH d.user u "
          + "LEFT JOIN FETCH u.userRoleMap urm "
          + "LEFT JOIN FETCH urm.role r "
          + "WHERE (:userId is null OR u.id = :userId)"
          + "AND (:keyword is null OR UPPER(d.name) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.code) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.description) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.serialNo) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.vehicleNumber) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.ybs) LIKE CONCAT('%', UPPER(:keyword), '%'))  "
          + "AND (:status is null OR d.status = :status) ")
  Page<DeviceEntity> findAll(
      String keyword,
      Pageable pageable,
      @Param("userId") Long userId,
      @Param("status") DeviceStatus status);

  @Query(
      "SELECT d FROM DeviceEntity d "
          + "WHERE (d.deviceGroup.id = :deviceGroupId)"
          + "AND (:keyword is null OR UPPER(d.name) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.code) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.description) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.serialNo) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.vehicleNumber) LIKE CONCAT('%', UPPER(:keyword), '%') OR UPPER(d.ybs) LIKE CONCAT('%', UPPER(:keyword), '%'))  "
          + "AND (:status is null OR d.status = :status) ")
  Page<DeviceEntity> findDevicesByDeviceGroupId(
      String keyword,
      Pageable pageable,
      @Param("status") DeviceStatus status,
      @Param("deviceGroupId") Long deviceGroupId);

  boolean existsByCode(String code);

  @Modifying
  @Transactional
  @Query("UPDATE DeviceEntity f SET f.deviceGroup.id = :deviceGroupId WHERE f.id IN :ids")
  void assignToDeviceGroupByDeviceIdAndIds(
      @Param("deviceGroupId") Long deviceGroupId, @Param("ids") List<Long> ids);

  DeviceEntity findByCode(String code);

  @Query(
      "SELECT p FROM DeviceEntity p LEFT JOIN FETCH p.deviceLogs logs WHERE p.id = :deviceId "
          + "AND (:status is null OR logs.status = :status) "
          + "AND (logs.date >= :startDate AND logs.date <= :endDate) "
          + "ORDER BY logs.id DESC") // TODO fix hardcoded 7
  DeviceEntity findWithDeviceLogsInADayById(
      @Param("deviceId") Long deviceId, DeviceStatus status, Date startDate, Date endDate);

  @Query(
      "SELECT d FROM DeviceEntity d LEFT JOIN FETCH d.deviceLogs logs WHERE d.deviceGroup.id = :deviceGroupId "
          + "AND (logs.date BETWEEN :startDate AND :endDate) "
          + "AND (:status is null OR logs.status = :status) "
          + "ORDER BY logs.date DESC")
  List<DeviceEntity> findDeviceLogsByDeviceGroupId(
      @Param("deviceGroupId") Long deviceGroupId,
      @Param("status") DeviceStatus status,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate);

  @Modifying
  @Transactional
  @Query("DELETE FROM DeviceEntity f WHERE f.id IN :ids")
  void deleteByIds(@Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query(
      "UPDATE DeviceEntity f SET f.deviceGroup.id=null WHERE f.deviceGroup.id in :deviceGroupIds")
  void removeDeviceGroupFromDevices(@Param("deviceGroupIds") List<Long> deviceGroupIds);

  @Modifying
  @Transactional
  @Query(
      "UPDATE DeviceEntity f SET f.deviceGroup.id=null WHERE f.id in :deviceIds"
          + " AND f.deviceGroup.id= :deviceGroupId ")
  void removeDevicesFromDeviceGroup(
      @Param("deviceIds") List<Long> deviceIds, @Param("deviceGroupId") Long deviceGroupId);

  List<DeviceEntity> findAllByStatus(DeviceStatus status);

  @Query(
      "SELECT count(d) FROM DeviceEntity d "
          + "WHERE (:userId is null OR d.user.id = :userId) AND d.status = :status")
  Long countDeviceByStatus(@Param("status") DeviceStatus status, @Param("userId") Long userId);

  @Modifying
  @Transactional
  @Query("UPDATE DeviceEntity d SET d.user.id=null WHERE d.user.id in :userIds")
  void removeUserFromDevice(@Param("userIds") List<Long> userIds);
}
