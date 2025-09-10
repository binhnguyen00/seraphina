package me.binhnguyen.seraphina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = League.TABLE_NAME)
public class League extends BaseEntity {
  public static final String TABLE_NAME = "league";

  @Getter @Setter
  @Column(nullable = false)
  private String code;

  @Getter @Setter
  private String name;
}
