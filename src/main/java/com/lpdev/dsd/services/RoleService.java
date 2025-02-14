package com.lpdev.dsd.services;

import com.lpdev.dsd.models.dtos.Role;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {
  Role getById(Long id);

  Page<Role> getByPaging(int pageNo, int pageSize, String sortBy);

  Role create(Role role);

  Role update(Role role);

  void delete(Long id);

  Role getUserRole();

  Role createUserRole();

  Role getAdminRole();

  Role createAdminRole();
}
