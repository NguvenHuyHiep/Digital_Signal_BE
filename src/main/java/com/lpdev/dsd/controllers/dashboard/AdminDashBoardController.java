package com.lpdev.dsd.controllers.dashboard;

import com.lpdev.dsd.commons.enums.ResponseStatus;
import com.lpdev.dsd.models.responses.BaseOutput;
import com.lpdev.dsd.models.responses.DashBoardResponse;
import com.lpdev.dsd.services.DashBoardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "Admin-DashBoard API")
@SecurityRequirement(name = "Authorization")
public class AdminDashBoardController {
  @Autowired DashBoardService dashBoardService;

  @GetMapping("")
  protected ResponseEntity<BaseOutput<DashBoardResponse>> getDashBoardStatus() {
    DashBoardResponse dashBoardResponse = dashBoardService.getDashBoard();
    BaseOutput<DashBoardResponse> response =
        BaseOutput.<DashBoardResponse>builder()
            .message(HttpStatus.OK.toString())
            .data(dashBoardResponse)
            .status(ResponseStatus.SUCCESS)
            .build();
    return ResponseEntity.ok(response);
  }
}
