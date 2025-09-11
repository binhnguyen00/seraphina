package me.binhnguyen.seraphina.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Table(name = MatchDay.TABLE_NAME)
public class MatchDay extends BaseEntity {
  public static final String TABLE_NAME = "match_day";

  public MatchDay(Season season, League league, LocalDate date) {
    this.season = season;
    this.league = league;
    this.date = date;
  }

  @Getter @Setter
  @ManyToOne
  @JoinColumn(name = "season_id", nullable = false)
  private Season season;

  @Getter @Setter
  @ManyToOne
  @JoinColumn(name = "league_id", nullable = false)
  private League league;

  @Getter @Setter
  @Column(name = "date", nullable = false)
  private LocalDate date;
}
