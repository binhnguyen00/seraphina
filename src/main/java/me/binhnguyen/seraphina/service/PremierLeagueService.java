package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremierLeagueService {
  private final MatchupRepo matchupRepo;
  private final CrawlerService crawlerService;

  /** save this week matches */
  @Transactional
  public List<Matchup> createOrUpdateMatches(Season season) {
    List<Map<String, Object>> matches = crawlerService.pullMatches(season);
    if (matches.isEmpty())
      return Collections.emptyList();

    List<Matchup> matchups = this.toMatchups(matches);
    List<Matchup> toCreates = new ArrayList<>();
    List<Matchup> toUpdates = new ArrayList<>();

    for (Matchup matchup : matchups) {
      String code = matchup.getOrCreateCode();
      Matchup exist = matchupRepo.getByCode(code);

      if (Objects.isNull(exist)) {
        toCreates.add(matchup);
      } else {
        exist.setHome(matchup.getHome());
        exist.setAway(matchup.getAway());
        exist.setMatchDay(matchup.getMatchDay());
        toUpdates.add(exist);
      }
    }

    List<Matchup> savedMatchups = new ArrayList<>();
    if (!toCreates.isEmpty()) savedMatchups.addAll(matchupRepo.saveAll(toCreates));
    if (!toUpdates.isEmpty()) savedMatchups.addAll(matchupRepo.saveAll(toUpdates));
    return savedMatchups;
  }

  /** save this matches by date range */
  @Transactional
  public List<Matchup> createOrUpdateMatches(Season season, LocalDate from, LocalDate to) {
    List<Map<String, Object>> matches = crawlerService.pullMatchesByDate(season, from, to);
    if (matches.isEmpty())
      return Collections.emptyList();

    List<Matchup> matchups = this.toMatchups(matches);
    List<Matchup> toCreates = new ArrayList<>();
    List<Matchup> toUpdates = new ArrayList<>();

    for (Matchup matchup : matchups) {
      String code = matchup.getOrCreateCode();
      Matchup exist = matchupRepo.getByCode(code);

      if (Objects.isNull(exist)) {
        matchup.setSeason(season);
        toCreates.add(matchup);
      } else {
        exist.setSeason(season);
        exist.setHome(matchup.getHome());
        exist.setAway(matchup.getAway());
        exist.setMatchDay(matchup.getMatchDay());
        toUpdates.add(exist);
      }
    }

    List<Matchup> savedMatchups = new ArrayList<>();
    if (!toCreates.isEmpty()) savedMatchups.addAll(matchupRepo.saveAll(toCreates));
    if (!toUpdates.isEmpty()) savedMatchups.addAll(matchupRepo.saveAll(toUpdates));
    return savedMatchups;
  }

  @SuppressWarnings("unchecked")
  private List<Matchup> toMatchups(List<Map<String, Object>> matches) {
    List<Matchup> matchups = new ArrayList<>();
    for (Map<String, Object> match : matches) {
      Matchup matchup = new Matchup();
      List<Map<String, Object>> competitors = (List<Map<String, Object>>) match.get("competitors");
      for (Map<String, Object> competitor : competitors) {
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
      OffsetDateTime utcTime = OffsetDateTime.parse(startTime);
      matchup.setMatchDay(utcTime.withOffsetSameInstant(ZoneOffset.of("+07:00"))); // Parse and convert to Vietnam offset (+07:00)
      matchup.setOriginMatchDay(utcTime.toLocalDateTime());
      matchup.setHomeStadium(homeStadium);
      matchup.setNotified(false);
      matchups.add(matchup);
    }

    return matchups;
  }

  /** returns this matches by date range*/
  public List<Matchup> getMatches(Season season, LocalDate from, LocalDate to) {
    return Collections.emptyList();
  }

  /** returns this week matches */
  public List<Matchup> getMatches(Season season) {
    List<LocalDate> thisWeekDates = season.getThisWeekMatchDays();
    List<Matchup> matches = matchupRepo.getByMatchDayBetween(
      thisWeekDates.getFirst(),
      thisWeekDates.getLast()
    );
    if (matches.isEmpty()) {
      log.info("No matches found for this week {}", thisWeekDates);
      return Collections.emptyList();
    }
    return matches;
  }

  public List<Matchup> markNotified(List<Matchup> matchups) {
    for (Matchup matchup : matchups) {
      matchup.setNotified(true);
    }
    return matchupRepo.saveAll(matchups);
  }
}
