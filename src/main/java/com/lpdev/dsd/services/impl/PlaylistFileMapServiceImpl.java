package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.models.dtos.Playlist;
import com.lpdev.dsd.models.entities.FileEntity;
import com.lpdev.dsd.models.entities.PlaylistEntity;
import com.lpdev.dsd.models.entities.PlaylistFileMapEntity;
import com.lpdev.dsd.repositories.DsdFileRepository;
import com.lpdev.dsd.repositories.PlaylistFileMapRepository;
import com.lpdev.dsd.repositories.PlaylistRepository;
import com.lpdev.dsd.services.CategoryService;
import com.lpdev.dsd.services.PlaylistFileMapService;
import com.lpdev.dsd.services.PlaylistService;
import java.util.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class PlaylistFileMapServiceImpl implements PlaylistFileMapService {
  @Lazy @Autowired PlaylistFileMapRepository playlistFileMapRepository;
  @Lazy @Autowired DsdFileRepository dsdFileRepository;
  @Lazy @Autowired PlaylistRepository playlistRepository;
  @Lazy @Autowired PlaylistService playlistService;
  @Lazy @Autowired CategoryService categoryService;

  @Override
  public Playlist assignByPlaylistIdsAndFileIds(
      @NonNull List<Long> playlistIds, @NonNull List<Long> fileIds) {
    List<PlaylistEntity> playlists = playlistRepository.findAllById(playlistIds);
    List<FileEntity> files = dsdFileRepository.findAllById(fileIds);
    files.sort(Comparator.comparingInt(file -> fileIds.indexOf(file.getId())));
    for (PlaylistEntity playlist : playlists) {
      LinkedHashSet<Long> fileOrder = playlist.getFileOrder();
      for (FileEntity file : files) {
        if (playlistFileMapRepository.existsByPlaylistAndFile(
            List.of(playlist.getId()), List.of(file.getId()))) {
          continue;
        }

        fileOrder.add(file.getId());
        PlaylistFileMapEntity playlistFileMap = new PlaylistFileMapEntity();
        playlistFileMap.setPlaylist(playlist);
        playlistFileMap.setFile(file);

        playlistFileMapRepository.save(playlistFileMap);
      }

      playlist.setFileOrder(fileOrder);
    }
    playlistRepository.saveAll(playlists);
    List<Long> updatedPlaylistIds = playlists.stream().map(PlaylistEntity::getId).toList();

    playlistService.updateLastUpdate(updatedPlaylistIds);
    Long playlistId = playlistIds.get(0);
    return playlistService.getWithFilesById(playlistId);
  }

  @Override
  public Playlist assignByPlaylistIdsCategoryIds(
      @NonNull List<Long> playlistIds, @NonNull List<Long> categoryIds) {
    List<Long> fileIds = dsdFileRepository.findFileIdsByCategoryIds(categoryIds);
    return assignByPlaylistIdsAndFileIds(playlistIds, fileIds);
  }

  @Override
  public void removeByPlaylistIdsAndFileIds(
      @NonNull List<Long> playlistIds, @NonNull List<Long> fileIds) {
    if (!playlistIds.isEmpty() && !fileIds.isEmpty()) {
      this.updateFileOrderByPlaylistIdsAndFileIds(playlistIds, fileIds);
    } else if (playlistIds.isEmpty() && !fileIds.isEmpty()) {
      this.updateFileOrderByFileIds(fileIds);
    } else if (!playlistIds.isEmpty()) {
      this.updateFileOrderByPlaylistIds(playlistIds);
    }
  }

  /**
   * Remove file of playlist only
   *
   * @param playlistIds
   * @param fileIds
   */
  private void updateFileOrderByPlaylistIdsAndFileIds(
      @NonNull List<Long> playlistIds, @NonNull List<Long> fileIds) {
    List<Long> fileIdsOfPlaylistEntityToDelete = new ArrayList<>();
    List<PlaylistEntity> playlistEntities = playlistRepository.findAllById(playlistIds);
    for (PlaylistEntity playlistEntity : playlistEntities) {
      LinkedHashSet<Long> fileOrder = playlistEntity.getFileOrder();
      if (fileOrder == null) {
        continue;
      }
      List<Long> fileIdsOfPlaylistEntity =
          playlistEntity.getPlayListFileMap().stream().map(pfm -> pfm.getFile().getId()).toList();
      fileIdsOfPlaylistEntity.forEach(
          fileIdOfPlaylistEntity -> {
            if (fileIds.contains(fileIdOfPlaylistEntity)) {
              fileOrder.remove(fileIdOfPlaylistEntity);
              fileIdsOfPlaylistEntityToDelete.add(fileIdOfPlaylistEntity);
            }
          });
      playlistEntity.setFileOrder(fileOrder);
      playlistEntity.setLastUpdateDate(new Date());
      playlistRepository.save(playlistEntity);
    }
    playlistFileMapRepository.removeByPlaylistIdsAndFileIds(
        playlistIds, fileIdsOfPlaylistEntityToDelete);
  }

  private void updateFileOrderByPlaylistIds(@NonNull List<Long> playlistIds) {
    List<PlaylistEntity> playlistEntities = playlistRepository.findAllById(playlistIds);
    for (PlaylistEntity playlistEntity : playlistEntities) {
      playlistEntity.setFileOrder(new LinkedHashSet<>());
      playlistEntity.setLastUpdateDate(new Date());
      playlistRepository.save(playlistEntity);
    }
    playlistFileMapRepository.removeByPlaylistIds(playlistIds);
  }

  private void updateFileOrderByFileIds(@NonNull List<Long> fileIds) {
    List<FileEntity> fileEntities = dsdFileRepository.findAllById(fileIds);
    for (FileEntity fileEntity : fileEntities) {
      List<PlaylistEntity> playlistEntitiesOfFileEntity =
          fileEntity.getPlayListFileMap().stream().map(PlaylistFileMapEntity::getPlaylist).toList();
      for (PlaylistEntity playlistEntity : playlistEntitiesOfFileEntity) {
        LinkedHashSet<Long> fileOrder = playlistEntity.getFileOrder();
        if (fileOrder == null) {
          continue;
        }
        fileOrder.remove(fileEntity.getId());
        playlistEntity.setFileOrder(fileOrder);
        playlistEntity.setLastUpdateDate(new Date());
        playlistRepository.save(playlistEntity);
      }
    }
    playlistFileMapRepository.removeByFileIds(fileIds);
  }
}
