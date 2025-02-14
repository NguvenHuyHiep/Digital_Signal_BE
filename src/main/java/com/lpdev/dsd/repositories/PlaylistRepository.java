package com.lpdev.dsd.repositories;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.entities.PlaylistEntity;
import com.lpdev.dsd.models.entities.ScheduleEntity;
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
public interface PlaylistRepository extends JpaRepository<PlaylistEntity, Long> {
  @Query(
      "SELECT p FROM PlaylistEntity  p "
          + "LEFT JOIN FETCH p.user u "
          + "LEFT JOIN FETCH u.userRoleMap urm "
          + "LEFT JOIN FETCH urm.role r "
          + "WHERE (:userId is null OR u.id=:userId) AND (p.status = :status AND:keyword is "
          + "null OR UPPER(p.status) LIKE"
          + " CONCAT('%', UPPER(:keyword), '%'))")
  Page<PlaylistEntity> findAll(Status status, String keyword, Pageable pageable, Long userId);

  @Query(
      "SELECT p FROM PlaylistEntity p LEFT JOIN FETCH p.playListFileMap pfm LEFT JOIN FETCH pfm.file WHERE p.id = :playlistId")
  PlaylistEntity findWithFilesById(@Param("playlistId") Long playlistId);

  @Query("SELECT p FROM PlaylistEntity p LEFT JOIN FETCH p.deviceGroups WHERE p.id = :playlistId")
  PlaylistEntity findWithDeviceGroupsById(@Param("playlistId") Long playlistId);

  @Query(
      "SELECT p FROM PlaylistEntity p  "
          + "WHERE (:userId is null OR p.user.id = :userId) ORDER BY p.id DESC LIMIT 1")
  PlaylistEntity findFirstByOrderByIdDesc(Long userId);

  @Query(
      "SELECT p FROM PlaylistEntity p LEFT JOIN FETCH p.playListFileMap pfm LEFT JOIN FETCH pfm.file WHERE p.schedule = :schedule")
  List<PlaylistEntity> findWithFilesBySchedule(@Param("schedule") ScheduleEntity schedule);

  @Modifying
  @Transactional
  @Query("UPDATE PlaylistEntity p SET p.schedule.id = :scheduleId WHERE p.id IN :ids")
  void assignToScheduleByScheduleIdsAndIds(
      @Param("scheduleId") Long scheduleId, @Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query("DELETE FROM PlaylistEntity f WHERE f.id IN :ids")
  void deleteByIds(@Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query("UPDATE PlaylistEntity p SET p.schedule.id=null WHERE p.schedule.id in :scheduleIds")
  void removeScheduleFromPlaylist(@Param("scheduleIds") List<Long> scheduleIds);

  @Modifying
  @Transactional
  @Query("UPDATE PlaylistEntity p SET p.user.id=null WHERE p.user.id in :userIds")
  void removeUserFromPlaylist(@Param("userIds") List<Long> userIds);

  @Modifying
  @Transactional
  @Query("UPDATE PlaylistEntity f SET f.lastUpdateDate = CURRENT_TIMESTAMP WHERE f.id IN :ids")
  void lastUpdateDate(@Param("ids") List<Long> ids);

  @Query(
      "SELECT count(p) from PlaylistEntity p " + "WHERE (:userId is null OR p.user.id = :userId)")
  Long countByUserId(Long userId);
}
