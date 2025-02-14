package com.lpdev.dsd.commons.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DsdDateUtils {
  private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
  private static final SimpleDateFormat EXPORT_DATE_FORMAT =
      new SimpleDateFormat("dd/MM/yyyy HH:mm:ss X");

  public static String parse(Date date) {
    if (date == null) {
      return null;
    }
    return FILE_DATE_FORMAT.format(date);
  }

  public static String parseForExport(Date date) {
    if (date == null) {
      return null;
    }
    return EXPORT_DATE_FORMAT.format(date);
  }

  public static Date getMaxDateForPostgreSQL() {
    LocalDate maxLocalDate = LocalDate.of(9999, 12, 31);
    ZoneId zoneId = ZoneId.systemDefault();
    return Date.from(maxLocalDate.atStartOfDay(zoneId).toInstant());
  }
}
