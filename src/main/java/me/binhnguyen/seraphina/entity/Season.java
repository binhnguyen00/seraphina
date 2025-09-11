package me.binhnguyen.seraphina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = Season.TABLE_NAME)
public class Season extends BaseEntity {
  public static final String TABLE_NAME = "season";

  @Getter @Setter
  @Column(name = "season_year", nullable = false)
  private String year;

  public Season() {
    setYear(String.valueOf(LocalDate.now().getYear()));
  }
}
