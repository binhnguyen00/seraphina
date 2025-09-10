package me.binhnguyen.seraphina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Entity
@Table(name = Season.TABLE_NAME)
public class Season extends BaseEntity {
  public static final String TABLE_NAME = "match_day";

  @Getter @Setter
  @Column(name = "season_year", nullable = false)
  private String year;

  @Deprecated
  @Getter @Setter
  @Column(name = "match_days")
  private List<LocalDate> matchDays;

  public Season() {
    setYear(String.valueOf(LocalDate.now().getYear()));
  }

  public List<LocalDate> getThisWeekMatchDays() {
    LocalDate today = LocalDate.now();
    LocalDate thisSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    LocalDate thisSunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    return this.matchDays
      .stream()
      .filter(date -> date.isAfter(today))
      .filter(date -> date.equals(thisSaturday) || date.equals(thisSunday))
      .toList();
  }
}
