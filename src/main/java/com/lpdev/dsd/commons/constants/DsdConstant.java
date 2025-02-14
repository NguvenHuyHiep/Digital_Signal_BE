package com.lpdev.dsd.commons.constants;

import java.util.Random;

public class DsdConstant {

  public static Random RANDOM = new Random();

  public static class ERROR {

    public static class AUTH {
      public static final String FAILED = "error.auth.failed";
      public static final String NOT_FOUND = "error.auth.credentials_not_found";
      public static final String NOT_ALLOWED = "error.auth.credentials_not_allowed";
      public static final String JWT_NOT_CREATED = "error.auth.credentials_jwt_not_created";
      public static final String CREDENTIALS_INVALID = "error.auth.credentials_invalid";
    }

    public static class REQUEST {
      public static final String INVALID_PATH_VARIABLE = "error.request.path_variable_invalid";
      public static final String INVALID_BODY = "error.request.body_invalid";
      public static final String INVALID_BODY_EMAIL = "error.request.body_invalid_email";
      public static final String INVALID_BODY_PASSWORD = "error.request.body_invalid_password";
      public static final String INVALID_PARAM = "error.request.param_invalid";
      public static final String INVALID_PARAM_OTP = "error.request.param_invalid_otp";
      public static final String INVALID_PARAM_EMAIL = "error.request.param_invalid_email";
      public static final String INVALID = "error.request.invalid";
    }

    public static class FILE {
      public static final String DELETE = "error.file.delete";
      public static final String CREATE = "error.file.create";
      public static final String UPDATE = "error.file.update";
      public static final String EXIST = "error.file.exist";
      public static final String NOT_EXIST = "error.file.not_exist";
      public static final String SAVE_TO_DB = "error.file.save_to_db";
      public static final String SAVE_TO_SOURCE = "error.file.save_to_source";
      public static final String DOWNLOAD = "error.file.download";
      public static final String ZIP = "error.file.zip";
      public static final String CORRUPTED = "error.file.corrupted";
      public static final String EMPTY = "error.file.empty";
      public static final String MOVE = "error.file.move";
    }

    public static class MINIO {
      public static final String INVALID_KEY = "error.minio.invalid_key";
      public static final String FILE_ALREADY_EXISTS = "error.minio.file_already_exists";
      public static final String RESPONSE = "error.minio.response";
      public static final String INSUFFICIENT_DATA = "error.minio.insufficient_data";
      public static final String INTERNAL_EXCEPTION = "error.minio.internal_exception";
      public static final String INVALID_RESPONSE = "error.minio.invalid_response";
      public static final String IO = "error.minio.io";
      public static final String NO_SUCH_ALGORITHM = "error.minio.no_such_algorithm";
      public static final String SERVER = "error.minio.server";
      public static final String XML_PARSER = "error.minio.xml_parser";
      public static final String EXTERNAL = "error.minio.external";
    }

    public static class CATEGORY {
      public static final String CREATE = "error.category.create";
      public static final String UPDATE = "error.category.update";
      public static final String DELETE = "error.category.delete";
      public static final String EXIST = "error.category.exist";
      public static final String NOT_CHILD = "error.category.not_child";
      public static final String NOT_EXIST = "error.category.not_exist";
    }

    public static class PLAYLIST {
      public static final String CREATE = "error.playlist.create";
      public static final String UPDATE = "error.playlist.update";
      public static final String DELETE = "error.playlist.delete";
      public static final String EXIST = "error.playlist.exist";
      public static final String NOT_EXIST = "error.playlist.not_exist";
    }

    public static class PLAYLIST_FILE_MAP {
      public static final String NULL_OR_EMPTY_IDS =
          "error.playlist_file_map.playlist_ids.file_ids_null_or_empty";
    }

    public static class LICENSE {
      public static final String CREATE = "error.license.create";
      public static final String UPDATE = "error.license.update";
      public static final String DELETE = "error.license.delete";
      public static final String EXIST = "error.license.exist";
      public static final String NOT_EXIST = "error.license.not_exist";
      public static final String UNMATCHED = "error.license.unmatched";
      public static final String EXPIRED = "error.license.expired";
      public static final String INVALID = "error.license.invalid";
      public static final String EXPAND_FAILED = "error.license.expand_failed";
      public static final String NOT_ASSIGNED_USER = "error.license.not_assigned_user";
      public static final String EXIST_BY_USER = "error.license.exist_by_user";
    }

    public static class DEVICE_GROUP {
      public static final String CREATE = "error.device_group.create";
      public static final String UPDATE = "error.device_group.update";
      public static final String DELETE = "error.device_group.delete";
      public static final String EXIST = "error.device_group.exist";
      public static final String NOT_EXIST = "error.device_group.not_exist";
    }

    public static class DEVICE {
      public static final String CREATE = "error.device.create";
      public static final String UPDATE = "error.device.update";
      public static final String DELETE = "error.device.delete";
      public static final String EXIST = "error.device.exist";
      public static final String NOT_EXIST = "error.device.not_exist";
      public static final String DOWNLOAD = "error.device.download";
      public static final String INVALID_DATE = "error.device.invalid_date";
    }

    public static class DEVICE_LOG {
      public static final String EXPORT = "error.device_log.date_invalid";
    }

    public static class SECURITY {
      public static final String GENERATE_KEY_PAIR = "error.security.generate_key_pair";
      public static final String ENCODE_KEY = "error.security.encode_key_pair";
      public static final String DECODE_KEY = "error.security.decode_key_pair";
    }

    public static class SCHEDULE {
      public static final String CREATE = "error.schedule.create";
      public static final String UPDATE = "error.schedule.update";
      public static final String DELETE = "error.schedule.delete";
      public static final String EXIST = "error.schedule.exist";
      public static final String NOT_EXIST = "error.schedule.not_exist";
      public static final String INVALID = "error.schedule.invalid";
      public static final String DOWNLOAD = "error.schedule.download";
    }

    public static class USER {
      public static final String NOT_EXIST = "error.user.not_exist";
      public static final String EXIST = "error.user.exist";
      public static final String NOT_ASSIGNED_ROLE = "error.user.not_assigned_role";
    }

    public static class ROLE {
      public static final String NOT_EXIST = "error.role.not_exist";
      public static final String NOT_ALLOWED = "error.role.not_allowed";
      public static final String EXIST = "error.role.exist";
    }

    public static class OTP {
      public static final String INVALID = "error.otp.invalid";
      public static final String NOT_EXIST = "error.otp.not_exist";
      public static final String STATUS_INACTIVE = "error.otp.status_inactive";
      public static final String EXPIRED = "error.otp.expired";
      public static final String WAIT = "error.otp.wait";
      public static final String UPDATE = "error.otp.update";
      public static final String CREATE = "error.otp.create";
    }

    public static class SERVER {
      public static final String INTERNAL = "error.server.internal";
    }
  }
}
