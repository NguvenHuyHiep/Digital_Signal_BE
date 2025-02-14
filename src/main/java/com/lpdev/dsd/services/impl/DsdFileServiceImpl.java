package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.commons.utils.DsdUtils;
import com.lpdev.dsd.components.DsdFileMapper;
import com.lpdev.dsd.components.UserMapper;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.DsdFile;
import com.lpdev.dsd.models.entities.*;
import com.lpdev.dsd.repositories.*;
import com.lpdev.dsd.services.*;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
public class DsdFileServiceImpl implements DsdFileService {
  @Lazy @Autowired private DsdFileRepository dsdFileRepository;
  @Lazy @Autowired private DsdFileMapper dsdFileMapper;
  @Lazy @Autowired private UserService userService;
  @Lazy @Autowired private UserRepository userRepository;
  @Lazy @Autowired private UserMapper userMapper;
  @Lazy @Autowired private MinioService minioService;
  @Lazy @Autowired private PlaylistService playlistService;
  @Lazy @Autowired private PlaylistFileMapRepository playlistFileMapRepository;
  @Lazy @Autowired private PlaylistRepository playlistRepository;
  @Lazy @Autowired private CategoryRepository categoryRepository;
  @Lazy @Autowired private PlaylistFileMapService playlistFileMapService;

  @Value("${dsd-config.minio.bucket}")
  private String bucketName;

