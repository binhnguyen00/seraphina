package me.binhnguyen.seraphina.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;
import me.binhnguyen.seraphina.common.DatePattern;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
  @Column(nullable = false)
  private String home;

  @Getter @Setter
  @Column(nullable = false)
  private String away;

  @Getter @Setter
  @ManyToOne
  @JoinColumn(name = "season_id", nullable = false)
  private Season season;

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
    return this.matchDay.format(DateTimeFormatter.ofPattern(DatePattern.READABLE_DATETIME));
  }

  @PrePersist
  public void generateCode() {
    if (Objects.isNull(this.code) || this.code.isEmpty() || this.code.isBlank()) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
      String matchDayStr = matchDay.format(formatter);
      String homeSlug = slugify(home);
      String awaySlug = slugify(away);
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
