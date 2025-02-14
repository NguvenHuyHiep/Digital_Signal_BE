package com.lpdev.dsd.repositories;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.entities.CategoryEntity;
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
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
  @Query(
      "SELECT DISTINCT c FROM CategoryEntity  c "
          + "LEFT JOIN FETCH c.user u "
          + "LEFT JOIN FETCH u.userRoleMap urm "
          + "LEFT JOIN FETCH urm.role r "
          + "WHERE (:userId is null OR u.id = :userId)"
          + " AND (c.status = :status AND :keyword is null OR UPPER(c.name) LIKE CONCAT('%', UPPER(:keyword), '%')) ")
  Page<CategoryEntity> findAll(
      Status status, String keyword, Pageable pageable, @Param("userId") Long userId);

  @Query(
      "SELECT c FROM CategoryEntity c WHERE  (:userId is null OR c.user.id = :userId) "
          + "AND (:searchInput IS NULL OR UPPER(c.name) LIKE  CONCAT('%', UPPER(:searchInput), '%')) "
          + "AND c.parent.id is null")
  List<CategoryEntity> findCategoriesBySearchInput(
      @Param("searchInput") String searchInput, @Param("userId") Long userId);

  @Query(
      "SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.files f WHERE (:searchInput IS NULL OR UPPER(c.name) LIKE CONCAT('%', UPPER(:searchInput), '%')) "
          + "OR f.category.id = c.id AND c.parent.id is null AND (:searchInput IS NULL OR UPPER(f.path) LIKE CONCAT('%', UPPER(:searchInput), '%'))")
  List<CategoryEntity> findCategoriesAndFilesBySearchInput(
      @Param("searchInput") String searchInput);

  @Query("SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.files WHERE c.id = :categoryId")
  CategoryEntity findWithFilesById(@Param("categoryId") Long categoryId);

  @Modifying
  @Transactional
  @Query("DELETE FROM CategoryEntity c WHERE c.id IN :ids")
  void deleteByIds(@Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query(
      "UPDATE CategoryEntity c SET c.parent.id = :parentCategoryId WHERE c.id = :childCategoryId")
  void assignChildCategoryToParentCategory(
      @Param("childCategoryId") Long childCategoryId,
      @Param("parentCategoryId") Long parentCategoryId);

  @Modifying
  @Transactional
  @Query("UPDATE CategoryEntity c SET c.parent.id = null WHERE c.parent.id in :ids")
  void deleteParentCategory(@Param("ids") List<Long> ids);

  @Modifying
  @Transactional
  @Query(
      "UPDATE CategoryEntity c SET c.parent.id=null WHERE c.parent.id = :parentCategoryId"
          + " AND c.id in :childrenCategoryIds")
  void removeChildrenCategoryFromParentCategory(
      @Param("childrenCategoryIds") List<Long> childrenCategoryIds,
      @Param("parentCategoryId") Long parentCategoryId);

  @Query(
      "SELECT c FROM CategoryEntity c WHERE (:userId is null OR c.user.id = :userId) AND c.parent.id = :parentId  AND (:searchInput IS NULL OR UPPER(c.name) LIKE CONCAT('%', UPPER(:searchInput), '%'))")
  List<CategoryEntity> findByChildCategory(
      @Param("parentId") Long parentId,
      @Param("searchInput") String searchInput,
      @Param("userId") Long userId);
}
