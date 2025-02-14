package com.lpdev.dsd.repositories;

import com.lpdev.dsd.models.entities.LicenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseRepository extends JpaRepository<LicenseEntity, Long> {
  LicenseEntity findByCode(String code);

  boolean existsByCode(String code);
}
