package me.binhnguyen.seraphina.service;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Season;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CrawlerService {
  private final String leagueId;
  private final WebClient webClient;

  @Autowired
  public CrawlerService(@Qualifier("premierLeague") WebClient webClient) {
    this.leagueId = "eng.1";
    this.webClient = webClient;
  }

  @SuppressWarnings("unchecked")
  public List<java.util.Map<String, Object>> pullScheduleMatches(String dates) {
    Objects.requireNonNull(dates, "Dates is required");
    if (dates.isBlank()) {
      log.error("Dates is empty or blank");
      return new ArrayList<>();
    }

    Map<String, Object> response = webClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/{leagueId}/scoreboard")
        .queryParam("dates", dates)
        .build(leagueId))
      .retrieve()
      .bodyToMono(Map.class)
      .timeout(Duration.ofSeconds(10))
      .block();

    if (Objects.isNull(response)) {
      log.error("pullScheduleMatches API has no response");
      return new ArrayList<>();
    }
    ArrayList<Map<String, Object>> matches = (ArrayList<Map<String, Object>>) response.getOrDefault("events", new ArrayList<>());
    if (matches.isEmpty()) {
      log.error("Has no schedule matches");
      return new ArrayList<>();
    }

    List<Map<String, Object>> matchesHolder = new ArrayList<>();
    matches.forEach(match -> {
      Map<String, Object> simplified = new HashMap<>();

      String label = match.getOrDefault("name", "").toString();
      String startTime = match.getOrDefault("date", "").toString();

      // competitors
      ArrayList<Map<String, Object>> competitions = (ArrayList<Map<String, Object>>) match.getOrDefault("competitions", new ArrayList<>());
      ArrayList<Map<String, Object>> competitors = (ArrayList<Map<String, Object>>) competitions.getFirst().getOrDefault("competitors", new ArrayList<>());

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

  /** pull this week matches */
  public List<Map<String, Object>> pullMatches(Season season) {
    Objects.requireNonNull(season, "Season is required");

    List<LocalDate> thisWeekMatchDays = season.getThisWeekMatchDays();
    if (thisWeekMatchDays.isEmpty()) {
      log.error("This weekend has no match days");
      return new ArrayList<>();
    }
    String dates = thisWeekMatchDays
      .stream()
      .map(date -> date.toString().replace("-", ""))
      .collect(Collectors.joining("-"));
    return this.pullScheduleMatches(dates);
  }

  /** pull matches by date range */
  public List<Map<String, Object>> pullMatchesByDate(Season season, LocalDate from, LocalDate to) {
    Objects.requireNonNull(season, "Season is required");
    Objects.requireNonNull(from, "From date is required");
    Objects.requireNonNull(to, "To date is required");
    // target result: "20250913-20250914"
    String dates = String.format("%s-%s", from.toString().replace("-", ""), to.toString().replace("-", ""));
    return this.pullScheduleMatches(dates);
  }

  @SuppressWarnings("unchecked")
  public List<LocalDate> getCurrentSeasonScheduleMatchDays() {
    Map<String, Object> response = this.webClient.get()
      .uri("/{leagueId}/scoreboard", this.leagueId)
      .retrieve()
      .bodyToMono(Map.class)
      .timeout(Duration.ofSeconds(10))
      .block();

    if (Objects.isNull(response)) {
      log.error("getCurrentSeasonScheduleMatchDays API has no response");
      return Collections.emptyList();
    }

    List<Map<String, Object>> leagues = (List<Map<String, Object>>) response.getOrDefault("leagues", new ArrayList<>());
    if (leagues.isEmpty()) log.error("getCurrentSeasonScheduleMatchDays has no leagues");

    Map<String, Object> league = leagues.getFirst();
    List<LocalDate> dates = new ArrayList<>();
    List<String> calendar = (List<String>) league.getOrDefault("calendar", new ArrayList<>());
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
}
