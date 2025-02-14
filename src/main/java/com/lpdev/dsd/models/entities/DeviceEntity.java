package com.lpdev.dsd.models.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.lpdev.dsd.commons.enums.DeviceStatus;
import jakarta.persistence.*;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@Table(name = "device")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DeviceEntity extends CommonEntity {

  @Column(name = "name")
  String name;

  @Column(name = "code", unique = true)
  String code;

  @Column(name = "information")
  String information;

  @Column(name = "description ")
  String description;

  @Column(name = "serial_no")
  String serialNo;

  @Column(name = "vehicle_number")
  String vehicleNumber;

  @Column(name = "ybs")
  String ybs;

  @ToString.Exclude
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  DeviceStatus status;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  UserEntity user;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "device_group_id",
      foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  DeviceGroupEntity deviceGroup;

  @ToString.Exclude
  @OneToMany(mappedBy = "device")
  List<DeviceLogEntity> deviceLogs;
}
