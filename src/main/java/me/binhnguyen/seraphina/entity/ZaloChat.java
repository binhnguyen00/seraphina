package me.binhnguyen.seraphina.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

@Table(name = ZaloChat.TABLE_NAME)
@Entity
@AllArgsConstructor
public class ZaloChat extends BaseEntity {
  public static final String TABLE_NAME = "zalo_chat";

  @Getter @Setter
  @Column(name = "lookup_id", nullable = false)
  private String lookupId;

  @Getter @Setter
  private String name;
}
