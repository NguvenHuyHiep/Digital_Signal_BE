package com.lpdev.dsd.controllers.schedule;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.REQUEST;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.dtos.Schedule;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/signed/schedule")
@Tag(name = "Signed-Schedule API")
public class SignedScheduleController {
  private final ScheduleService scheduleService;

  @Operation(
      summary = "Get all Schedule with pagination",
      description = "Returns all Schedule with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  public ResponseEntity<BaseOutput<List<Schedule>>> getByPaging(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {
    Page<Schedule> schedulePage =
        scheduleService.getByPaging(page, size, sortBy, sortDirection, keyword);

    BaseOutput<List<Schedule>> response =
        BaseOutput.<List<Schedule>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(schedulePage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(schedulePage.getTotalElements())
            .data(schedulePage.getContent())
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get Schedule with pagination by id", description = "Returns Schedule by ID")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/{id}")
  public ResponseEntity<BaseOutput<Schedule>> getById(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<Schedule> response =
          BaseOutput.<Schedule>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Schedule schedule = scheduleService.getById(id);
    BaseOutput<Schedule> response =
        BaseOutput.<Schedule>builder()
            .message(HttpStatus.OK.toString())
            .data(schedule)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get playlist in the schedule by id",
      description = "Returns playlist in the schedule")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}/playlists")
  public ResponseEntity<BaseOutput<Schedule>> getWithPlaylistsById(
      @PathVariable("id") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<Schedule> response =
          BaseOutput.<Schedule>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Schedule schedule = scheduleService.getWithPlaylistsWithFilesById(id);
    return ResponseEntity.ok(
        BaseOutput.<Schedule>builder()
            .message(HttpStatus.OK.toString())
            .data(schedule)
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @Operation(summary = "Create Schedule", description = "Returns created Schedule")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @PostMapping
  public ResponseEntity<BaseOutput<Schedule>> create(
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) Schedule schedule) {
    if (schedule == null) {
      BaseOutput<Schedule> response =
          BaseOutput.<Schedule>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_BODY))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Schedule createdSchedule = scheduleService.create(schedule);
    BaseOutput<Schedule> response =
        BaseOutput.<Schedule>builder()
            .message(HttpStatus.OK.toString())
            .data(createdSchedule)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update Schedule", description = "Returns updated Schedule")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @PutMapping("/{id}")
  public ResponseEntity<BaseOutput<Schedule>> update(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) Schedule schedule) {
    if (id <= 0 || schedule == null) {
      BaseOutput<Schedule> response =
          BaseOutput.<Schedule>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    schedule.setId(id);
    Schedule createdSchedule = scheduleService.update(schedule);
    BaseOutput<Schedule> response =
        BaseOutput.<Schedule>builder()
            .message(HttpStatus.OK.toString())
            .data(createdSchedule)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Assign playlist to schedule",
      description = "Return Assigned playlist to schedule")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully assign playist"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}/playlists")
  public ResponseEntity<BaseOutput<Schedule>> assignPlaylist(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          List<Long> playlistId) {
    if (id <= 0 || playlistId == null || playlistId.isEmpty()) {
      BaseOutput<Schedule> response =
          BaseOutput.<Schedule>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Schedule assignedSchedule = scheduleService.assignPlaylist(id, playlistId);
    return ResponseEntity.ok(
        BaseOutput.<Schedule>builder()
            .message(HttpStatus.OK.toString())
            .data(assignedSchedule)
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @Operation(
      summary = "Update status of schedule",
      description = "Update status of schedule to ACITVE OR INACTIVE")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update the schedule status"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{scheduleId}/update-status/{status}")
  public ResponseEntity<BaseOutput<Schedule>> updateStatus(
      @PathVariable("scheduleId") @NotNull(message = REQUEST.INVALID_PATH_VARIABLE) Long scheduleId,
      @PathVariable("status") @NotNull(message = REQUEST.INVALID_PATH_VARIABLE) String status) {
    if (status == null || StringUtils.isBlank(status)) {
      BaseOutput<Schedule> response =
          BaseOutput.<Schedule>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_BODY))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Schedule updatedSchedule = scheduleService.updateStatus(scheduleId, Status.parse(status));
    return ResponseEntity.ok(
        BaseOutput.<Schedule>builder()
            .status(ResponseStatus.SUCCESS)
            .message(HttpStatus.OK.toString())
            .data(updatedSchedule)
            .build());
  }
}
