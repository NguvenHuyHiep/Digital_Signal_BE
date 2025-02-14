package com.lpdev.dsd.services;

import com.lpdev.dsd.models.dtos.User;
import com.lpdev.dsd.models.dtos.UserRoleMap;
import java.util.List;

public interface UserRoleMapService {
  List<UserRoleMap> findByUser(User user);

  void setUserRoleForUser(Long userId);

  void setAdminRoleForUser(Long userId);
}
