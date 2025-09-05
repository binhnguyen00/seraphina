package me.binhnguyen.seraphina.service;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PremierLeagueService {
  private final SeasonService seasonService;
  private final CrawlerService crawlerService;
  private final MatchupRepo matchupRepo;

}
