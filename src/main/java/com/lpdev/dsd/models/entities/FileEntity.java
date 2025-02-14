package com.lpdev.dsd.models.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.lpdev.dsd.commons.enums.Status;
import jakarta.persistence.*;
import java.util.Date;
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
@Table(name = "file")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class FileEntity extends CommonEntity {

  @Column(name = "name")
  String name;

  @Column(name = "file_type")
  String fileType;

  @Column(name = "path", unique = true)
  String path;

  @Column(name = "create_date")
  Date createDate;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  Status status;

  @ToString.Exclude
  @OneToMany(mappedBy = "file")
  List<PlaylistFileMapEntity> playListFileMap;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  UserEntity user;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  CategoryEntity category;
}
