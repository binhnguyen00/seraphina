package me.binhnguyen.seraphina.service;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.MatchDay;
import me.binhnguyen.seraphina.entity.Team;
import me.binhnguyen.seraphina.repository.MatchDayRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@Service
public class CrawlerService {
  private final WebClient webClient;
  private final MatchDayRepo matchDayRepo;

  @Autowired
  public CrawlerService(
    @Qualifier("premierLeague") WebClient webClient,
    MatchDayRepo matchDayRepo
  ) {
    this.webClient = webClient;
    this.matchDayRepo = matchDayRepo;
  }

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> pullMatchesByDateRange(String leagueId, LocalDate from, LocalDate to) {
    String dates = String.format("%s-%s", from.toString().replace("-", ""), to.toString().replace("-", ""));

    Map<String, Object> response = webClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/{leagueId}/scoreboard")
        .queryParam("dates", dates)
        .build(leagueId))
      .retrieve()
      .bodyToMono(Map.class)
      .timeout(Duration.ofSeconds(20))
      .block();

    if (Objects.isNull(response)) {
      log.error("pullScheduleMatches API has no response");
      return Collections.emptyList();
    }
    ArrayList<Map<String, Object>> matches = (ArrayList<Map<String, Object>>) response.getOrDefault("events", Collections.emptyList());
    if (matches.isEmpty()) {
      log.error("Has no schedule matches");
      return Collections.emptyList();
    }

    List<Map<String, Object>> matchesHolder = new ArrayList<>();
    matches.forEach(match -> {
      Map<String, Object> simplified = new HashMap<>();

      String label = match.getOrDefault("name", "").toString();
      String startTime = match.getOrDefault("date", "").toString();

      // competitors
      ArrayList<Map<String, Object>> competitions = (ArrayList<Map<String, Object>>) match.getOrDefault("competitions", Collections.emptyList());
      ArrayList<Map<String, Object>> competitors = (ArrayList<Map<String, Object>>) competitions.getFirst().getOrDefault("competitors", Collections.emptyList());

      List<Map<String, Object>> competitorsList = new ArrayList<>();
      competitors.forEach(competitor -> {
        Map<String, Object> competitorMap = new HashMap<>();
        String homeAway = competitor.getOrDefault("homeAway", "").toString();
        Map<String, Object> team = (Map<String, Object>) competitor.getOrDefault("team", new HashMap<>());
        String teamName = team.getOrDefault("name", "").toString();

        competitorMap.put("homeAway", homeAway);
        competitorMap.put("teamName", teamName);
        competitorsList.add(competitorMap);
      });

      // home stadium
      Map<String, String> venue = (Map<String, String>) match.getOrDefault("venue", new HashMap<String, String>());
      String homeStadium = venue.getOrDefault("displayName", "");

      simplified.put("label", label);
      simplified.put("startTime", startTime);
      simplified.put("homeStadium", homeStadium);
      simplified.put("competitors", competitorsList);

      matchesHolder.add(simplified);
    });

    return matchesHolder;
  }

  /** pull this week matches from target league */
  public List<Map<String, Object>> pullCurrentWeekMatches(String leagueId) {
    Objects.requireNonNull(leagueId, "League is required");

    List<MatchDay> allMatchDays = matchDayRepo.getMatchDay(leagueId, LocalDate.now().getYear());
    if (allMatchDays.isEmpty()) {
      log.error("League {} in this weekend has no match days", leagueId);
      return Collections.emptyList();
    }

    LocalDate today = LocalDate.now();
    LocalDate thisSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    LocalDate thisSunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    List<MatchDay> thisWeekMatchDays = allMatchDays.stream()
      .filter(matchDay -> matchDay.getDate().isAfter(today))
      .filter(matchDay -> matchDay.getDate().equals(thisSaturday) || matchDay.getDate().equals(thisSunday))
      .toList();

    return this.pullMatchesByDateRange(
      leagueId,
      thisWeekMatchDays.getFirst().getDate(),
      thisWeekMatchDays.getLast().getDate()
    );
  }

  @SuppressWarnings("unchecked")
  public List<LocalDate> pullCurrentSeasonScheduleMatchDays(String leagueId) {
    Map<String, Object> response = this.webClient.get()
      .uri("/{leagueId}/scoreboard", leagueId)
      .retrieve()
      .bodyToMono(Map.class)
      .timeout(Duration.ofSeconds(10))
      .block();

    if (Objects.isNull(response)) {
      log.error("getCurrentSeasonScheduleMatchDays API has no response");
      return Collections.emptyList();
    }

    List<Map<String, Object>> leagues = (List<Map<String, Object>>) response.getOrDefault("leagues", Collections.emptyList());
    if (leagues.isEmpty()) log.error("getCurrentSeasonScheduleMatchDays has no leagues");

    Map<String, Object> league = leagues.getFirst();
    List<LocalDate> dates = new ArrayList<>();
    List<String> calendar = (List<String>) league.getOrDefault("calendar", Collections.emptyList());
    if (calendar.isEmpty()) {
      log.error("Has no calendar");
      return dates;
    }
    calendar.forEach(date -> {
      date = date.substring(0, 10);
      LocalDate localDate = LocalDate.parse(date);
      dates.add(localDate);
    });
    return dates;
  }

  @SuppressWarnings("unchecked")
  public List<Team> pullTeams(String leagueId) {
    Map<String, Object> response = this.webClient.get()
      .uri("/{leagueId}/teams", leagueId)
      .retrieve()
      .bodyToMono(Map.class)
      .timeout(Duration.ofSeconds(10))
      .block();
    if (Objects.isNull(response)) {
      log.error("pullTeams API has no response");
      return Collections.emptyList();
    }

    List<Map<String, Object>> sports = (List<Map<String, Object>>) response.getOrDefault("sports", Collections.emptyList());
    if (sports.isEmpty()) {
      log.error("pullTeams API has no sports");
      return Collections.emptyList();
    }

    List<Map<String, Object>> leagues = (List<Map<String, Object>>) sports.getFirst().getOrDefault("leagues", Collections.emptyList());
    if (leagues.isEmpty()) {
      log.error("pullTeams API has no leagues");
      return Collections.emptyList();
    }

    List<Map<String, Object>> teams = (List<Map<String, Object>>) leagues.getFirst().getOrDefault("teams", Collections.emptyList());
    if (teams.isEmpty()) {
      log.error("pullTeams API has no teams");
      return Collections.emptyList();
    }

    List<Team> results = new ArrayList<>();
    for (Map<String, Object> team : teams) {
      Map<String, Object> data = (Map<String, Object>) team.getOrDefault("team", new HashMap<>());
      String lookupId = String.valueOf(data.getOrDefault("id", ""));
      if (lookupId.isBlank()) {
        continue;
      }
      String name = String.valueOf(data.getOrDefault("name", ""));
      String code = String.valueOf(data.getOrDefault("abbreviation", ""));
      Team t = new Team(code, name);
      t.setLookupId(lookupId);
      results.add(t);
    }

    if (results.isEmpty())
      return Collections.emptyList();

    return results;
  }
}
