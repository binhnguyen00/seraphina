package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.service.CrawlerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class CrawlerServiceTest {

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private CrawlerService crawlerService;

  @Test
  void pullScheduleMatchesTest() {
    crawlerService.pullScheduleMatches();
  }
}
