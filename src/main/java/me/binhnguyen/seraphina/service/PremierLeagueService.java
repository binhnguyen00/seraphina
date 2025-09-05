package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremierLeagueService {
  private final SeasonService seasonService;
  private final CrawlerService crawlerService;
  private final MatchupRepo matchupRepo;

  /** save this week matches */
  @Transactional
  public List<Matchup> saveMatches(Season season) {
    List<Matchup> matchups = new ArrayList<>();
    List<HashMap<String, Object>> matches = crawlerService.pullMatches(season);
    if (matches.isEmpty())
      return matchups;

    matchups = this.toMatchups(matches);
    try {
      if (!matchups.isEmpty()) {
        matchups = matchupRepo.saveAll(matchups);
      } else {
        log.warn("No matches found for this week");
        return matchups;
      }
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    return matchups;
  }

  /** save this matches by date range */
  @Transactional
  public List<Matchup> saveMatches(Season season, LocalDate from, LocalDate to) {
    List<Matchup> matchups = new ArrayList<>();
    List<HashMap<String, Object>> matches = crawlerService.pullMatchesByDate(season, from, to);
    if (matches.isEmpty())
      return matchups;

    matchups = this.toMatchups(matches);
    try {
      if (!matchups.isEmpty()) {
        matchups = matchupRepo.saveAll(matchups);
      } else {
        log.warn("No matches found from {} to {}", from.toString(), to.toString());
        return matchups;
      }
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }

    return matchups;
  }

  @SuppressWarnings("unchecked")
  public List<Matchup> toMatchups(List<HashMap<String, Object>> matches) {
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

    return matchups;
  }
}
