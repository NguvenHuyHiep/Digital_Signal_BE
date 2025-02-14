package com.lpdev.dsd.repositories;

import com.lpdev.dsd.commons.enums.RoleType;
import com.lpdev.dsd.models.entities.RoleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
  Optional<RoleEntity> findByType(RoleType type);

  boolean existsByType(RoleType type);
}
