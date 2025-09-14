package me.binhnguyen.seraphina.service;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class CrawlerService {
  private final WebClient webClient;

  @Autowired
  public CrawlerService(@Qualifier("espnWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> pullMatchesByDateRange(String leagueCode, LocalDate from, LocalDate to) {
    String dates = String.format("%s-%s", from.toString().replace("-", ""), to.toString().replace("-", ""));

    Map<String, Object> response = webClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/{leagueCode}/scoreboard")
        .queryParam("dates", dates)
        .build(leagueCode))
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
        String teamCode = team.getOrDefault("abbreviation", "").toString();

        competitorMap.put("homeAway", homeAway);
        competitorMap.put("teamCode", teamCode);
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

  @SuppressWarnings("unchecked")
  public List<LocalDate> pullCurrentSeasonScheduleMatchDays(String leagueCode) {
    Map<String, Object> response = this.webClient.get()
      .uri("/{leagueId}/scoreboard", leagueCode)
      .retrieve()
      .bodyToMono(Map.class)
      .timeout(Duration.ofSeconds(10))
      .block();

    if (Objects.isNull(response)) {
      log.error("getCurrentSeasonScheduleMatchDays API has no response");
      return Collections.emptyList();
    }

    List<Map<String, Object>> leagues = (List<Map<String, Object>>) response.getOrDefault("leagues", Collections.emptyList());
    if (leagues.isEmpty()) log.error("getCurrentSeasonScheduleMatchDays API has no leagues");

    Map<String, Object> league = leagues.getFirst();
    List<LocalDate> dates = new ArrayList<>();
    List<String> calendar = (List<String>) league.getOrDefault("calendar", Collections.emptyList());
    if (calendar.isEmpty()) {
      log.error("getCurrentSeasonScheduleMatchDays API has no calendar");
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
  public List<Team> pullTeams(String leagueCode) {
    Map<String, Object> response = this.webClient.get()
      .uri("/{leagueId}/teams", leagueCode)
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
