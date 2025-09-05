package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PremierLeagueService {
  private final SeasonService seasonService;
  private final CrawlerService crawlerService;
  private final MatchupRepo matchupRepo;

  @Transactional
  @SuppressWarnings("unchecked")
  public List<Matchup> saveThisWeekMatches() {
    Season thisSeason = seasonService.getOrCreate();
    List<HashMap<String, Object>> matches = crawlerService.pullMatches(thisSeason);

    List<Matchup> matchups = new ArrayList<>();
    for (HashMap<String, Object> match : matches) {
      Matchup matchup = new Matchup();
      ArrayList<HashMap<String, Object>> competitors = (ArrayList<HashMap<String, Object>>) match.get("competitors");
      for (HashMap<String, Object> competitor : competitors) {
        String teamName = competitor.getOrDefault("teamName", "").toString();
        String homeAway = competitor.getOrDefault("homeAway", "").toString();
        boolean isHome = homeAway.equals("home");
        if (isHome)
          matchup.setHome(teamName);
        else
          matchup.setAway(teamName);
      }
      String homeStadium = match.getOrDefault("homeStadium", "").toString();
      String startTime = match.getOrDefault("startTime", "").toString();
      OffsetDateTime offsetDateTime = OffsetDateTime.parse(startTime);
      LocalDateTime matchStartAt = offsetDateTime.toLocalDateTime();
      matchup.setMatchDay(matchStartAt);
      matchup.setHomeStadium(homeStadium);
      matchup.setNotified(false);
      matchups.add(matchup);
    }

    try {
      if (!matchups.isEmpty())
        matchups = matchupRepo.saveAll(matchups);
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    return matchups;
  }
}
