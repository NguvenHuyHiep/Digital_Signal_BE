package com.lpdev.dsd.models.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseOutput<T> implements Serializable {
  @Serial private static final long serialVersionUID = 7786696609340276930L;
  transient List<String> errors;
  String message;
  Integer currentPage;
  Integer pageSize;
  Integer totalPages;
  Long total;
  ResponseStatus status;
  transient T data;
}
