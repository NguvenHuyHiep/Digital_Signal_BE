package com.lpdev.dsd.commons.utils;

import com.lpdev.dsd.models.DownloadFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Slf4j
public class DsdUtils {
  private static final String ALGORITHM_RSA = "RSA";
  private static final Random RANDOM = new Random();

  public static ByteArrayResource parse(@NonNull InputStream inputStream) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int bytesRead;

      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      byte[] byteArray = outputStream.toByteArray();
      return new ByteArrayResource(byteArray);
    }
  }

  public static KeyPair generateRsaKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
      generator.initialize(2048);
      return generator.generateKeyPair();
    } catch (Exception e) {
      log.error("ERROR generateRsaKeyPair with e: {}", e.getMessage(), e);
      return null;
    }
  }

  public static String encodeKeyToString(Key key) {
    try {
      byte[] keyBytes = key.getEncoded();
      return Base64.getEncoder().encodeToString(keyBytes);
    } catch (Exception e) {
      log.error("ERROR encodeKeyToString with e: {}", e.getMessage(), e);
      return null;
    }
  }

  public static PublicKey getPublicFromString(String publicKeyString) {
    try {
      byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
      return keyFactory.generatePublic(spec);
    } catch (Exception e) {
      log.error("ERROR getPublicFromString with e: {}", e.getMessage(), e);
      return null;
    }
  }

  public static PrivateKey getPrivateFromString(String privateKeyString) {
    try {
      byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
      return keyFactory.generatePrivate(keySpec);
    } catch (Exception e) {
      log.error("ERROR getPrivateFromString with e: {}", e.getMessage(), e);
      return null;
    }
  }

  public static HttpHeaders getHeadersForDownload(@NonNull String fileName) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", fileName);
    return headers;
  }

  public static ByteArrayResource zipDownloadFiles(List<DownloadFile> downloadFiles)
      throws IOException {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      for (DownloadFile downloadFile : downloadFiles) {
        ByteArrayResource resource = downloadFile.getResource();
        String fileName = downloadFile.getName();
        addToZip(resource, fileName, zipOutputStream);
      }
      zipOutputStream.finish();
      byte[] zipData = byteArrayOutputStream.toByteArray();
      return new ByteArrayResource(zipData);
    }
  }

  public static String randomLicenseGenerate() {
    StringBuilder licenseCode = new StringBuilder();
    // Define the length of each group
    int groupLength = 4;
    // Define the number of groups
    int groupCount = 3;
    // Define the character set
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (int i = 0; i < groupCount; i++) {
      // Generate a group
      StringBuilder group = new StringBuilder();
      for (int j = 0; j < groupLength; j++) {
        int index = RANDOM.nextInt(characters.length());
        group.append(characters.charAt(index));
      }

      // Append the group to the license code
      licenseCode.append(group);

      // Add an underscore if it's not the last group
      if (i != groupCount - 1) {
        licenseCode.append("_");
      }
    }

    return licenseCode.toString();
  }

  private static void addToZip(
      ByteArrayResource resource, String fileName, ZipOutputStream zipOutputStream)
      throws IOException {
    try (InputStream inputStream = resource.getInputStream()) {
      ZipEntry zipEntry = new ZipEntry(fileName);
      zipOutputStream.putNextEntry(zipEntry);

      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0) {
        zipOutputStream.write(buffer, 0, length);
      }

      zipOutputStream.closeEntry();
    }
  }
}
