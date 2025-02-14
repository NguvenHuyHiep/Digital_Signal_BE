package com.lpdev.dsd.models.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.lpdev.dsd.commons.enums.DeviceType;
import jakarta.persistence.*;

import java.util.Date;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ip_log")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class IPLogEntity extends CommonEntity {

  @Column(name = "ip")
  String ip;

  @Column(name = "path")
  String path;

  @Column(name = "user_info")
  String userInfo;

  @Column(name = "action_time")
  Date actionTime;

  @ToString.Exclude
  @Column(name = "device_type")
  @Enumerated(EnumType.STRING)
  DeviceType deviceType;

  @Column(name = "device_id")
  String deviceId;
}
