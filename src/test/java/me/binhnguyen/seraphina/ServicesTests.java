package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.service.CrawlerService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ServicesTests {

  @Autowired
  private CrawlerService crawlerService;

  @Autowired
  private SeasonService seasonService;

  @Autowired
  private PremierLeagueService premierLeagueService;

  @Test
  void pullEmptyMatchesTest() {
    Season thisSeason = seasonService.getOrCreate();
    List<Map<String, Object>> matches = crawlerService.pullMatchesByDate(
      thisSeason,
      LocalDate.parse("2025-09-06"),
      LocalDate.parse("2025-09-07")
    );
    Assertions.assertTrue(matches.isEmpty());
  }

  @Test
  void pullMatchesTest() {
    Season thisSeason = seasonService.getOrCreate();
    List<Map<String, Object>> matches = crawlerService.pullMatchesByDate(
      thisSeason,
      LocalDate.parse("2025-09-13"),
      LocalDate.parse("2025-09-14")
    );
    Assertions.assertFalse(matches.isEmpty());
  }

  @Test
  void createOrUpdateMatchesTest() {
    Season thisSeason = seasonService.getOrCreate();
    List<Matchup> matchups = premierLeagueService.createOrUpdateMatches(
      thisSeason,
      LocalDate.parse("2025-09-13"),
      LocalDate.parse("2025-09-14")
    );
    Assertions.assertFalse(matchups.isEmpty());
    matchups.forEach(matchup -> {
      System.out.println(matchup.getCode());
      System.out.println(matchup.getMatchDay().toString());
    });
  }
}
