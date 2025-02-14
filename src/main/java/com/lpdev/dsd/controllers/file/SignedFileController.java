package com.lpdev.dsd.controllers.file;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.commons.utils.DsdUtils;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.models.DownloadFile;
import com.lpdev.dsd.models.dtos.DsdFile;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.DsdFileService;
import com.lpdev.dsd.services.PlaylistFileMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/signed/file")
@Tag(name = "Signed-File API")
public class SignedFileController {

  private final DsdFileService dsdFileService;
  private final PlaylistFileMapService playlistFileMapService;

  @Operation(
      summary = "Get all File with pagination",
      description = "Returns all File with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  protected ResponseEntity<BaseOutput<List<DsdFile>>> getByPaging(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {

    Page<DsdFile> DsdFilePage =
        dsdFileService.getByPaging(page, size, sortBy, sortDirection, keyword);
    BaseOutput<List<DsdFile>> response =
        BaseOutput.<List<DsdFile>>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .totalPages(DsdFilePage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(DsdFilePage.getTotalElements())
            .data(DsdFilePage.getContent())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get all file in the playlist with pagination",
      description = "Returns all file in the playlist with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/playlist/{id}")
  public ResponseEntity<BaseOutput<List<DsdFile>>> getFilesByPlaylistId(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {
    Page<DsdFile> pageFiles =
        dsdFileService.getFilesByPlaylistId(page, size, sortBy, sortDirection, keyword, id);
    BaseOutput<List<DsdFile>> response =
        BaseOutput.<List<DsdFile>>builder()
            .message(HttpStatus.OK.toString())
            .currentPage(page)
            .pageSize(size)
            .totalPages(pageFiles.getTotalPages())
            .total(pageFiles.getTotalElements())
            .status(ResponseStatus.SUCCESS)
            .data(pageFiles.getContent())
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get a file by Path", description = "Returns a File by Path")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/{id}")
  public ResponseEntity<BaseOutput<DsdFile>> getById(
      @PathVariable @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE) Long id) {
    if (id <= 0) {
      BaseOutput<DsdFile> response =
          BaseOutput.<DsdFile>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    DsdFile dsdFile = dsdFileService.getById(id);
    return ResponseEntity.ok(
        BaseOutput.<DsdFile>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(dsdFile)
            .build());
  }

  @Operation(summary = "Get a file by Path", description = "Returns a File by Path")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/path")
  public ResponseEntity<BaseOutput<DsdFile>> getByPath(
      @RequestParam @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PARAM) String path) {
    if (!StringUtils.hasText(path)) {
      BaseOutput<DsdFile> response =
          BaseOutput.<DsdFile>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PARAM))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    DsdFile dsdFile = dsdFileService.getByPath(path);
    return ResponseEntity.ok(
        BaseOutput.<DsdFile>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(dsdFile)
            .build());
  }

