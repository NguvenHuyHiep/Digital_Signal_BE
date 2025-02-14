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
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"playlist", "file"})
@Entity
@Table(name = "playlist_file_map")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class PlaylistFileMapEntity extends CommonEntity {

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "playlist_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  PlaylistEntity playlist;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  FileEntity file;

  @Builder.Default
  @Column(name = "assign_date")
  Date assignDate = new Date();
}
