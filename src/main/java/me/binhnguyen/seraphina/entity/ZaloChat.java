package me.binhnguyen.seraphina.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

import java.util.List;

@Table(name = ZaloChat.TABLE_NAME)
@Entity
@NoArgsConstructor
public class ZaloChat extends BaseEntity {
  public static final String TABLE_NAME = "zalo_chat";

  public ZaloChat(String lookupId, String name) {
    this.name = name;
    this.lookupId = lookupId;
  }

  @Getter @Setter
  @Column(name = "lookup_id", nullable = false)
  private String lookupId;

  @Getter @Setter
  private String name;

  @Getter @Setter
  @ManyToMany
  @JoinTable(
    name = "zalo_chat_following_league",
    joinColumns = @JoinColumn(name = "zalo_chat_id"),     // FK to ZaloChat
    inverseJoinColumns = @JoinColumn(name = "league_id")  // FK to League
  )
  private List<League> followingLeagues;
}
