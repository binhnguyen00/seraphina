package me.binhnguyen.seraphina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

import java.time.LocalDate;

@Table(name = Matchup.TABLE_NAME)
@Entity
public class Matchup extends BaseEntity {
  public static final String TABLE_NAME = "matchup";

  @Getter @Setter
  private String home;

  @Getter @Setter
  private String away;

  @Getter @Setter
  @Column(name = "is_notified")
  private boolean isNotified;

  @Getter @Setter
  @Column(name = "match_day")
  private LocalDate matchDay;
}
