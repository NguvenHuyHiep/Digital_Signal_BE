package com.lpdev.dsd.repositories;

import com.lpdev.dsd.commons.enums.Status;
import com.lpdev.dsd.models.entities.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

  OtpEntity findByOtp(String otp);

  OtpEntity findByEmail(String email);

  OtpEntity findByEmailAndOtpAndStatus(String email, String otp, Status status);
}
