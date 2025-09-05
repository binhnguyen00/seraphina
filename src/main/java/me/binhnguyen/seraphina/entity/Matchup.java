package me.binhnguyen.seraphina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

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
  private String home;

  @Getter @Setter
  private String away;

  @Getter @Setter
  @Column(name = "home_stadium")
  private String homeStadium;

  @Getter @Setter
  @Column(name = "match_day")
  private OffsetDateTime matchDay;

  @Getter @Setter
  @Column(name = "is_notified")
  private boolean isNotified;

  @PrePersist
  public String generateCode() {
    if (Objects.isNull(this.code) || this.code.isEmpty() || this.code.isBlank()) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
      String matchDayStr = matchDay.format(formatter);
      String homeSlug = slugify(home);
      String awaySlug = slugify(away);
      this.code = String.format("%s-%s-%s", homeSlug, awaySlug, matchDayStr);
    }
    return this.code;
  }

  private String slugify(String input) {
    if (input == null) return "";
    return input.toLowerCase()
      .replaceAll("\\s+", "_")
      .replaceAll("[^a-z0-9_]", "");
  }
}
