package com.lpdev.dsd.models.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@Table(name = "license")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class LicenseEntity extends CommonEntity {

  @Column(name = "code", unique = true, nullable = false)
  String code;

  @Column(name = "token", length = 4096, nullable = false)
  String token;

  @Column(name = "public_key", length = 4096, nullable = false)
  String publicKey;

  @Column(name = "private_key", length = 4096, nullable = false)
  String privateKey;

  @Column(name = "active_date", nullable = false)
  Date activeDate;

  @Column(name = "expire_date", nullable = false)
  Date expireDate;

  @Column(name = "description ")
  String description;

  @ToString.Exclude
  @OneToOne(mappedBy = "license", fetch = FetchType.LAZY)
  UserEntity user;
}
