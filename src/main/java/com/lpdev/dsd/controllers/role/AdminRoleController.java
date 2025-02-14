package com.lpdev.dsd.controllers.role;

import com.lpdev.dsd.commons.constants.DsdConstant.ERROR.REQUEST;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.dtos.Role;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/admin/role")
@Tag(name = "Admin-Role API")
@SecurityRequirement(name = "Authorization")
public class AdminRoleController {
  private final RoleService roleService;

  @Operation(
      summary = "Get all user's Role with pagination",
      description = "Returns all user's Role with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("")
  protected ResponseEntity<BaseOutput<List<Role>>> getByPaging(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy) {
    Page<Role> rolesPage = roleService.getByPaging(page, size, sortBy);

    BaseOutput<List<Role>> response =
        BaseOutput.<List<Role>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(rolesPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(rolesPage.getTotalElements())
            .data(rolesPage.getContent())
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get user's Role by ID", description = "Returns user's Role by ID")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @GetMapping("/{id}")
  protected ResponseEntity<BaseOutput<Role>> getById(
      @PathVariable("id") @NotBlank(message = "error.id.invalid") Long id) {
    if (id <= 0) {
      BaseOutput<Role> response =
          BaseOutput.<Role>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    Role role = roleService.getById(id);

    BaseOutput<Role> response =
        BaseOutput.<Role>builder()
            .message(HttpStatus.OK.toString())
            .data(role)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Create role", description = "Returns created role with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @PostMapping
  protected ResponseEntity<BaseOutput<Role>> create(
      @RequestBody @NotNull(message = "error.request.body.invalid") Role role) {

    if (role == null) {
      BaseOutput<Role> response =
          BaseOutput.<Role>builder()
              .errors(List.of(REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Role createdRole = roleService.create(role);
    BaseOutput<Role> response =
        BaseOutput.<Role>builder()
            .message(HttpStatus.OK.toString())
            .data(createdRole)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update role", description = "Returns updated role with pagination")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully retrieved"))
  @PutMapping("/{id}")
  protected ResponseEntity<BaseOutput<Role>> update(
      @PathVariable("id") @NotBlank(message = "error.id.invalid") Long id,
      @RequestBody @NotNull(message = "error.request.body.invalid") Role role) {
    if (id <= 0) {
      BaseOutput<Role> response =
          BaseOutput.<Role>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    role.setId(id);
    Role createdRole = roleService.update(role);
    BaseOutput<Role> response =
        BaseOutput.<Role>builder()
            .message(HttpStatus.OK.toString())
            .data(createdRole)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete a role by id", description = "Return deleted role")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully delete license",
            content = {@Content(examples = @ExampleObject(value = "{\"message\": \"200 OK\"}"))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @DeleteMapping("/{id}")
  protected ResponseEntity<BaseOutput<String>> delete(
      @PathVariable("id") @NotBlank(message = "error.id.invalid") Long id) {
    if (id <= 0) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    roleService.delete(id);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .data(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
