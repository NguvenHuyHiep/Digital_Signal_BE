package com.lpdev.dsd.controllers;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.commons.enums.RoleType;
import com.lpdev.dsd.components.JwtUtil;
import com.lpdev.dsd.configs.exceptions.JwtAuthenticationException;
import com.lpdev.dsd.models.dtos.Otp;
import com.lpdev.dsd.models.dtos.User;
import com.lpdev.dsd.models.requests.AuthRequest;
import com.lpdev.dsd.models.requests.OtpRequest;
import com.lpdev.dsd.models.responses.AuthResponse;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.EmailService;
import com.lpdev.dsd.services.OtpService;
import com.lpdev.dsd.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@SecurityRequirement(name = "Authorization")
public class AuthenticationController {

  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtUtil jwtUtil;

  private final EmailService emailService;
  private final OtpService otpService;
  private final UserService userService;

  @PostMapping("/login")
  public ResponseEntity<BaseOutput<AuthResponse>> login(@RequestBody @Valid AuthRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (BadCredentialsException e) {
      throw new JwtAuthenticationException(DsdConstant.ERROR.AUTH.CREDENTIALS_INVALID);
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
    RoleType userRole =
        RoleType.parse(userDetails.getAuthorities().iterator().next().getAuthority());
    if (userRole.isAdmin()) {
      log.info("User role is admin, process to login");
      String jwt = jwtUtil.generateToken(userDetails);
      return ResponseEntity.ok(
          BaseOutput.<AuthResponse>builder()
              .message(HttpStatus.OK.toString())
              .data(
                  AuthResponse.builder()
                      .user(userService.getByEmail(request.getEmail()))
                      .token(jwt)
                      .build())
              .status(ResponseStatus.SUCCESS)
              .build());
    } else if (userRole.isUser()) {
      log.info("User role is user, process to send otp");
      otpService.create(request.getEmail());
      if (request.getIsNotSendingEmail() == null
          || Boolean.FALSE.equals(request.getIsNotSendingEmail())) {
        emailService.sendOTP(request.getEmail());
      }
      return ResponseEntity.ok(
          BaseOutput.<AuthResponse>builder()
              .message(HttpStatus.OK.toString())
              .status(ResponseStatus.SUCCESS)
              .build());
    }

    log.info("User role is undefined, process to forbidden");
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            BaseOutput.<AuthResponse>builder()
                .errors(List.of(DsdConstant.ERROR.AUTH.NOT_ALLOWED))
                .status(ResponseStatus.FAILED)
                .build());
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<BaseOutput<AuthResponse>> verifyOtp(
      @RequestBody @Valid OtpRequest otpRequest) {
    if (otpRequest == null) {
      BaseOutput<AuthResponse> response =
          BaseOutput.<AuthResponse>builder()
              .errors(List.of(DsdConstant.ERROR.REQUEST.INVALID_BODY))
              .status(ResponseStatus.FAILED)
              .build();
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Otp otp = otpService.verify(otpRequest.getEmail(), otpRequest.getOtp());
    if (otp == null || StringUtils.isAllBlank(otp.getOtp(), otp.getEmail())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BaseOutput.<AuthResponse>builder()
                  .errors(List.of(DsdConstant.ERROR.OTP.NOT_EXIST))
                  .status(ResponseStatus.FAILED)
                  .build());
    }

    User user = userService.getByEmail(otp.getEmail());
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              BaseOutput.<AuthResponse>builder()
                  .errors(List.of(DsdConstant.ERROR.USER.NOT_EXIST))
                  .status(ResponseStatus.FAILED)
                  .build());
    }

    String jwt = jwtUtil.generateToken(user.getEmail());
    if (StringUtils.isBlank(jwt)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              BaseOutput.<AuthResponse>builder()
                  .errors(List.of(DsdConstant.ERROR.AUTH.JWT_NOT_CREATED))
                  .status(ResponseStatus.FAILED)
                  .build());
    }

    return ResponseEntity.ok(
        BaseOutput.<AuthResponse>builder()
            .message(HttpStatus.OK.toString())
            .data(AuthResponse.builder().user(user).token(jwt).build())
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
