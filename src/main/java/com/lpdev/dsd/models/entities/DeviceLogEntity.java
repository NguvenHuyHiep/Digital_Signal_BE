package com.lpdev.dsd.models.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.lpdev.dsd.commons.enums.DeviceStatus;
import jakarta.persistence.*;
import java.util.Date;
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
@Table(
    name = "device_log",
    indexes = {@Index(name = "idx_log_date", columnList = "log_date")})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DeviceLogEntity extends CommonEntity {

  @Column(name = "log_date")
  Date date;

  @ToString.Exclude
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  DeviceStatus status;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "device_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  DeviceEntity device;
}
