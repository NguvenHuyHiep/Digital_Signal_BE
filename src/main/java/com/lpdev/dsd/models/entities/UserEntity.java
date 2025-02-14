package com.lpdev.dsd.models.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.util.List;
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
@EqualsAndHashCode(callSuper = true, exclude = "userRoleMap")
@Entity
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserEntity extends CommonEntity {

  @Column(name = "username", unique = true)
  String userName;

  @Column(name = "email", unique = true)
  String email;

  @Column(name = "password")
  String password;

  @Column(name = "phone")
  String phone;

  @Column(name = "first_name")
  String firstName;

  @Column(name = "last_name")
  String lastName;

  @ToString.Exclude
  @OneToMany(mappedBy = "user")
  List<UserRoleMapEntity> userRoleMap;

  @ToString.Exclude
  @OneToMany(mappedBy = "user")
  @Transient
  List<PlaylistEntity> playlists;

  @ToString.Exclude
  @OneToMany(mappedBy = "user")
  @Transient
  List<ScheduleEntity> schedules;

  @ToString.Exclude
  @OneToMany(mappedBy = "user")
  @Transient
  List<DeviceEntity> devices;

  @ToString.Exclude
  @OneToMany(mappedBy = "user")
  @Transient
  List<DeviceGroupEntity> deviceGroups;

  @ToString.Exclude
  @OneToMany(mappedBy = "user")
  List<FileEntity> file;

  @ToString.Exclude
  @OneToMany(mappedBy = "user")
  @Transient
  List<CategoryEntity> categories;

  @OneToOne(cascade = CascadeType.REMOVE)
  @JoinColumn(
      name = "license_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  LicenseEntity license;
}
