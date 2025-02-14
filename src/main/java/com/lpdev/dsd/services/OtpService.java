package com.lpdev.dsd.services;

import com.lpdev.dsd.models.dtos.Otp;
import lombok.NonNull;

public interface OtpService {

  Otp create(@NonNull String email);

  Otp verify(@NonNull String email, @NonNull String otp);

  Otp getOtpByEmail(@NonNull String email);
}
