package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.*;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.repository.MatchDayRepo;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import me.binhnguyen.seraphina.repository.TeamRepo;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremierLeagueService {
  public static final String LEAGUE_ID = "eng.1";

  private final TeamRepo teamRepo;
  private final LeagueRepo leagueRepo;
  private final MatchupRepo matchupRepo;
  private final MatchDayRepo matchDayRepo;
  private final CrawlerService crawlerService;
  private final SeasonService seasonService;

  public League create() {
    League exist = leagueRepo.getByCode(LEAGUE_ID);
    if (Objects.nonNull(exist)) {
      log.warn("League: {} is already exist", exist.getName());
      return exist;
    } else {
      League premierLeague = leagueRepo.save(new League(LEAGUE_ID, "Premier League"));
      log.info("League: {} created", premierLeague.getName());
      return premierLeague;
    }
  }

  public List<MatchDay> getThisWeekMatchDays() {
    Season season = seasonService.getCurrentSeason();
    LocalDate today = LocalDate.now();
    LocalDate thisSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    LocalDate thisSunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    List<MatchDay> matchDays = matchDayRepo.getMatchDay(LEAGUE_ID, Integer.parseInt(season.getYear()));
    if (matchDays.isEmpty()) {
      log.error("Found no match day in this week");
      return Collections.emptyList();
    }
    return matchDays
      .stream()
      .filter(matchDay -> matchDay.getDate().isAfter(today))
      .filter(matchDay -> matchDay.getDate().equals(thisSaturday) || matchDay.getDate().equals(thisSunday))
      .toList();
  }

  public List<MatchDay> getAllMatchDays() {
    Season season = seasonService.getCurrentSeason();
    List<MatchDay> matchDays = matchDayRepo.getMatchDay(LEAGUE_ID, Integer.parseInt(season.getYear()));
    if (matchDays.isEmpty()) {
      log.error("Found no match day");
      return Collections.emptyList();
    }
    return matchDays;
  }

  /** Create/Update matches from all league */
  @Transactional
  public List<Matchup> createOrUpdateMatches(LocalDate from, LocalDate to) {
    List<League> leagues = leagueRepo.findAll();
    List<Matchup> savedMatchups = new ArrayList<>();

    for (League league : leagues) {
      List<Matchup> matches = this.createOrUpdateMatches(league, from, to);
      savedMatchups.addAll(matches);
    }

    return savedMatchups;
  }

  /** Create/Update matches from target league */
  @Transactional
  public List<Matchup> createOrUpdateMatches(League league, LocalDate from, LocalDate to) {
    Season season = seasonService.getCurrentSeason();
    List<Matchup> toCreates = new ArrayList<>();
    List<Matchup> toUpdates = new ArrayList<>();
    List<Matchup> savedMatchups = new ArrayList<>();

    List<Map<String, Object>> matches = crawlerService.pullMatchesByDateRange(league.getCode(), from, to);
    if (matches.isEmpty())
      return Collections.emptyList();

    List<Matchup> matchups = this.toMatchups(matches);
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

    if (!toCreates.isEmpty()) savedMatchups.addAll(matchupRepo.saveAll(toCreates));
    log.info("Created {} matches", toCreates.size());
    if (!toUpdates.isEmpty()) savedMatchups.addAll(matchupRepo.saveAll(toUpdates));
    log.info("Updated {} matches", toUpdates.size());

    return savedMatchups;
  }

  @SuppressWarnings("unchecked")
  public List<Matchup> toMatchups(List<Map<String, Object>> matches) {
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

  /** returns current week scheduled matches */
  public List<Matchup> getCurrentWeekMatches(Season season) {
    List<LocalDate> thisWeekDates = season.getThisWeekMatchDays();
    OffsetDateTime from = thisWeekDates.getFirst().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    OffsetDateTime to = thisWeekDates.getLast().atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
    List<Matchup> matches = matchupRepo.getByMatchDayBetween(from, to);
    if (matches.isEmpty()) {
      log.info("No matches found for this week {}", thisWeekDates);
      return Collections.emptyList();
    }
    return matches;
  }

  @Transactional
  public List<Team> createOrUpdateTeams() {
    List<Team> teams = crawlerService.pullTeams(LEAGUE_ID);
    if (teams.isEmpty())
      return Collections.emptyList();

    List<Team> toCreates = new ArrayList<>();
    List<Team> toUpdates = new ArrayList<>();

    League premierLeague = leagueRepo.getByCode(LEAGUE_ID);
    teams.forEach(team -> {
      Team exist = teamRepo.getByCode(team.getCode());
      if (Objects.isNull(exist)) {
        team.setLeague(premierLeague);
        toCreates.add(team);
      } else {
        exist.setName(team.getName());
        exist.setCode(team.getCode());
        exist.setLookupId(team.getLookupId());
        toUpdates.add(exist);
      }
    });

    List<Team> savedTeams = new ArrayList<>();
    if (!toCreates.isEmpty()) savedTeams.addAll(teamRepo.saveAll(toCreates));
    if (!toUpdates.isEmpty()) savedTeams.addAll(teamRepo.saveAll(toUpdates));
    log.info("Created {} teams", toCreates.size());
    log.info("Updated {} teams", toUpdates.size());
    return savedTeams;
  }
}
