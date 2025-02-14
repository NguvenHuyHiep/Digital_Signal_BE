package com.lpdev.dsd.configs.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpdev.dsd.commons.enums.DeviceType;
import com.lpdev.dsd.components.JwtUtil;
import com.lpdev.dsd.models.dtos.IPLog;
import com.lpdev.dsd.services.IPLogService;
import com.lpdev.dsd.services.impl.JwtUserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private static final String IP_LOGGED_ATTRIBUTE = "IP_LOGGED";
  @Autowired JwtUtil jwtUtil;
  @Autowired JwtUserDetailsServiceImpl jwtUserDetailsService;
  @Autowired private IPLogService ipLogService;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String servletPath = request.getServletPath();
    if (servletPath.startsWith("/api/v1/auth")) {
      if (request.getAttribute(IP_LOGGED_ATTRIBUTE) == null) {
        String ip = this.getClientIp(request);
        DeviceType deviceType = this.extractDeviceType(request);
        String deviceId = this.extractDeviceId(request);

        this.saveIPLog(ip, servletPath, null, new Date(), deviceType, deviceId);

        request.setAttribute(IP_LOGGED_ATTRIBUTE, true);
      }
      return true;
    }

    return false;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String requestTokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    String email = null;
    String jwtToken = null;

    String ip = this.getClientIp(request);
    DeviceType deviceType = this.extractDeviceType(request);
    String deviceId = this.extractDeviceId(request);
    // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
    if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
      jwtToken = requestTokenHeader.substring(7);
      try {
        email = jwtUtil.extractEmail(jwtToken);
      } catch (IllegalArgumentException e) {
        log.error("Unable to get JWT Token");
      } catch (ExpiredJwtException e) {
        log.error("JWT Token has expired");
      }
    } else {
      log.warn("JWT Token does not begin with Bearer String");
    }

    // Once we get the token validate it.
    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(email);

      // if token is valid configure Spring Security to manually set authentication
      if (jwtUtil.validateToken(jwtToken, userDetails)) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));
        // After setting the Authentication in the context, we specify
        // that the current user is authenticated. So it passes the Spring Security Configurations
        // successfully.
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        this.saveIPLog(ip, request.getServletPath(), email, new Date(), deviceType, deviceId);
      }
    }

    filterChain.doFilter(request, response);
  }

  private String getClientIp(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    } else {
      ipAddress = ipAddress.split(",")[0];
    }
    return ipAddress;
  }

  public DeviceType extractDeviceType(HttpServletRequest request) {
    String deviceId = request.getHeader("Device-ID");

    if (deviceId != null && !deviceId.isEmpty()) {
      return DeviceType.APP;
    } else {
      return DeviceType.WEB;
    }
  }

  public String extractDeviceId(HttpServletRequest request) {
    String deviceId = request.getHeader("Device-ID");

    return (deviceId != null && !deviceId.isEmpty()) ? deviceId : null;
  }

  private void saveIPLog(
      String ip,
      String path,
      String userInfo,
      Date actionTime,
      DeviceType deviceType,
      String deviceId) {
    IPLog ipLog =
        IPLog.builder()
            .ip(ip)
            .path(path)
            .userInfo(userInfo)
            .actionTime(actionTime)
            .deviceType(deviceType)
            .deviceId(deviceId)
            .build();
    ipLogService.save(ipLog);
  }
}
