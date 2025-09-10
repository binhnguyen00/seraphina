package me.binhnguyen.seraphina.init;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamAndLeagueInit {
  private final PremierLeagueService premierLeagueService;

  @PostConstruct
  public void init() {
    try {
      premierLeagueService.create();
      premierLeagueService.createOrUpdateTeams();
    } catch (Exception e) {
      log.error("Failed to initialize TeamAndLeagueInit", e);
      throw e; // rethrow so Spring still fails and shows error
    }
  }
}
