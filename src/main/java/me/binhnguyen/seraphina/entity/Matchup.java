package me.binhnguyen.seraphina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Table(name = Matchup.TABLE_NAME)
@Entity
public class Matchup extends BaseEntity {
  public static final String TABLE_NAME = "matchup";

  @Getter @Setter
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
  private LocalDateTime matchDay;

  @Getter @Setter
  @Column(name = "is_notified")
  private boolean isNotified;

  @PrePersist
  public void generateCode() {
    if (this.code == null || this.code.isEmpty()) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
      String matchDayStr = matchDay.format(formatter);

      // Normalize team names
      String homeSlug = slugify(home);
      String awaySlug = slugify(away);

      this.code = homeSlug + "_" + awaySlug + "_" + matchDayStr;
    }
  }

  private String slugify(String input) {
    if (input == null) return "";
    return input.toLowerCase()
      .replaceAll("\\s+", "_")
      .replaceAll("[^a-z0-9_]", "");
  }
}
