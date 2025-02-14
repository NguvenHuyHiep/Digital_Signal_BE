package com.lpdev.dsd.services;

import lombok.NonNull;
import org.springframework.scheduling.annotation.Async;

public interface EmailService {

  @Async
  void sendOTP(@NonNull String to);
}
