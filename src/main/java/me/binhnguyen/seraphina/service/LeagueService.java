package me.binhnguyen.seraphina.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.*;
import me.binhnguyen.seraphina.repository.LeagueRepo;
import me.binhnguyen.seraphina.repository.MatchDayRepo;
import me.binhnguyen.seraphina.repository.MatchupRepo;
import me.binhnguyen.seraphina.repository.TeamRepo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public abstract class LeagueService {
  protected final LeagueRepo leagueRepo;
  protected final TeamRepo teamRepo;
  protected final MatchupRepo matchupRepo;
  protected final MatchDayRepo matchDayRepo;
  protected final CrawlerService crawlerService;
  protected final SeasonService seasonService;

  protected abstract String getCode();

  @Transactional
  public abstract League create();

  public League get() {
    final String LEAGUE_CODE = this.getCode();
    return leagueRepo.getByCode(LEAGUE_CODE);
  }

  public List<MatchDay> getThisWeekMatchDays() {
    final LocalDate today = LocalDate.now();
    final LocalDate THIS_MONDAY = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    final LocalDate THIS_SUNDAY = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    final String LEAGUE_CODE = this.getCode();

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
    String code = this.getCode();
    Season season = seasonService.getCurrentSeason();
    List<MatchDay> matchDays = matchDayRepo.findByLeagueAndYear(code, season.getYear());
    if (matchDays.isEmpty()) {
      log.error("Found no match day");
      return Collections.emptyList();
    }
    return matchDays;
  }

  /** create/update this season match days */
  public List<MatchDay> createOrUpdateAllMatchDays() {
    final League league = this.get();
    List<MatchDay> matchDays = this.getAllMatchDays();
    List<LocalDate> schedules = crawlerService.pullCurrentSeasonScheduleMatchDays(league.getCode());

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

    try {
      holder = matchDayRepo.saveAll(holder);
      log.info("{} create/update {} match days", league.getName(), holder.size());
      return holder;
    } catch (Exception e) {
      log.error("Failed to create/update match days", e);
      return Collections.emptyList();
    }
  }

  /** Create/Update matches from target league. */
  @Transactional
  public List<Matchup> createOrUpdateMatchups(LocalDate from, LocalDate to) {
    final League league = this.get();
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
        exist.setLeague(league);
        exist.setHomeTeam(matchup.getHomeTeam());
        exist.setAwayTeam(matchup.getAwayTeam());
        exist.setMatchDay(matchup.getMatchDay());
        toUpdates.add(exist);
      }
    }

    if (!toCreates.isEmpty()) savedMatchups.addAll(matchupRepo.saveAll(toCreates));
    log.info("{} Created {} matches", league.getName(), toCreates.size());
    if (!toUpdates.isEmpty()) savedMatchups.addAll(matchupRepo.saveAll(toUpdates));
    log.info("{} Updated {} matches", league.getName(), toUpdates.size());

    return savedMatchups;
  }

  @SuppressWarnings("unchecked")
  public List<Matchup> toMatchups(List<Map<String, Object>> matches) {
    final League league = this.get();
    final Season season = seasonService.getCurrentSeason();
    List<Matchup> matchups = new ArrayList<>();
    for (Map<String, Object> match : matches) {
      Matchup matchup = new Matchup();
      List<Map<String, Object>> competitors = (List<Map<String, Object>>) match.get("competitors");
      for (Map<String, Object> competitor : competitors) {
        String teamCode = competitor.getOrDefault("teamCode", "").toString().toLowerCase();
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
      matchup.setSeason(season);
      matchup.setLeague(league);
      matchups.add(matchup);
    }

    return matchups;
  }

  /** returns current week scheduled matches */
  public List<Matchup> getCurrentWeekMatches() {
    final League league = this.get();
    List<Matchup> matches = new ArrayList<>();
    List<MatchDay> thisWeekMatchDays = this.getThisWeekMatchDays();
    for (MatchDay matchDay : thisWeekMatchDays) {
      OffsetDateTime start = matchDay.getDate().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
      OffsetDateTime end = start.plusDays(1).minusNanos(1);
      List<Matchup> matchups = matchupRepo.getByMatchDay(start, end, league.getCode());
      if (matchups.isEmpty()) {
        log.info("{} No matches found on this date {}", league.getName(), start);
        continue;
      }
      matches.addAll(matchups);
    }

    return matches;
  }

  @Transactional
  public List<Team> createOrUpdateTeams() {
    final League league = this.get();
    List<Team> teams = crawlerService.pullTeams(league.getCode());
    if (teams.isEmpty())
      return Collections.emptyList();

    List<Team> toCreates = new ArrayList<>();
    List<Team> toUpdates = new ArrayList<>();

    teams.forEach(team -> {
      Team exist = teamRepo.getByCode(team.getCode());
      if (Objects.isNull(exist)) {
        team.setLeague(league);
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
    log.info("{} Created {} teams", league.getName(), toCreates.size());
    log.info("{} Updated {} teams", league.getName(), toUpdates.size());
    return savedTeams;
  }
}