  @Override
  public DsdFile getById(Long id) {
    FileEntity oldFileEntity =
        dsdFileRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST));
    if (!isAllowed(oldFileEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return dsdFileMapper.toDTO(oldFileEntity);
  }

  @Override
  public Page<DsdFile> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword) {
    Long userId;
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
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
    return dsdFileRepository
        .findAll(Status.ACTIVE, keyword, pageable, userId)
        .map(dsdFileMapper::toDTO);
  }

  @Override
  public Page<DsdFile> getFilesByPlaylistId(
      int pageNo,
      int pageSize,
      String sortBy,
      String sortDirection,
      String keyword,
      @NonNull Long playlistId) {
    if (!userService.isAdmin()) {
      PlaylistEntity oldPlaylistEntity =
          playlistRepository
              .findById(playlistId)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      if (!oldPlaylistEntity.getUser().getId().equals(loggedUser.getId())) {
        throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
      }
    }
    Pageable pageable =
        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    return dsdFileRepository
        .findFilesByPlaylistId(Status.ACTIVE, keyword, pageable, playlistId)
        .map(dsdFileMapper::toDTO);
  }

  @Override
  public DsdFile getByPath(@NonNull String path) {
    FileEntity oldFileEntity =
        dsdFileRepository
            .findByPath(path)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST));
    if (!isAllowed(oldFileEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return dsdFileMapper.toDTO(oldFileEntity);
  }

  @Override
  public List<DsdFile> getFilesWithoutCategory() {
    return dsdFileRepository.findByCategoryIsNull().stream().map(dsdFileMapper::toDTO).toList();
  }

  @Override
  public List<DsdFile> searchFilesWithoutCategory(String searchInput) {
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
    return dsdFileRepository.findByCategoryIsNullAndSearchInput(searchInput, userId).stream()
        .map(dsdFileMapper::toDTO)
        .toList();
  }

  @Override
  @Transactional
  public List<DsdFile> upload(@NonNull MultipartFile[] files) {
    String userEmail = userService.getAuthenticatedUserEmail();
    UserEntity userEntity = userRepository.findByEmail(userEmail).orElse(null);
    return Arrays.stream(files)
        .filter(f -> !dsdFileRepository.existsByPath(f.getOriginalFilename()))
        .map(
            vmf -> {
              String mimeType = vmf.getContentType();
              String fileName = vmf.getOriginalFilename();
              DsdFile toStoreFile =
                  DsdFile.builder()
                      .fileType(mimeType)
                      .name(fileName)
                      .path(fileName)
                      .createDate(new Date())
                      .status(Status.ACTIVE)
                      .user(userMapper.toDTO(userEntity))
                      .build();
              DsdFile storedFile =
                  Optional.of(vmf)
                      .map(e -> dsdFileMapper.toEntity(toStoreFile))
                      .map(e -> e.setStatus(Status.ACTIVE))
                      .map(e -> e.setUser(userEntity))
                      .map(dsdFileRepository::save)
                      .map(dsdFileMapper::toDTO)
                      .orElse(null);
              if (storedFile != null) {
                log.info("uploaded file to db, file: {}", storedFile.getPath());
                this.minioService.uploadToMinio(bucketName, vmf);
                return storedFile;
              }
              log.info("stored file to DB is failed, removed, file: {}", fileName);
              return null;
            })
        .filter(Objects::nonNull)
        .toList();
  }

  @Override
  public DownloadFile downloadByPath(@NonNull String path) {
    DsdFile dsdFile =
        dsdFileRepository
            .findByPath(path)
            .map(dsdFileMapper::toDTO)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST));
    log.info("downloading file: {}", dsdFile.getPath());
    try (InputStream inputStream = this.minioService.downloadFromMinio(dsdFile.getPath())) {
      return DownloadFile.builder().name(path).resource(DsdUtils.parse(inputStream)).build();
    } catch (Exception e) {
      log.error("ERROR parse file: {}", path);
      throw new DsdCommonException(DsdConstant.ERROR.FILE.CORRUPTED);
    }
  }

  @Override
  public List<DownloadFile> downloadByPaths(@NonNull List<String> paths) {
    return paths.stream()
        .filter(Objects::nonNull)
        .map(dsdFileRepository::findByPath)
        .map(feOpt -> feOpt.orElse(null))
        .filter(Objects::nonNull)
        .map(
            fe -> {
              try (InputStream inputStream = this.minioService.downloadFromMinio(fe.getPath())) {
                return DownloadFile.builder()
                    .name(fe.getPath())
                    .resource(DsdUtils.parse(inputStream))
                    .build();
              } catch (Exception e) {
                log.error("ERROR parse file: {}", fe);
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsByPath(@NonNull String path) {
    return dsdFileRepository.existsByPath(path);
  }

  @Override
  @Transactional
  public void delete(@NonNull String path) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }

    FileEntity fileEntity =
        dsdFileRepository
            .findByPath(path)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST));

    Long fileId = fileEntity.getId();
    playlistFileMapService.removeByPlaylistIdsAndFileIds(Collections.emptyList(), List.of(fileId));

    this.deleteFromDatabase(path);
    this.deleteFromSource(path);
  }

  @Override
  public void deleteFromSource(@NonNull String path) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    this.minioService.deleteFromMinio(path);
  }

  @Override
  public void deleteFromDatabase(@NonNull String path) {
    if (!userService.isAdmin()) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    DsdFile file = dsdFileRepository.findByPath(path).map(dsdFileMapper::toDTO).orElse(null);
    if (file == null) {
      log.info("file is null, stop delete file");
      throw new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST);
    }

    dsdFileRepository.deleteByPath(path);
    log.info("deleted from DB file: {}", file.getName());
  }

  @Override
  public void removeUserFromFile(@NonNull List<Long> userIds) {
    dsdFileRepository.removeUserFromFile(userIds);
  }

  @Override
  public DsdFile updateStatus(@NonNull Long id, @NonNull Status status) {
    FileEntity oldFileEntity =
        dsdFileRepository
            .findById(id)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST));
    if (!isAllowed(oldFileEntity)) {
      throw new DsdCommonException(DsdConstant.ERROR.ROLE.NOT_ALLOWED);
    }
    return dsdFileRepository
        .findById(id)
        .map(f -> f.toBuilder().status(status).build())
        .map(dsdFileRepository::save)
        .map(dsdFileMapper::toDTO)
        .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.PLAYLIST.NOT_EXIST));
  }

  @Override
  public void assignToCategoryByCategoryIdAndIds(@NonNull Long categoryId, @NonNull Long id) {
    dsdFileRepository.assignToCategoryByCategoryIdAndIds(categoryId, id);
  }

  @Override
  public void removeFilesFromCategory(@NonNull List<Long> fileIds, @NonNull Long categoryId) {
    dsdFileRepository.removeFilesFromCategory(fileIds, categoryId);
  }

  @Override
  public void removeCategoryFromFiles(@NonNull List<Long> categoryIds) {
    dsdFileRepository.removeCategoryFromFiles(categoryIds);
  }

  @Override
  public boolean isAllowed(@NonNull FileEntity oldFileEntity) {
    if (!userService.isAdmin()) {
      String userEmail = userService.getAuthenticatedUserEmail();
      UserEntity loggedUser =
          userRepository
              .findByEmail(userEmail)
              .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.USER.NOT_EXIST));
      return oldFileEntity.getUser().getId().equals(loggedUser.getId());
    }
    return true;
  }

  @Override
  public boolean isAllowed(@NonNull Long fileId) {
    FileEntity oldFileEntity =
        dsdFileRepository
            .findById(fileId)
            .orElseThrow(() -> new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST));
    return this.isAllowed(oldFileEntity);
  }
}
