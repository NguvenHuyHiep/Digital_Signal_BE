package com.lpdev.dsd.services;

import com.lpdev.dsd.models.dtos.License;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface LicenseService {
  License getById(@NonNull Long id);

  Page<License> getByPaging(int pageNo, int pageSize, String sortBy, String sortDirection);

  License generateByEmail(@NonNull String email, @NonNull Long activeDuration);

  License expand(@NonNull String code, @NonNull Long duration);

  void delete(@NonNull Long id);

  void assignUserByUserIdAndId(@NonNull Long licenseId, @NonNull Long id);

  boolean isExpired(@NonNull String code);

  License verify(@NonNull String code);
}
