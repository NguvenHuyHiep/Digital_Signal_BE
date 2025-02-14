package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.configs.objects.DsdMailProp;
import com.lpdev.dsd.models.dtos.Otp;
import com.lpdev.dsd.services.EmailService;
import com.lpdev.dsd.services.OtpService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class EmailServiceImpl implements EmailService {

  @Lazy @Autowired private DsdMailProp dsdMailProp;
  @Lazy @Autowired private JavaMailSender emailSender;
  @Lazy @Autowired private OtpService otpService;

  @Override
  public void sendOTP(@NonNull String email) {
    Otp otp = otpService.getOtpByEmail(email);
    if (otp == null) {
      log.info("OTP is null, stop sending email: {}", email);
      throw new DsdCommonException(DsdConstant.ERROR.OTP.NOT_EXIST);
    }

    log.info("Start sending otp to email: {}", email);
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject(dsdMailProp.getSubject());
    message.setText(dsdMailProp.getContent() + otp.getOtp());
    emailSender.send(message);
  }
}
