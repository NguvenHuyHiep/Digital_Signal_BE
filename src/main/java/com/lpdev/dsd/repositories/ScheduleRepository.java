package com.lpdev.dsd.repositories;

import com.lpdev.dsd.commons.enums.Status;
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
public interface ScheduleRepository
    extends ScheduleCustomRepository, JpaRepository<ScheduleEntity, Long> {
  @Query(
      "SELECT DISTINCT p FROM ScheduleEntity  p "
          + "LEFT JOIN FETCH p.user u "
          + "LEFT JOIN FETCH u.userRoleMap urm "
          + "LEFT JOIN FETCH urm.role r "
          + "WHERE (:userId is null OR u.id=:userId) AND (p.status = :status AND:keyword is null OR UPPER(p.status) LIKE"
          + " CONCAT('%', UPPER(:keyword), '%'))")
  Page<ScheduleEntity> findAll(Status status, String keyword, Pageable pageable, Long userId);

  @Query("SELECT s FROM ScheduleEntity s JOIN FETCH s.playlists WHERE s.id = :scheduleId")
  ScheduleEntity findWithPlaylistsById(@Param("scheduleId") Long scheduleId);

  @Modifying
  @Transactional
  @Query("DELETE FROM ScheduleEntity s WHERE s.id IN :ids")
  void deleteByIds(@Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query("UPDATE ScheduleEntity s SET s.user.id=null WHERE s.user.id in :userIds")
  void removeUserFromSchedule(@Param("userIds") List<Long> userIds);
}
