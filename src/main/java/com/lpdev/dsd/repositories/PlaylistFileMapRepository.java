package com.lpdev.dsd.repositories;

import com.lpdev.dsd.models.entities.PlaylistFileMapEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PlaylistFileMapRepository extends JpaRepository<PlaylistFileMapEntity, Long> {
  @Modifying
  @Transactional
  @Query(
      "DELETE FROM PlaylistFileMapEntity p "
          + "WHERE  p.playlist.id IN :playlistIds "
          + "AND p.file.id IN :fileIds")
  void removeByPlaylistIdsAndFileIds(
      @Param("playlistIds") List<Long> playlistIds, @Param("fileIds") List<Long> fileIds);

  @Modifying
  @Transactional
  @Query("DELETE FROM PlaylistFileMapEntity p WHERE p.file.id IN :fileIds")
  void removeByFileIds(@Param("fileIds") List<Long> fileIds);

  @Modifying
  @Transactional
  @Query("DELETE FROM PlaylistFileMapEntity p WHERE p.playlist.id IN :playlistIds")
  void removeByPlaylistIds(@Param("playlistIds") List<Long> playlistIds);

  @Query(
      "SELECT CASE WHEN COUNT(pf) > 0 THEN true ELSE false END "
          + "FROM PlaylistFileMapEntity pf "
          + "WHERE pf.playlist.id IN :playlistIds AND pf.file.id IN :fileIds")
  boolean existsByPlaylistAndFile(List<Long> playlistIds, List<Long> fileIds);

  @Query("SELECT pfm FROM PlaylistFileMapEntity pfm WHERE pfm.playlist.id = :playlistId")
  List<PlaylistFileMapEntity> findByPlaylistId(@Param("playlistId") Long playlistId);
}
