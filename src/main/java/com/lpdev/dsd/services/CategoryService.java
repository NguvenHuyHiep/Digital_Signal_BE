package com.lpdev.dsd.services;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.dtos.Category;
import com.lpdev.dsd.models.dtos.Tree;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;

public interface CategoryService {
  Page<Category> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword);

  List<Tree> getCategoryTree(@NonNull String searchInput);

  List<Tree> getCategoryChildren(@NonNull Long categoryId, String searchInput);

  Category getById(Long id);

  Category save(Category category);

  Category update(@NonNull Category category);

  void delete(Long id);

  void deleteByIds(List<Long> ids);

  Category getWithFileById(@NonNull Long id);

  Category assignFiles(@NonNull Long id, @NonNull Long fileId);

  void assignChildCategoryToParentCategory(
      @NonNull Long parentCategoryId, @NonNull Long subId, boolean isFile);

  void removeFiles(@NonNull Long id, @NonNull List<Long> fileIds);

  void removeChildCategoryFromParentCategory(
      @NonNull Long parentCategoryId, @NonNull List<Long> childCategoryIds);

  Category updateStatus(@NonNull Long id, @NonNull Status status);
}
