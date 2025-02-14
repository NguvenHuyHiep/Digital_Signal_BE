package com.lpdev.dsd.configs;

import com.lpdev.dsd.commons.enums.RoleType;
import com.lpdev.dsd.components.JwtAuthenticationEntryPoint;
import com.lpdev.dsd.configs.filters.JwtRequestFilter;
import com.lpdev.dsd.configs.objects.DsdMinioProp;
import com.lpdev.dsd.services.impl.JwtUserDetailsServiceImpl;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final DsdMinioProp dsdMinioProp;
  private final JwtRequestFilter jwtRequestFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  public MinioClient minioClient() {
    return MinioClient.builder()
        .endpoint(dsdMinioProp.getEndpoint())
        .credentials(dsdMinioProp.getAccessKey(), dsdMinioProp.getSecretKey())
        .build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService());
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowCredentials(true);
    corsConfig.addExposedHeader("Authorization");
    corsConfig.addAllowedOriginPattern("*"); // Allow all origins
    corsConfig.addAllowedMethod("*"); // Allow all HTTP methods
    corsConfig.addAllowedHeader("*"); // Allow all headers

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsFilter(source);
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return new JwtUserDetailsServiceImpl();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            aMRMR ->
                aMRMR
                    .requestMatchers("/swagger/**", "/api/v1/auth/**", "/api/v1/public/**")
                    .permitAll()
                    .requestMatchers("api/v1/admin/**")
                    .hasAnyAuthority(
                        RoleType.ADMIN.getValue(),
                        RoleType.USER.getValue()) // TODO REMOVE USER LATER
                    .requestMatchers("api/v1/signed/**")
                    .hasAnyAuthority(RoleType.ADMIN.getValue(), RoleType.USER.getValue())
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            httpSEHC -> httpSEHC.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .sessionManagement(
            httpSSMC -> httpSSMC.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
