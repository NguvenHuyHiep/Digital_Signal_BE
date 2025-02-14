package com.lpdev.dsd.repositories;

import com.lpdev.dsd.models.entities.DeviceEntity;
import com.lpdev.dsd.models.entities.DeviceLogEntity;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DeviceLogRepository extends JpaRepository<DeviceLogEntity, Long> {
  @Query(
      "SELECT d FROM DeviceLogEntity d WHERE "
          + "(:keyword is null OR UPPER(d.status) LIKE CONCAT('%', UPPER(:keyword), '%')) ")
  // @Query("SELECT dg FROM DeviceGroupEntity dg WHERE EXISTS " +
  //        "(SELECT d FROM dg.devices d WHERE d.id BETWEEN :startId AND :endId) " +
  //        "AND dg.name LIKE CONCAT('%', :keyword, '%')")
  Page<DeviceLogEntity> findAll(String keyword, Pageable pageable);

  @Query(
      "SELECT d FROM DeviceLogEntity d WHERE d.device.id = :deviceId"
          + " AND (:keyword is null OR UPPER(d.status) LIKE CONCAT('%', UPPER(:keyword), '%'))")
  Page<DeviceLogEntity> findDeviceLogsByDeviceId(
      String keyword, Pageable pageable, @Param("deviceId") Long deviceId);

  @Query("SELECT dl FROM DeviceLogEntity dl WHERE dl.device = :device")
  List<DeviceLogEntity> findByDevice(DeviceEntity device);

  @Query("SELECT dl FROM DeviceLogEntity dl WHERE dl.device = :device ORDER BY dl.date DESC")
  Page<DeviceLogEntity> findLatestByDeviceAndPagingOrderByDate(
      DeviceEntity device, Pageable pageable);

  @Modifying
  @Transactional
  @Query("DELETE FROM DeviceLogEntity d WHERE d.device.id = :deviceId")
  void deleteDeviceLogWhenDeleteDevice(@Param("deviceId") Long deviceId);

  @Query(
      "SELECT d FROM DeviceLogEntity d WHERE d.device.id = :deviceId "
          + "AND  d.date BETWEEN :startDate AND :endDate ORDER BY d.date DESC")
  List<DeviceLogEntity> findByDeviceIdAndDateRange(
      @Param("deviceId") Long deviceId,
      @Param("startDate") Date startDate,
      @Param("endDate") Date endDate);

  @Query(
      "SELECT d FROM DeviceLogEntity d WHERE d.device.id = :deviceId "
          + "AND d.date>CURRENT_DATE -7 ORDER BY d.date DESC")
  List<DeviceLogEntity> findWithDeviceLogsInADayById(@Param("deviceId") Long deviceId);
}
