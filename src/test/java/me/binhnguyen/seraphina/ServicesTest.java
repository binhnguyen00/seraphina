package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.service.CrawlerService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class ServicesTest {

  @Autowired
  private CrawlerService crawlerService;

  @Autowired
  private SeasonService seasonService;

  @Autowired
  private PremierLeagueService premierLeagueService;

  @Test
  void pullEmptyMatchesTest() {
    Season thisSeason = seasonService.getOrCreate();
    List<HashMap<String, Object>> matches = crawlerService.pullMatchesByDate(
      thisSeason,
      LocalDate.parse("2025-09-06"),
      LocalDate.parse("2025-09-07")
    );
    Assertions.assertTrue(matches.isEmpty());
  }

  @Test
  void pullMatchesTest() {
    Season thisSeason = seasonService.getOrCreate();
    List<HashMap<String, Object>> matches = crawlerService.pullMatchesByDate(
      thisSeason,
      LocalDate.parse("2025-09-13"),
      LocalDate.parse("2025-09-14")
    );
    Assertions.assertFalse(matches.isEmpty());
  }

  @Test
  @Disabled("Skipping until finished debugging")
  void saveMatchupTest() {
    List<Matchup> matchups = premierLeagueService.saveThisWeekMatches();
    Assertions.assertFalse(matchups.isEmpty());
  }
}
