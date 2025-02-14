package com.lpdev.dsd.controllers.user;

import com.lpdev.dsd.models.dtos.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/signed/user")
public class SignedUserController {

  @PostMapping("/update-info")
  public ResponseEntity<User> updateInfo(@RequestBody User user) {
    return ResponseEntity.ok(user);
  }
}
