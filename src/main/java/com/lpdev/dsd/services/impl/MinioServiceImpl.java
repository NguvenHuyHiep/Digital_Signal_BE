package com.lpdev.dsd.services.impl;

import com.lpdev.dsd.commons.constants.DsdConstant;
import com.lpdev.dsd.configs.exceptions.DsdCommonException;
import com.lpdev.dsd.services.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
public class MinioServiceImpl implements MinioService {
  @Autowired private MinioClient minioClient;

  @Value("${dsd-config.minio.bucket}")
  private String bucketName;

  @Override
  public String getBucket() {
    return this.bucketName;
  }

  @Override
  public void uploadToMinio(@NonNull String bucket, @NonNull MultipartFile file) {
    if (file.isEmpty()) {
      log.error("ERROR file empty");
      throw new DsdCommonException(DsdConstant.ERROR.FILE.EMPTY);
    }

    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(file.getOriginalFilename()).stream(
                  file.getInputStream(), file.getSize(), -1)
              .contentType(file.getContentType())
              .build());
      log.info("uploaded file to minio, file: {}", file.getOriginalFilename());
    } catch (InvalidKeyException e) {
      log.error("ERROR InvalidKeyException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INVALID_KEY);
    } catch (FileAlreadyExistsException e) {
      log.error("ERROR FileAlreadyExistsException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.FILE_ALREADY_EXISTS);
    } catch (ErrorResponseException e) {
      log.error("ERROR ErrorResponseException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.RESPONSE);
    } catch (InsufficientDataException e) {
      log.error("ERROR InsufficientDataException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INSUFFICIENT_DATA);
    } catch (InternalException e) {
      log.error("ERROR InternalException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INTERNAL_EXCEPTION);
    } catch (InvalidResponseException e) {
      log.error("ERROR InvalidResponseException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INVALID_RESPONSE);
    } catch (IOException e) {
      log.error("ERROR IOException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.IO);
    } catch (NoSuchAlgorithmException e) {
      log.error("ERROR NoSuchAlgorithmException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.NO_SUCH_ALGORITHM);
    } catch (ServerException e) {
      log.error("ERROR ServerException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.SERVER);
    } catch (XmlParserException e) {
      log.error("ERROR XmlParserException when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.XML_PARSER);
    } catch (Exception e) {
      log.error("ERROR when upload file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.EXTERNAL);
    }
  }

  @Override
  public void deleteFromMinio(@NonNull String path) {
    try {
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(path).build());
      log.info("deleted from minio client, file: {}", path);
    } catch (InvalidKeyException e) {
      log.error("ERROR InvalidKeyException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INVALID_KEY);
    } catch (FileAlreadyExistsException e) {
      log.error("ERROR FileAlreadyExistsException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.FILE_ALREADY_EXISTS);
    } catch (ErrorResponseException e) {
      log.error("ERROR ErrorResponseException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.RESPONSE);
    } catch (InsufficientDataException e) {
      log.error("ERROR InsufficientDataException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INSUFFICIENT_DATA);
    } catch (InternalException e) {
      log.error("ERROR InternalException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INTERNAL_EXCEPTION);
    } catch (InvalidResponseException e) {
      log.error("ERROR InvalidResponseException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INVALID_RESPONSE);
    } catch (IOException e) {
      log.error("ERROR IOException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.IO);
    } catch (NoSuchAlgorithmException e) {
      log.error("ERROR NoSuchAlgorithmException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.NO_SUCH_ALGORITHM);
    } catch (ServerException e) {
      log.error("ERROR ServerException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.SERVER);
    } catch (XmlParserException e) {
      log.error("ERROR XmlParserException when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.XML_PARSER);
    } catch (Exception e) {
      log.error("ERROR when delete file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.EXTERNAL);
    }
  }

  @Override
  public InputStream downloadFromMinio(@NonNull String path) {
    try {
      return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(path).build());
    } catch (InvalidKeyException e) {
      log.error("ERROR InvalidKeyException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INVALID_KEY);
    } catch (FileAlreadyExistsException e) {
      log.error("ERROR FileAlreadyExistsException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.FILE_ALREADY_EXISTS);
    } catch (ErrorResponseException e) {
      log.error("ERROR ErrorResponseException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.RESPONSE);
    } catch (InsufficientDataException e) {
      log.error("ERROR InsufficientDataException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INSUFFICIENT_DATA);
    } catch (InternalException e) {
      log.error("ERROR InternalException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INTERNAL_EXCEPTION);
    } catch (InvalidResponseException e) {
      log.error("ERROR InvalidResponseException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.INVALID_RESPONSE);
    } catch (IOException e) {
      log.error("ERROR IOException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.IO);
    } catch (NoSuchAlgorithmException e) {
      log.error("ERROR NoSuchAlgorithmException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.NO_SUCH_ALGORITHM);
    } catch (ServerException e) {
      log.error("ERROR ServerException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.SERVER);
    } catch (XmlParserException e) {
      log.error("ERROR XmlParserException when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.XML_PARSER);
    } catch (Exception e) {
      log.error("ERROR when get file", e);
      throw new DsdCommonException(DsdConstant.ERROR.MINIO.EXTERNAL);
    }
  }
}
