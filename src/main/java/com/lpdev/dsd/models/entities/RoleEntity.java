package com.lpdev.dsd.models.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.lpdev.dsd.commons.enums.RoleType;
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
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "role")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RoleEntity extends CommonEntity {

  @Column(name = "name")
  String name;

  @ToString.Exclude
  @Column(name = "type", unique = true)
  @Enumerated(EnumType.STRING)
  RoleType type;

  @ToString.Exclude
  @OneToMany(mappedBy = "role")
  List<UserRoleMapEntity> userRoleMap;
}
