package com.lpdev.dsd.models.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpdev.dsd.models.dtos.Device;
import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class DeviceUpdateStatus {
  Device device;
  Date lastUpdate;
}
