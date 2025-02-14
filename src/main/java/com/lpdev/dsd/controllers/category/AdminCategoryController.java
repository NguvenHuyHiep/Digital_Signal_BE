package com.lpdev.dsd.controllers.category;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.dtos.Category;
import com.lpdev.dsd.models.dtos.Tree;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.CategoryService;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/category")
@Tag(name = "Admin-Category API")
@SecurityRequirement(name = "Authorization")
public class AdminCategoryController {

  @Lazy private final CategoryService categoryService;

  @Operation(
      operationId = "getByPaging",
      summary = "Get all categories with pagination",
      description = "Returns all categories with pagination")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("")
  protected ResponseEntity<BaseOutput<List<Category>>> getByPaging(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "DESC") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String keyword) {

    Page<Category> categoryPage =
        categoryService.getByPaging(page, size, sortBy, sortDirection, keyword);
    BaseOutput<List<Category>> response =
        BaseOutput.<List<Category>>builder()
            .message(HttpStatus.OK.toString())
            .totalPages(categoryPage.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .total(categoryPage.getTotalElements())
            .data(categoryPage.getContent())
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "getTree",
      summary = "Get Tree categories",
      description = "Returns all categories with tree")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/tree")
  protected ResponseEntity<BaseOutput<List<Tree>>> getTree(
      @Param("search") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PARAM) String search) {
    String searchInput = search == null || search.trim().isEmpty() ? "" : search.trim();
    List<Tree> categoryTreeList = categoryService.getCategoryTree(searchInput);
    BaseOutput<List<Tree>> response =
        BaseOutput.<List<Tree>>builder()
            .message(HttpStatus.OK.toString())
            .data(categoryTreeList)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/tree/node/{categoryId}")
  protected ResponseEntity<BaseOutput<List<Tree>>> getTreeNodeByIdAndSearch(
      @PathVariable("categoryId")
          @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long categoryId,
      @Param("search") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PARAM) String search) {

    if (categoryId == null || categoryId < 0) {
      BaseOutput<List<Tree>> response =
          BaseOutput.<List<Tree>>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    String searchInput = search == null || search.trim().isEmpty() ? "" : search.trim();
    List<Tree> categoryTreeList = categoryService.getCategoryChildren(categoryId, searchInput);
    BaseOutput<List<Tree>> response =
        BaseOutput.<List<Tree>>builder()
            .message(HttpStatus.OK.toString())
            .data(categoryTreeList)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "getById",
      summary = "Get a category by id",
      description = "Returns a category as per the id")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/{id}")
  protected ResponseEntity<BaseOutput<Category>> getById(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<Category> response =
          BaseOutput.<Category>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    Category category = categoryService.getById(id);

    BaseOutput<Category> response =
        BaseOutput.<Category>builder()
            .message(HttpStatus.OK.toString())
            .data(category)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "create",
      summary = "Create a new category",
      description = "Create a new category")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping
  protected ResponseEntity<BaseOutput<Category>> create(
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY) Category category) {
    if (category == null) {
      BaseOutput<Category> response =
          BaseOutput.<Category>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Category createdCategory = categoryService.save(category);
    BaseOutput<Category> response =
        BaseOutput.<Category>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(createdCategory)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "update",
      summary = "Update a category by id",
      description = "Return updated category")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update category"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("/{id}")
  protected ResponseEntity<BaseOutput<Category>> update(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @RequestBody @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Category category) {
    if (id <= 0) {
      BaseOutput<Category> response =
          BaseOutput.<Category>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    category.setId(id);
    Category updateCategory = categoryService.update(category);
    BaseOutput<Category> response =
        BaseOutput.<Category>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .data(updateCategory)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "delete",
      summary = "Delete a category by id",
      description = "Return deleted category")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully delete category"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @DeleteMapping("/{id}")
  protected ResponseEntity<BaseOutput<String>> delete(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id) {
    if (id <= 0) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    categoryService.delete(id);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .status(ResponseStatus.SUCCESS)
            .message(HttpStatus.OK.toString())
            .build());
  }

  @Operation(
      operationId = "deleteByIds",
      summary = "Delete categories by ids",
      description = "Return deleted categories")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully delete  categories"),
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
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    categoryService.deleteByIds(ids);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @Operation(operationId = "assignFiles", summary = "Assign files", description = "Assign files")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully update new category"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PutMapping("assign-file/{id}/{fileId}")
  public ResponseEntity<BaseOutput<Category>> assignFiles(
      @PathVariable("id") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long id,
      @PathVariable("fileId") @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          Long fileId) {
    if (id <= 0 || fileId == null) {

      BaseOutput<Category> response =
          BaseOutput.<Category>builder()
              .status(ResponseStatus.FAILED)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Category assignedCategory = categoryService.assignFiles(id, fileId);
    BaseOutput<Category> response =
        BaseOutput.<Category>builder()
            .message(HttpStatus.OK.toString())
            .data(assignedCategory)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @PutMapping("/re-assign/{newParentyId}/{subId}")
  public ResponseEntity<BaseOutput<Category>> assignChildCategoryToParentCategory(
      @PathVariable("newParentyId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long newParentyId,
      @PathVariable("subId") @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long subId,
      @RequestParam @NotNull(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          boolean isFile) {
    if (subId == null || newParentyId == null) {
      BaseOutput<Category> response =
          BaseOutput.<Category>builder()
              .status(ResponseStatus.FAILED)
              .message(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    categoryService.assignChildCategoryToParentCategory(newParentyId, subId, isFile);
    BaseOutput<Category> response =
        BaseOutput.<Category>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "removeFiles",
      summary = "Remove files from a category ",
      description = "Return removed files")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully removed files"),
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
    categoryService.removeFiles(id, fileIds);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }

  @PutMapping("/remove-child/{parentCategoryId}")
  public ResponseEntity<BaseOutput<String>> removeChildCategoryFromParentCategory(
      @RequestBody @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_BODY)
          List<Long> childCategoryIds,
      @PathVariable("parentCategoryId")
          @NotBlank(message = DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
          Long parentCategoryId) {
    if (parentCategoryId == null || childCategoryIds == null) {
      BaseOutput<String> response =
          BaseOutput.<String>builder()
              .status(ResponseStatus.FAILED)
              .message(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE)
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_PATH_VARIABLE))
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    categoryService.removeChildCategoryFromParentCategory(parentCategoryId, childCategoryIds);
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .message(HttpStatus.OK.toString())
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
