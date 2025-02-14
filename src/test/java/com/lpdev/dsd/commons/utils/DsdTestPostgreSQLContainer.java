package com.lpdev.dsd.commons.utils;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class DsdTestPostgreSQLContainer extends PostgreSQLContainer<DsdTestPostgreSQLContainer> {
  private static final String IMAGE_VERISON = "postgres:16.0";
  private static DsdTestPostgreSQLContainer container;

  private DsdTestPostgreSQLContainer() {
    super(IMAGE_VERISON);
  }

  public static synchronized DsdTestPostgreSQLContainer getInstance() {
    if (container == null) {
      container = new DsdTestPostgreSQLContainer();
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    log.info("===== Start test container =====");
    log.info("DB_URL: {}", container.getJdbcUrl());
    log.info("DB_USERNAME: {}", container.getUsername());
    log.info("DB_PASSWORD: {}", container.getPassword());
  }

  @Override
  public void stop() {
    log.info("===== Stop test container =====");
  }
}
