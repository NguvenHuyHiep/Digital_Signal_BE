package com.lpdev.dsd.services;

import com.lpdev.dsd.models.dtos.Role;
import com.lpdev.dsd.models.dtos.User;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;

public interface UserService {

  String getAuthenticatedUserEmail();

  Page<User> getByPaging(
      int pageNo, int pageSize, String sortBy, String sortDirection, String keyword);

  User getById(Long id);

  User create(User user);

  User update(User user);

  void delete(@NonNull Long id);

  User findLoggedInfoByEmail(String email);

  User createAdmin(User user);

  void assignLicenseByLicenseIdAndId(@NonNull Long licenseId, @NonNull Long id);

  User getByEmail(@NonNull String email);

  void deleteByIds(List<Long> ids);

  boolean isAdmin();

  List<Role> findRolesByEmail(@NonNull String email);
}
