package com.lpdev.dsd.repositories;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.entities.FileEntity;
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
public interface DsdFileRepository extends JpaRepository<FileEntity, Long> {
  @Query(
      "SELECT DISTINCT f FROM FileEntity  f "
          + "LEFT JOIN FETCH f.user u "
          + "LEFT JOIN FETCH u.userRoleMap urm "
          + "LEFT JOIN FETCH urm.role r "
          + "WHERE (:userId is null OR u.id = :userId)"
          + " AND (f.status = :status AND :keyword is null OR UPPER(f.name) LIKE CONCAT('%', UPPER(:keyword), '%')) ")
  Page<FileEntity> findAll(Status status, String keyword, Pageable pageable, Long userId);

  @Query(
      "SELECT f FROM FileEntity f LEFT JOIN FETCH f.playListFileMap pfm WHERE pfm.playlist.id = :playlistId "
          + "AND (f.status = :status AND :keyword is null OR UPPER(f.name) LIKE CONCAT('%', UPPER(:keyword), '%'))")
  Page<FileEntity> findFilesByPlaylistId(
      Status status, String keyword, Pageable pageable, @Param("playlistId") Long playlistId);

  @Query(
      "SELECT f FROM FileEntity f WHERE f.user.id = :userId AND "
          + "(:keyword is null OR UPPER(f.name) LIKE CONCAT('%', UPPER(:keyword), '%')) ")
  Page<FileEntity> findAllByUserId(String keyword, Long userId, Pageable pageable);

  Optional<FileEntity> findByPath(String path);

  List<FileEntity> findByCategoryIsNull();

  @Query(
      "SELECT f FROM FileEntity f WHERE (:userId is null OR f.user.id = :userId) AND f.category.id IS NULL AND (:searchInput IS NULL OR UPPER(f.path) LIKE  CONCAT('%', UPPER(:searchInput), '%'))")
  List<FileEntity> findByCategoryIsNullAndSearchInput(
      @Param("searchInput") String searchInput, @Param("userId") Long userId);

  @Query(
      "SELECT f FROM FileEntity f WHERE (:userId is null OR f.user.id = :userId) AND f.category.id = :categoryId AND (:searchInput IS NULL OR UPPER(f.path) LIKE CONCAT('%', UPPER(:searchInput), '%'))")
  List<FileEntity> findByCategoryIdAndSearchInput(
      @Param("categoryId") Long categoryId,
      @Param("searchInput") String searchInput,
      @Param("userId") Long userId);

  Page<FileEntity> findByName(String fileName, Pageable pageable);

  boolean existsByPath(String path);

  void deleteByPath(String path);

  @Modifying
  @Transactional
  @Query("UPDATE FileEntity f SET f.user.id=null WHERE f.user.id in :userIds")
  void removeUserFromFile(@Param("userIds") List<Long> userIds);

  @Modifying
  @Transactional
  @Query("UPDATE FileEntity f SET f.category.id = :categoryId WHERE f.id = :id")
  void assignToCategoryByCategoryIdAndIds(
      @Param("categoryId") Long categoryId, @Param("id") Long id);

  @Modifying
  @Transactional
  @Query(
      "UPDATE FileEntity f SET f.category.id=null WHERE f.id in :fileIds"
          + " AND f.category.id = :categoryId")
  void removeFilesFromCategory(
      @Param("fileIds") List<Long> fileIds, @Param("categoryId") Long categoryId);

  @Modifying
  @Transactional
  @Query("UPDATE FileEntity f SET f.category.id=null WHERE f.category.id in :categoryIds")
  void removeCategoryFromFiles(@Param("categoryIds") List<Long> categoryIds);

  @Query("SELECT f.id FROM FileEntity f WHERE f.category.id IN (:categoryIds)")
  List<Long> findFileIdsByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

  @Query("SELECT f FROM FileEntity f WHERE f.category.id = :categoryId")
  List<FileEntity> findFilesByCategoryId(@Param("categoryId") Long categoryId);
}
