package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.repository.SeasonRepo;
import me.binhnguyen.seraphina.service.CrawlerService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
class CrawlerServiceTest {

  @Autowired
  private CrawlerService crawlerService;

  @Autowired
  private SeasonService seasonService;

  @Test
  void pullScheduleMatchesTest() {
    Season thisSeason = seasonService.getOrCreate();
    List<HashMap<String, Object>> matches = crawlerService.pullScheduleMatches(thisSeason);
    Assertions.assertTrue(matches.isEmpty(), "Expected matches from ESPN API");
  }
}
