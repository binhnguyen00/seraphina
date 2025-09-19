package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Subscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class SubscriberTest extends InitDataTest {
  private final String LOOKUP_ID = "306e6075fc20157e4c31";

  @Test
  void subscribeTest() {
    Subscriber subscriber = subscriberService.getSubscriber(LOOKUP_ID);
    List<League> leagues = subscriber.getFollowingLeagues().stream().toList();
    Assertions.assertFalse(leagues.isEmpty());
  }

  @Test
  void getAllSubscribersTest() {
    List<Subscriber> subscribers = subscriberService.getAllSubscribers();
    Assertions.assertFalse(subscribers.isEmpty());
  }

  @Test
  void unsubscribeTest() {
    boolean success = subscriberService.unsubscribe(LOOKUP_ID);
    Assertions.assertTrue(success);
  }

  @Test
  void followLeagueTest() {
    League premierLeague = premierLeagueService.get();
    // unfollow first becuz subscriber auto follow premier league
    subscriberService.unfollowLeague(LOOKUP_ID, premierLeague.getCode());
    boolean success = subscriberService.followLeague(LOOKUP_ID, premierLeague.getCode()).success();
    Assertions.assertTrue(success);

    League laliga = laligaService.get();
    success = subscriberService.followLeague(LOOKUP_ID, laliga.getCode()).success();
    Assertions.assertTrue(success);
  }

  @Test
  void unfollowLeagueTest() {
    League premierLeague = premierLeagueService.get();
    boolean success = subscriberService.unfollowLeague(LOOKUP_ID, premierLeague.getCode()).success();
    Assertions.assertTrue(success);

    League laliga = laligaService.get();
    success = subscriberService.unfollowLeague(LOOKUP_ID, laliga.getCode()).success();
    Assertions.assertTrue(success);
  }

  @Test
  void getFollowingLeaguesMatches() {
    League championsLeague = championLeagueService.get();
    League premierLeague = premierLeagueService.get();
    subscriberService.unfollowLeague(LOOKUP_ID, premierLeague.getCode());
    subscriberService.followLeague(LOOKUP_ID, championsLeague.getCode());

    Subscriber subscriber = subscriberService.getSubscriber(LOOKUP_ID);
    final LocalDate today = LocalDate.now();
    final LocalDate MONDAY = today.with(DayOfWeek.MONDAY);
    final LocalDate SUNDAY = today.with(DayOfWeek.SUNDAY);
    List<DataRecord> leagues = subscriberService.getLeagues(subscriber, MONDAY, SUNDAY);
    Assertions.assertFalse(leagues.isEmpty());
  }
}
