package me.binhnguyen.seraphina.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

@Entity
@Table(name = Team.TABLE_NAME)
@NoArgsConstructor
public class Team extends BaseEntity {
  public static final String TABLE_NAME = "team";

  public Team(String code, String name) {
    this.code = code.toLowerCase();
    this.name = name;
  }

  @Getter @Setter
  private String name;

  @Getter @Setter
  @Column(nullable = false, unique = true)
  private String code;

  @Getter @Setter
  @Column(name = "lookup_id", nullable = false, unique = true)
  private String lookupId;

  @Getter @Setter
  @ManyToOne
  @JoinColumn(name = "league_id", nullable = false)
  private League league;
}
