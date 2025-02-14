package com.lpdev.dsd.controllers.user;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.REQUEST;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.dtos.User;
import com.lpdev.dsd.models.responses.BaseOutput;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/user")
@Tag(name = "Admin-Users API")
@SecurityRequirement(name = "Authorization")
public class AdminUserController {

  private final UserService userService;

  @Operation(
      summary = "Get all users with pagination",
      description = "Returns all users with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  public ResponseEntity<BaseOutput<List<User>>> getAllByPaging(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {
    Page<User> userPage = userService.getByPaging(page, size, sortBy, sortDirection, keyword);
    BaseOutput<List<User>> response =
        BaseOutput.<List<User>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(userPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(userPage.getTotalElements())
            .data(userPage.getContent())
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get a user by id", description = "Returns a user as per the id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<BaseOutput<User>> getById(
      @PathVariable("id") @NotBlank(message = "error.request.path.variable.id.invalid") Long id) {
    if (id == null) {
      BaseOutput<User> response =
          BaseOutput.<User>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    User user = userService.getById(id);

    BaseOutput<User> response =
        BaseOutput.<User>builder()
            .message(HttpStatus.OK.toString())
            .data(user)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get a user by email", description = "Returns a user as per the email")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/email/{email}")
  public ResponseEntity<BaseOutput<User>> getByEmail(
      @PathVariable("email") @NotBlank(message = REQUEST.INVALID_PATH_VARIABLE) String email) {
    if (StringUtils.isAllBlank(email)) {
      BaseOutput<User> response =
          BaseOutput.<User>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    User user = userService.getByEmail(email);

    BaseOutput<User> response =
        BaseOutput.<User>builder()
            .message(HttpStatus.OK.toString())
            .data(user)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Create a new user", description = "Create a new user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping
  public ResponseEntity<BaseOutput<User>> create(
      @RequestBody @NotNull(message = "error.request.body.invalid") User user) {

    if (user == null) {
      BaseOutput<User> response =
          BaseOutput.<User>builder()
              .errors(List.of(REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    User createdUser = userService.create(user);
    BaseOutput<User> response =
        BaseOutput.<User>builder()
            .message(HttpStatus.OK.toString())
            .data(createdUser)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update a user by id", description = "Return updated user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update new user"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<BaseOutput<User>> update(
      @PathVariable("id") @NotBlank(message = "error.request.path.variable.id.invalid") Long id,
      @RequestBody @NotNull(message = "error.request.body.invalid") User user) {
    if (id == null) {
      BaseOutput<User> response =
          BaseOutput.<User>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    user.setId(id);
    User createdBid = userService.update(user);
    BaseOutput<User> response =
        BaseOutput.<User>builder()
            .message(HttpStatus.OK.toString())
            .data(createdBid)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete a user by id", description = "Return deleted user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully delete  user"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<BaseOutput<String>> delete(
      @PathVariable("id") @NotBlank(message = "error.request.path.variable.id.invalid") Long id) {
    if (id <= 0) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    userService.delete(id);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .data(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @PutMapping("/{userId}/license/{licenseId}")
  public ResponseEntity<BaseOutput<User>> assignLicenseByLicenseIdAndId(
      @PathVariable("userId") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long userId,
      @PathVariable("licenseId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long licenseId) {
    if (userId == null || licenseId == null) {
      BaseOutput<User> response =
          BaseOutput.<User>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    userService.assignLicenseByLicenseIdAndId(licenseId, userId);
    return ResponseEntity.ok(
        BaseOutput.<User>builder()
            .message(HttpStatus.OK.toString())
            .data(userService.getById(userId))
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @Operation(summary = "Delete users by id", description = "Return deleted users")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully delete  users"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @DeleteMapping()
  public ResponseEntity<BaseOutput<String>> deleteByIds(
      @RequestBody @NotBlank(message = "error.id.invalid") List<Long> ids) {
    if (ids == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .message("error.id.invalid")
              .errors(List.of(REQUEST.INVALID_BODY))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    userService.deleteByIds(ids);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
