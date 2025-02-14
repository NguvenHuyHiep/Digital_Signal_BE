package com.lpdev.dsd.services;

import java.io.InputStream;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
  String getBucket();

  void uploadToMinio(@NonNull String bucket, @NonNull MultipartFile file);

  void deleteFromMinio(@NonNull String path);

  InputStream downloadFromMinio(@NonNull String path);
}
