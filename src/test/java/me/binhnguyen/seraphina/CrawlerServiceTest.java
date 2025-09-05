package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.service.CrawlerService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class CrawlerServiceTest {

  @Autowired
  private CrawlerService crawlerService;

  @Autowired
  private SeasonService seasonService;

  @Test
  void pullCurrentWeekScheduleMatchesTest() {
    Season thisSeason = seasonService.getOrCreate();
    List<HashMap<String, Object>> matches = crawlerService.pullScheduleMatches(thisSeason, null, null);
    Assertions.assertTrue(matches.isEmpty());
  }

  @Test
  void pullScheduleMatchesTest() {
    Season thisSeason = seasonService.getOrCreate();
    List<HashMap<String, Object>> matches = crawlerService.pullScheduleMatches(thisSeason, LocalDate.parse("2025-09-13"), LocalDate.parse("2025-09-14"));
    Assertions.assertFalse(matches.isEmpty());
  }
}
