package me.binhnguyen.seraphina.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.binhnguyen.seraphina.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Table(name = Subscriber.TABLE_NAME)
@Entity
@NoArgsConstructor
public class Subscriber extends BaseEntity {
  public static final String TABLE_NAME = "subscriber";

  public Subscriber(String lookupId, String name) {
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
    name = "subscriber_following_league",
    joinColumns = @JoinColumn(name = "subscriber_id"),    // FK to Subscriber
    inverseJoinColumns = @JoinColumn(name = "league_id")  // FK to League
  )
  private Set<League> followingLeagues = new HashSet<>();
}
