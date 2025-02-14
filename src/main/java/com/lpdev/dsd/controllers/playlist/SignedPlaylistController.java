package com.lpdev.dsd.controllers.playlist;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.REQUEST;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.dtos.Playlist;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.PlaylistFileMapService;
import com.lpdev.dsd.services.PlaylistService;
import com.lpdev.dsd.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/signed/playlist")
@Tag(name = "Signed-PlayList API")
@SecurityRequirement(name = "Authorization")
public class SignedPlaylistController {

  private final PlaylistService playlistService;

  private final UserService userService;
  private final PlaylistFileMapService playlistFileMapService;

  @Operation(
      summary = "Get all playlists with pagination",
      description = "Returns all playlists with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  public ResponseEntity<BaseOutput<List<Playlist>>> getByPaging(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {
    Page<Playlist> playlistsPage =
        playlistService.getByPaging(page, size, sortBy, sortDirection, keyword);

    BaseOutput<List<Playlist>> response =
        BaseOutput.<List<Playlist>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(playlistsPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(playlistsPage.getTotalElements())
            .data(playlistsPage.getContent())
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get a playlist by id", description = "Returns a playlist as per the id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<BaseOutput<Playlist>> getById(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<Playlist> response =
          BaseOutput.<Playlist>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Playlist playlist = playlistService.getById(id);

    BaseOutput<Playlist> response =
        BaseOutput.<Playlist>builder()
            .message(HttpStatus.OK.toString())
            .data(playlist)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get a playlist containing  the files", description = "Returns a playlist")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}/files")
  public ResponseEntity<BaseOutput<Playlist>> getByIdWithFiles(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<Playlist> response =
          BaseOutput.<Playlist>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    Playlist playlist = playlistService.getWithFilesById(id);
    BaseOutput<Playlist> response =
        BaseOutput.<Playlist>builder()
            .message(HttpStatus.OK.toString())
            .data(playlist)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get a playlist containing device groups",
      description = "Returns a playlist")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}/device-groups")
  public ResponseEntity<BaseOutput<Playlist>> getByIdWithFilesDeviceGroups(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<Playlist> response =
          BaseOutput.<Playlist>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    Playlist playlist = playlistService.getWithDeviceGroupsById(id);
    BaseOutput<Playlist> response =
        BaseOutput.<Playlist>builder()
            .message(HttpStatus.OK.toString())
            .data(playlist)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Create a new playlist", description = "Create a new playlist")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping
  public ResponseEntity<BaseOutput<Playlist>> create(
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) Playlist playlist) {

    if (playlist == null) {
      BaseOutput<Playlist> response =
          BaseOutput.<Playlist>builder()
              .errors(List.of(REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    Playlist createdPlaylist = playlistService.create(playlist);
    BaseOutput<Playlist> response =
        BaseOutput.<Playlist>builder()
            .message(HttpStatus.OK.toString())
            .data(createdPlaylist)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update a playlist by id", description = "Return updated playlist")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update new playlist"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<BaseOutput<Playlist>> update(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) Playlist playlist) {
    if (id <= 0) {
      BaseOutput<Playlist> response =
          BaseOutput.<Playlist>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    playlist.setId(id);
    Playlist createdPlaylist = playlistService.update(playlist);
    BaseOutput<Playlist> response =
        BaseOutput.<Playlist>builder()
            .message(HttpStatus.OK.toString())
            .data(createdPlaylist)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Update status of playlist",
      description = "Update status of playlist to ACITVE OR INACTIVE")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update the playlist status"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{playlistId}/update-status/{status}")
  public ResponseEntity<BaseOutput<Playlist>> updateStatus(
      @PathVariable("playlistId") @NotNull(message = REQUEST.INVALID_PATH_VARIABLE) Long playlistId,
      @PathVariable("status") @NotNull(message = REQUEST.INVALID_PATH_VARIABLE) String status) {
    if (status == null || StringUtils.isBlank(status)) {
      BaseOutput<Playlist> response =
          BaseOutput.<Playlist>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_BODY))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Playlist updatedPlaylist = playlistService.updateStatus(playlistId, Status.parse(status));
    return ResponseEntity.ok(
        BaseOutput.<Playlist>builder()
            .status(ResponseStatus.SUCCESS)
            .message(HttpStatus.OK.toString())
            .data(updatedPlaylist)
            .build());
  }

  @Operation(summary = "Assign files", description = "Assign files")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update new playlist"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}/files")
  public ResponseEntity<BaseOutput<Playlist>> assignFiles(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) List<Long> fileIds) {
    if (id <= 0 || fileIds == null || fileIds.isEmpty()) {

      BaseOutput<Playlist> response =
          BaseOutput.<Playlist>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Playlist assignedPlaylist =
        playlistFileMapService.assignByPlaylistIdsAndFileIds(List.of(id), fileIds);
    BaseOutput<Playlist> response =
        BaseOutput.<Playlist>builder()
            .message(HttpStatus.OK.toString())
            .data(assignedPlaylist)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Assign device groups to playlist ",
      description = "Return Assigned device groups to playlist ")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully assign device groups"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}/device-groups")
  public ResponseEntity<BaseOutput<Playlist>> assignDeviceGroups(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          List<Long> deviceGroupIds) {
    if (id <= 0 || deviceGroupIds == null || deviceGroupIds.isEmpty()) {
      BaseOutput<Playlist> response =
          BaseOutput.<Playlist>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Playlist assignedPlaylist = playlistService.assignDeviceGroups(id, deviceGroupIds);
    return ResponseEntity.ok(
        BaseOutput.<Playlist>builder()
            .message(HttpStatus.OK.toString())
            .data(assignedPlaylist)
            .status(ResponseStatus.SUCCESS)
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
  @PutMapping("/remove/{id}/files")
  public ResponseEntity<BaseOutput<String>> removeFiles(
      @RequestBody @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) List<Long> fileIds,
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (fileIds == null || id == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .message(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    playlistFileMapService.removeByPlaylistIdsAndFileIds(List.of(id), fileIds);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @Operation(
      summary = "Remove device groups from a playlist ",
      description = "Return removed device groups")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully removed device groups"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/remove/{id}/device-groups")
  public ResponseEntity<BaseOutput<String>> removeDeviceGroups(
      @RequestBody @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          List<Long> deviceGroupIds,
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (deviceGroupIds == null || id == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .message(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    playlistService.removeDeviceGroups(id, deviceGroupIds);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
