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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremierLeagueService {
  public static final String LEAGUE_CODE = "eng.1";

  private final TeamRepo teamRepo;
  private final LeagueRepo leagueRepo;
  private final MatchupRepo matchupRepo;
  private final MatchDayRepo matchDayRepo;
  private final CrawlerService crawlerService;
  private final SeasonService seasonService;

  public List<MatchDay> getThisWeekMatchDays() {
    final LocalDate today = LocalDate.now();
    final LocalDate THIS_MONDAY = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    final LocalDate THIS_SUNDAY = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    Season season = seasonService.getCurrentSeason();
    List<MatchDay> matchDays = matchDayRepo.findByLeagueAndYear(LEAGUE_CODE, season.getYear());
    if (matchDays.isEmpty()) {
      log.error("Found no match day in this week");
      return Collections.emptyList();
    }

    return matchDays
      .stream()
      .filter(matchDay -> !matchDay.getDate().isBefore(THIS_MONDAY)) // >= Monday
      .filter(matchDay -> !matchDay.getDate().isAfter(THIS_SUNDAY))  // <= Sunday
      .toList();
  }

  public List<MatchDay> getAllMatchDays() {
    Season season = seasonService.getCurrentSeason();
    List<MatchDay> matchDays = matchDayRepo.findByLeagueAndYear(LEAGUE_CODE, season.getYear());
    if (matchDays.isEmpty()) {
      log.error("Found no match day");
      return Collections.emptyList();
    }
    return matchDays;
  }

  /** create/update this season match days */
  public List<MatchDay> createOrUpdateAllMatchDays() {
    List<MatchDay> matchDays = this.getAllMatchDays();
    List<LocalDate> schedules = crawlerService.pullCurrentSeasonScheduleMatchDays(LEAGUE_CODE);
    League league = this.get();
    Season season = seasonService.getCurrentSeason();
    List<MatchDay> holder = new ArrayList<>();
    if (matchDays.isEmpty()) {
      for (LocalDate schedule : schedules) {
        MatchDay matchDay = new MatchDay(season, league, schedule);
        holder.add(matchDay);
      }
    } else {
      for (LocalDate schedule : schedules) {
        MatchDay matchDay = matchDays.stream()
          .filter(day -> day.getDate().equals(schedule))
          .findFirst()
          .orElse(null);
        if (Objects.isNull(matchDay)) {
          matchDay = new MatchDay(season, league, schedule);
        }
        holder.add(matchDay);
      }
    }

    return matchDayRepo.saveAll(holder);
  }

  /** Create/Update matches from all league */
  @Transactional
  public List<Matchup> createOrUpdateMatchups(LocalDate from, LocalDate to) {
    List<League> leagues = leagueRepo.findAll();
    List<Matchup> savedMatchups = new ArrayList<>();

    for (League league : leagues) {
      List<Matchup> matches = this.createOrUpdateMatchups(league, from, to);
      savedMatchups.addAll(matches);
    }

    return savedMatchups;
  }

  /** Create/Update matches from target league. */
  @Transactional
  public List<Matchup> createOrUpdateMatchups(League league, LocalDate from, LocalDate to) {
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
        exist.setHomeTeam(matchup.getHomeTeam());
        exist.setAwayTeam(matchup.getAwayTeam());
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
        String teamCode = competitor.getOrDefault("teamCode", "").toString();
        String homeAway = competitor.getOrDefault("homeAway", "").toString();
        boolean isHome = homeAway.equals("home");
        Team team = teamRepo.getByCode(teamCode);
        if (isHome)
          matchup.setHomeTeam(team);
        else
          matchup.setAwayTeam(team);
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
  public List<Matchup> getCurrentWeekMatches() {
    LocalDate today = LocalDate.now();
    LocalDate from = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate to = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    List<Matchup> matches = new ArrayList<>();
    List<MatchDay> thisWeekMatchDays = matchDayRepo.findByDateRange(from, to);
    for (MatchDay matchDay : thisWeekMatchDays) {
      OffsetDateTime date = matchDay.getDate().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
      List<Matchup> matchups = matchupRepo.getByMatchDay(date, LEAGUE_CODE);
      if (matchups.isEmpty()) {
        log.info("No matches found on this date {}", date);
        continue;
      }
      matches.addAll(matchups);
    }

    return matches;
  }

  public League get() {
    League premierLeague = leagueRepo.getByCode(LEAGUE_CODE);
    if (Objects.isNull(premierLeague)) {
      log.warn("Premier League is not found");
      return null;
    }
    return premierLeague;
  }

  @Transactional
  public League create() {
    League exist = leagueRepo.getByCode(LEAGUE_CODE);
    if (Objects.nonNull(exist)) {
      log.warn("League: {} is already exist", exist.getName());
      return exist;
    } else {
      League premierLeague = leagueRepo.save(new League(LEAGUE_CODE, "Premier League"));
      log.info("League: {} created", premierLeague.getName());
      return premierLeague;
    }
  }

  @Transactional
  public List<Team> createOrUpdateTeams() {
    List<Team> teams = crawlerService.pullTeams(LEAGUE_CODE);
    if (teams.isEmpty())
      return Collections.emptyList();

    List<Team> toCreates = new ArrayList<>();
    List<Team> toUpdates = new ArrayList<>();

    League premierLeague = leagueRepo.getByCode(LEAGUE_CODE);
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
