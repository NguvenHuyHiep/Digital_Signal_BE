package com.lpdev.dsd.controllers.test;

import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/test")
@SecurityRequirement(name = "Authorization")
public class AdminTestController {

  private final UserService userService;

  @GetMapping("/endpoint")
  public ResponseEntity<BaseOutput<String>> test() {
    String loggedEmail = userService.getAuthenticatedUserEmail();
    return ResponseEntity.ok(
        BaseOutput.<String>builder()
            .data("admin endpoint: " + loggedEmail)
            .status(ResponseStatus.SUCCESS)
            .build());
  }
}
