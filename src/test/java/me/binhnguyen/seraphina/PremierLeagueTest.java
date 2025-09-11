package me.binhnguyen.seraphina;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.MatchDay;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class PremierLeagueTest extends InitDataTest {

  @Test
  void getAllMatchDayTest() {
    List<MatchDay> matchDays = premierLeagueService.getAllMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    log.info("ALl MatchDays: {}", matchDays.size());
  }

  @Test
  void getThisWeekMatchDaysTest() {
    List<MatchDay> matchDays = premierLeagueService.getThisWeekMatchDays();
    Assertions.assertFalse(matchDays.isEmpty());
    matchDays.forEach(matchDay -> log.info("Date: {}", matchDay.getDate()));
  }

  @Test
  void createOrUpdateAllMatchDaysTest() {
    List<MatchDay> matchDays = premierLeagueService.createOrUpdateAllMatchDays();
    log.info("All MatchDays: {}", matchDays.size());
    Assertions.assertFalse(matchDays.isEmpty());
  }
}