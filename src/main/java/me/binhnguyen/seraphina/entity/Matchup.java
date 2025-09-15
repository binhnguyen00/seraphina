package me.binhnguyen.seraphina.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;
import me.binhnguyen.seraphina.common.DatePattern;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Table(name = Matchup.TABLE_NAME)
@Entity
public class Matchup extends BaseEntity {
  public static final String TABLE_NAME = "matchup";

  @Getter @Setter
  @Column(unique = true, nullable = false)
  private String code;

  @Getter @Setter
  @ManyToOne
  @JoinColumn(name = "home_id", nullable = false)
  private Team homeTeam;

  @Getter @Setter
  @ManyToOne
  @JoinColumn(name = "away_id", nullable = false)
  private Team awayTeam;

  @Getter @Setter
  @ManyToOne
  @JoinColumn(name = "season_id", nullable = false)
  private Season season;

  @Getter @Setter
  @ManyToOne
  @JoinColumn(name = "league_id", nullable = false)
  private League league;

  @Getter @Setter
  @Column(name = "home_stadium")
  private String homeStadium;

  @Getter @Setter
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatePattern.READABLE_DATETIME)
  @Column(name = "match_day")
  private OffsetDateTime matchDay;

  @Getter @Setter
  @Column(name = "origin_match_day")
  private LocalDateTime originMatchDay;

  @Getter @Setter
  @Column(name = "is_notified")
  private boolean isNotified;

  public String getOrCreateCode() {
    this.generateCode();
    return this.code;
  }

  public String getFormatMatchDay() {
    return this.matchDay
      .atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")) // adjust to +07
      .format(DateTimeFormatter.ofPattern(DatePattern.READABLE_DATETIME));
  }

  @PrePersist
  public void generateCode() {
    if (Objects.isNull(this.code) || this.code.isEmpty() || this.code.isBlank()) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
      String matchDayStr = matchDay.format(formatter);
      String homeSlug = slugify(homeTeam.getName());
      String awaySlug = slugify(awayTeam.getName());
      this.code = String.format("%s-%s-%s", homeSlug, awaySlug, matchDayStr);
    }
  }

  private String slugify(String input) {
    if (input == null) return "";
    return input.toLowerCase()
      .replaceAll("\\s+", "_")
      .replaceAll("[^a-z0-9_]", "");
  }
}
