package com.lpdev.dsd.configs.objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
@ConfigurationProperties("dsd-config.minio")
public class DsdMinioProp {
  String endpoint;
  String username;
  String password;
  String bucket;
  String accessKey;
  String secretKey;
}
