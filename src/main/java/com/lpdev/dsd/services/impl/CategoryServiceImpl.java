package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.components.CategoryMapper;
import com.lpdev.dsd.components.DsdFileMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.dtos.Category;
import com.lpdev.dsd.models.dtos.Tree;
import com.lpdev.dsd.models.entities.CategoryEntity;
import com.lpdev.dsd.models.entities.FileEntity;
import com.lpdev.dsd.models.entities.UserEntity;
import com.lpdev.dsd.repositories.CategoryRepository;
import com.lpdev.dsd.repositories.DsdFileRepository;
import com.lpdev.dsd.repositories.UserRepository;
import com.lpdev.dsd.services.CategoryService;
import com.lpdev.dsd.services.DsdFileService;
import com.lpdev.dsd.services.UserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CategoryServiceImpl implements CategoryService {
  @Lazy private final CategoryRepository categoryRepository;
  @Lazy private final CategoryMapper categoryMapper;
  @Lazy private final DsdFileRepository dsdFileRepository;
  @Lazy private final DsdFileService dsdFileService;
  @Lazy private final DsdFileMapper dsdFileMapper;
  @Lazy private final UserService userService;
  @Lazy private final UserRepository userRepository;

  @Override
  public Page<Category> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword) {

    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

    return categoryRepository
        .findAll(Status.ACTIVE, keyword, pageable, getUserId())
        .map(categoryMapper::toDTOWithFiles);
  }

  @Override
  public List<Tree> getCategoryTree(@NonNull String searchInput) {
    log.info("getCategoryTree with searchInput: {}", searchInput);
    List<Tree> categoryTreeList =
        categoryRepository.findCategoriesBySearchInput(searchInput.trim(), getUserId()).stream()
            .map(categoryMapper::toDTO)
            .map(
                c ->
                    Tree.builder()
                        .key(c.getId() + "-" + c.getName())
                        .title(c.getName() + "-" + c.getDescription())
                        .isLeaf(false)
                        .build())
            .collect(Collectors.toList());

    Tree customCategoryTree =
        Tree.builder().key("0-common.uncategory").title("common.uncategory").isLeaf(false).build();
    categoryTreeList.add(customCategoryTree);
    return categoryTreeList;
  }

  @Override
  public List<Tree> getCategoryChildren(@NonNull Long categoryId, String searchInput) {
    log.info("getCategoryChildren with categoryId: {}, searchInput: {}", categoryId, searchInput);
    String keyword = searchInput == null || searchInput.trim().isEmpty() ? "" : searchInput;
    if (categoryId == 0) {
      // get uncategory files
      return dsdFileService.searchFilesWithoutCategory(keyword).stream()
          .map(
              f ->
                  Tree.builder()
                      .key(f.getId() + "-" + f.getPath())
                      .title(f.getPath())
                      .isLeaf(true)
                      .build())
          .collect(Collectors.toList());
    } else if (categoryId > 0) {
      List<Tree> result = new ArrayList<>();
      List<FileEntity> dsdFiles =
          dsdFileRepository.findByCategoryIdAndSearchInput(categoryId, keyword, getUserId());
      result.addAll(
          dsdFiles.stream()
              .map(dsdFileMapper::toDTO)
              .map(
                  f ->
                      Tree.builder()
                          .key(f.getId() + "-" + f.getPath())
                          .title(f.getPath())
                          .isLeaf(true)
                          .build())
              .toList());

      List<CategoryEntity> childCategories =
          categoryRepository.findByChildCategory(categoryId, searchInput, getUserId());
      result.addAll(
          childCategories.stream()
              .map(categoryMapper::toDTO)
              .map(
                  f ->
                      Tree.builder()
                          .key(f.getId() + "-" + f.getName())
                          .title(f.getName() + "-" + f.getDescription())
                          .isLeaf(false)
                          .build())
              .toList());

      return result;
    }
    return Collections.emptyList();
  }

  @Override
  public Category getById(Long id) {
    CategoryEntity oldCategoryEntity =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));

    if (!isAllowed(oldCategoryEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return categoryMapper.toDTO(oldCategoryEntity);
  }

  @Override
  public Category save(Category category) {
    CategoryEntity categoryEntity = categoryMapper.toEntity(category);
    String userEmail = userService.getAuthenticatedUserEmail();
    userRepository.findByEmail(userEmail).ifPresent(categoryEntity::setUser);
    return Optional.of(categoryEntity)
        .map(e -> e.setStatus(Status.ACTIVE))
        .map(categoryRepository::save)
        .map(categoryMapper::toDTO)
        .orElse(null);
  }

  @Override
  public Category update(@NonNull Category category) {
    CategoryEntity oldCategoryEntity =
        categoryRepository
            .findById(category.getId())
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));

    if (!isAllowed(oldCategoryEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return Optional.of(oldCategoryEntity)
        .map(
            odg ->
                odg.toBuilder()
                    .name(category.getName())
                    .description(category.getDescription())
                    .build())
        .map(categoryRepository::save)
        .map(categoryMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));
  }

  @Override
  public void delete(Long id) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    if (id <= 0) {
      throw new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST);
    }
    categoryRepository.deleteById(id);
    dsdFileService.removeCategoryFromFiles(List.of(id));
    categoryRepository.deleteParentCategory(List.of(id));
  }

  @Override
  public void deleteByIds(List<Long> ids) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    if (ids == null || ids.isEmpty()) {
      throw new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST);
    }
    categoryRepository.deleteByIds(ids);
    dsdFileService.removeCategoryFromFiles(ids);
    categoryRepository.deleteParentCategory(ids);
  }

  @Override
  public Category getWithFileById(@NonNull Long id) {
    return Optional.ofNullable(categoryRepository.findWithFilesById(id))
        .map(categoryMapper::toDTOWithFiles)
        .orElse(null);
  }

  @Override
  public Category assignFiles(@NonNull Long id, @NonNull Long fileId) {
    if (!categoryRepository.existsById(id)) {
      throw new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST);
    }
    dsdFileRepository.assignToCategoryByCategoryIdAndIds(id, fileId);

    return getWithFileById(id);
  }

  @Override
  public void assignChildCategoryToParentCategory(
      @NonNull Long parentCategoryId, @NonNull Long subId, boolean isFile) {

    if (parentCategoryId == 0) {
      if (isFile) {
        FileEntity fileEntity =
            dsdFileRepository
                .findById(subId)
                .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST));

        if (!dsdFileService.isAllowed(fileEntity)) {
          throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
        }

        fileEntity.setCategory(null);
        dsdFileRepository.save(fileEntity);
      } else {
        CategoryEntity categoryEntity =
            categoryRepository
                .findById(subId)
                .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));

        if (!isAllowed(categoryEntity)) {
          throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
        }

        categoryEntity.setParent(null);
        categoryRepository.save(categoryEntity);
      }
    } else if (isFile) {
      if (!dsdFileService.isAllowed(subId) || !isAllowed(parentCategoryId)) {
        throw new DsdCommonException(DsdConstant.ERROR.CATEGORY.UPDATE);
      }
      dsdFileRepository.assignToCategoryByCategoryIdAndIds(parentCategoryId, subId);
    } else {
      CategoryEntity parentCategory =
          categoryRepository
              .findById(parentCategoryId)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));
      CategoryEntity childCategoryEntity =
          categoryRepository
              .findById(subId)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));
      if (isChildCategory(parentCategory, childCategoryEntity)
          || !isAllowed(parentCategoryId)
          || !isAllowed(childCategoryEntity)) {
        throw new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_CHILD);
      }
      categoryRepository.assignChildCategoryToParentCategory(subId, parentCategoryId);
    }
  }

  @Override
  public void removeFiles(@NonNull Long id, @NonNull List<Long> fileIds) {
    for (Long fileId : fileIds) {
      FileEntity oldFileEntity =
          dsdFileRepository
              .findById(fileId)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST));

      if (!dsdFileService.isAllowed(oldFileEntity)) {
        throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
      }
      dsdFileService.removeFilesFromCategory(List.of(fileId), id);
    }
  }

  @Override
  public void removeChildCategoryFromParentCategory(
      @NonNull Long parentCategoryId, @NonNull List<Long> childCategoryIds) {
    if (!categoryRepository.existsById(parentCategoryId)) {
      throw new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST);
    }
    categoryRepository.removeChildrenCategoryFromParentCategory(childCategoryIds, parentCategoryId);
  }

  @Override
  public Category updateStatus(@NonNull Long id, @NonNull Status status) {
    CategoryEntity oldCategoryEntity =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));

    if (!isAllowed(oldCategoryEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return categoryRepository
        .findById(id)
        .map(p -> p.toBuilder().status(status).build())
        .map(categoryRepository::save)
        .map(categoryMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));
  }

  private boolean isAllowed(@NonNull CategoryEntity oldCategoryEntity) {
    if (!userService.isAdmin()) {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      return oldCategoryEntity.getUser().getId().equals(loggedUser.getId());
    }
    return true;
  }

  private boolean isAllowed(@NonNull Long categoryId) {
    CategoryEntity oldCategoryEntity =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.CATEGORY.NOT_EXIST));
    return this.isAllowed(oldCategoryEntity);
  }

  private Long getUserId() {
    Long userId;
    if (userService.isAdmin()) {
      userId = null;
    } else {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      userId = loggedUser.getId();
    }
    return userId;
  }

  private boolean isChildCategory(CategoryEntity parentCategory, CategoryEntity childCategory) {
    if (parentCategory == null || childCategory == null) {
      return false;
    }

    if (parentCategory.equals(childCategory)) {
      return true;
    }

    CategoryEntity currentParent = parentCategory.getParent();
    while (currentParent != null) {
      if (currentParent.equals(childCategory)) {
        return true;
      }
      currentParent = currentParent.getParent();
    }

    return false;
  }
}
