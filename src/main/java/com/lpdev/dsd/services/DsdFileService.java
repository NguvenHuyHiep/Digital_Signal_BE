package com.lpdev.dsd.services;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.DsdFile;
import com.lpdev.dsd.models.entities.FileEntity;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface DsdFileService {
  DsdFile getById(Long id);

  Page<DsdFile> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword);

  Page<DsdFile> getFilesByPlaylistId(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      @NonNull Long playlistId);

  DsdFile getByPath(@NonNull String path);

  List<DsdFile> getFilesWithoutCategory();

  List<DsdFile> searchFilesWithoutCategory(String searchInput);

  List<DsdFile> upload(@NonNull MultipartFile[] files);

  DownloadFile downloadByPath(@NonNull String path);

  List<DownloadFile> downloadByPaths(@NonNull List<String> path);

  boolean existsByPath(@NonNull String path);

  void delete(@NonNull String path);

  void deleteFromSource(@NonNull String path);

  void deleteFromDatabase(@NonNull String path);

  void removeUserFromFile(@NonNull List<Long> userIds);

  DsdFile updateStatus(@NonNull Long id, @NonNull Status status);

  void assignToCategoryByCategoryIdAndIds(@NonNull Long categoryId, @NonNull Long id);

  void removeFilesFromCategory(@NonNull List<Long> fileIds, @NonNull Long categoryId);

  void removeCategoryFromFiles(@NonNull List<Long> categoryIds);

  boolean isAllowed(@NonNull FileEntity oldFileEntity);

  boolean isAllowed(@NonNull Long fileId);
}