  @Operation(summary = "Download file", description = "Download file by Path")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/download")
  public ResponseEntity<Resource> downloadByPath(
      @RequestParam @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PARAM) String path) {
    if (!StringUtils.hasText(path)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    DsdFile dsdFile = dsdFileService.getByPath(path);
    if (dsdFile == null) {
      throw new DsdCommonException(DsdConstant.ERROR.FILE.NOT_EXIST);
    }

    DownloadFile downloadFile = dsdFileService.downloadByPath(path);
    ByteArrayResource resource = downloadFile.getResource();
    return ResponseEntity.ok()
        .headers(DsdUtils.getHeadersForDownload(downloadFile.getName()))
        .contentLength(resource.contentLength())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
  }

  @PostMapping("/upload")
  @Operation(
      summary = "Upload files",
      description = "Uploads multiple files and returns the uploaded file details.")
  public ResponseEntity<BaseOutput<List<DsdFile>>> upload(
      @Parameter(
              description = "Files to upload",
              required = true,
              content =
                  @Content(
                      mediaType = "multipart/form-data",
                      array = @ArraySchema(schema = @Schema(type = "file", format = "binary"))))
          @RequestParam("files")
          MultipartFile[] files) {
    if (files == null || files.length == 0) {
      BaseOutput<List<DsdFile>> response =
          BaseOutput.<List<DsdFile>>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PARAM))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    List<DsdFile> createdFiles = dsdFileService.upload(files);
    if (createdFiles == null) {
      BaseOutput<List<DsdFile>> response =
          BaseOutput.<List<DsdFile>>builder().status(ResponseStatus.FAILED).build();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    return ResponseEntity.ok(
        BaseOutput.<List<DsdFile>>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(createdFiles)
            .build());
  }

  @Operation(
      summary = "Update status of file",
      description = "Update status of file to ACTIVE OR INACTIVE")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update the file status"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{fileId}/update-status/{status}")
  public ResponseEntity<BaseOutput<DsdFile>> updateStatus(
      @PathVariable("fileId") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long fileId,
      @PathVariable("status") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          String status) {
    if (status == null || org.apache.commons.lang3.StringUtils.isBlank(status)) {
      BaseOutput<DsdFile> response =
          BaseOutput.<DsdFile>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    DsdFile updatedFile = dsdFileService.updateStatus(fileId, Status.parse(status));
    return ResponseEntity.ok(
        BaseOutput.<DsdFile>builder()
            .status(ResponseStatus.SUCCESS)
            .message(HttpStatus.OK.toString())
            .data(updatedFile)
            .build());
  }

  @Operation(
      summary = "Assign file to playlist",
      description = "Return Assigned file to playlist by id and playlist id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully assign file"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{fileId}/playlists")
  public ResponseEntity<BaseOutput<DsdFile>> assignFileByPlaylists(
      @PathVariable("fileId") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long fileId,
      @PathVariable("playlistIds")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          List<Long> playlistIds) {
    if (fileId == null || playlistIds == null) {
      BaseOutput<DsdFile> response =
          BaseOutput.<DsdFile>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    playlistFileMapService.assignByPlaylistIdsAndFileIds(playlistIds, List.of(fileId));
    return ResponseEntity.ok(
        BaseOutput.<DsdFile>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(dsdFileService.getById(fileId))
            .build());
  }

  @Operation(summary = "Remove files from a playlist ", description = "Return removed file")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully removed file"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/remove/playlist/{playlistId}")
  public ResponseEntity<BaseOutput<String>> removeFilesFromPlaylist(
      @RequestBody @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) List<Long> fileIds,
      @PathVariable("playlistId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long playlistId) {
    if (fileIds == null || playlistId == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .message(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    playlistFileMapService.removeByPlaylistIdsAndFileIds(List.of(playlistId), fileIds);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @Operation(
      operationId = "assignToCategoryByCategoryIdAndIds",
      summary = "Assign file to category",
      description = "Return Assigned file to category by id and category id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully assign file"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{fileId}/category/{categoryId}")
  public ResponseEntity<BaseOutput<DsdFile>> assignToCategoryByCategoryIdAndIds(
      @PathVariable("fileId") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long fileId,
      @PathVariable("categoryId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long categoryId) {
    if (fileId == null || categoryId == null) {
      BaseOutput<DsdFile> response =
          BaseOutput.<DsdFile>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    dsdFileService.assignToCategoryByCategoryIdAndIds(categoryId, fileId);
    return ResponseEntity.ok(
        BaseOutput.<DsdFile>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(dsdFileService.getById(fileId))
            .build());
  }

  @Operation(
      operationId = "removeFilesFromCategory",
      summary = "Remove files from a category ",
      description = "Return removed file")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully removed file"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/remove/category/{categoryId}")
  public ResponseEntity<BaseOutput<String>> removeFilesFromCategory(
      @RequestBody @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) List<Long> fileIds,
      @PathVariable("categoryId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long categoryId) {
    if (fileIds == null || categoryId == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .message(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    dsdFileService.removeFilesFromCategory(fileIds, categoryId);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
