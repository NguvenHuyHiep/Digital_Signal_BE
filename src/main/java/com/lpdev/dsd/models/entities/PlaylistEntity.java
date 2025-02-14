package com.lpdev.dsd.models.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.lpdev.dsd.commons.enums.Status;
import jakarta.persistence.*;
import java.util.Date;
import java.util.LinkedHashSet;
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
@Table(name = "playlist")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class PlaylistEntity extends CommonEntity {

  @Column(name = "name")
  String name;

  @Column(name = "description")
  String description;

  @Column(name = "start_time")
  Date startTime;

  @Column(name = "end_time")
  Date endTime;

  @Column(name = "is_loop")
  Boolean isLoop;

  @Column(name = "last_update")
  Date lastUpdateDate;

  @Column(name = "file_order")
  LinkedHashSet<Long> fileOrder;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  Status status;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  UserEntity user;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  ScheduleEntity schedule;

  @ToString.Exclude
  @OneToMany(mappedBy = "playlist")
  List<PlaylistFileMapEntity> playListFileMap;

  @ToString.Exclude
  @OneToMany(mappedBy = "playlist")
  List<DeviceGroupEntity> deviceGroups;
}
