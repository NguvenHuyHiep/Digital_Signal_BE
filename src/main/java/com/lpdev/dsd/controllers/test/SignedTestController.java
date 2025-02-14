package com.lpdev.dsd.controllers.test;

import com.lpdev.dsd.models.responses.BaseOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signed/test")
public class SignedTestController {

  @GetMapping("/endpoint")
  public ResponseEntity<BaseOutput<String>> testAdmin() {
    return ResponseEntity.ok(BaseOutput.<String>builder().data("user endpoint").build());
  }
}
