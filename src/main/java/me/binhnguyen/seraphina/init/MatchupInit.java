package me.binhnguyen.seraphina.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class MatchupInit {
  private final SeasonService seasonService;
  private final PremierLeagueService premierLeagueService;

  @PostConstruct
  public void initData() {
    int year = LocalDate.now().getYear();
    Season season = seasonService.getCurrentSeason();
    LocalDate from = LocalDate.parse("2025-09-01");
    LocalDate to = LocalDate.parse("2025-09-30");

    log.info("Init matchup data {} - {}", from, to);
  }
}
