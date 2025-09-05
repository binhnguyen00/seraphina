package me.binhnguyen.seraphina.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.entity.Season;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CrawlerService {
  private final String baseUrl = "https://site.api.espn.com/apis/site/v2/sports/soccer";
  private final String leagueId = "eng.1";
  private final RestTemplate restTemplate = new RestTemplate();

  /** pull this week matches */
  public List<HashMap<String, Object>> pullScheduleMatches(@NonNull Season season) {
    String url = String.format("%s/%s/scoreboard", this.baseUrl, this.leagueId);
    List<LocalDate> thisWeekMatchDays = season.getThisWeekMatchDays();
    if (thisWeekMatchDays.isEmpty()) {
      log.error("This weekend has no matches");
      return new ArrayList<>();
    }
    log.info("This week match days: {}", thisWeekMatchDays);

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
    String dates = thisWeekMatchDays
      .stream()
      .map(date -> date.toString().replace("-", ""))
      .collect(Collectors.joining("-")); // matchesHolder: "20250913-20250914"
    builder.queryParam("dates", dates);

    ResponseEntity<HashMap> response = restTemplate.getForEntity(builder.toUriString(), HashMap.class);
    HashMap<String, Object> body = response.getBody();
    if (Objects.isNull(body)) {
      log.error("Schedule matches are not found");
      return new ArrayList<>();
    }

    ArrayList<HashMap<String, Object>> matches = (ArrayList<HashMap<String, Object>>) body.getOrDefault("events", new ArrayList<>());
    if (matches.isEmpty()) {
      log.error("Has no schedule matches");
      return new ArrayList<>();
    }

    List<HashMap<String, Object>> matchesHolder = new ArrayList<>();
    matches.forEach(match -> {
      HashMap<String, Object> simplified = new HashMap<>();

      String label = match.getOrDefault("name", "").toString();
      String startTime = match.getOrDefault("date", "").toString();

      // competitors
      ArrayList<HashMap<String, Object>> competitions = (ArrayList<HashMap<String, Object>>) match.getOrDefault("competitions", new ArrayList<>());
      ArrayList<HashMap<String, Object>> competitors = (ArrayList<HashMap<String, Object>>) competitions.getFirst().getOrDefault("competitors", new ArrayList<>());

      List<HashMap<String, Object>> competitorsList = new ArrayList<>();
      competitors.forEach(competitor -> {
        HashMap<String, Object> competitorMap = new HashMap<>();
        String homeAway = competitor.getOrDefault("homeAway", "").toString();
        HashMap<String, Object> team = (HashMap<String, Object>) competitor.getOrDefault("team", new HashMap<>());
        String teamName = team.getOrDefault("name", "").toString();

        competitorMap.put("homeAway", homeAway);
        competitorMap.put("teamName", teamName);
        competitorsList.add(competitorMap);
      });

      // home stadium
      HashMap<String, String> venue = (HashMap<String, String>) match.getOrDefault("venue", new HashMap<String, String>());
      String homeStadium = venue.getOrDefault("displayName", "");

      simplified.put("label", label);
      simplified.put("startTime", startTime);
      simplified.put("homeStadium", homeStadium);
      simplified.put("competitors", competitorsList);

      matchesHolder.add(simplified);
    });

    return matchesHolder;
  }

  public ArrayList<LocalDate> getCurrentSeasonScheduleMatchDays() {
    String url = String.format("%s/%s/scoreboard", this.baseUrl, this.leagueId);
    ResponseEntity<HashMap> response = restTemplate.getForEntity(url, HashMap.class);
    HashMap<String, Object> body = response.getBody();
    if (Objects.isNull(body)) log.error("Schedule dates are not found");

    ArrayList<HashMap<String, Object>> leagues = (ArrayList<HashMap<String, Object>>) body.getOrDefault("leagues", new ArrayList<>());
    if (leagues.isEmpty()) log.error("Has no leagues");

    HashMap<String, Object> league = leagues.getFirst();
    ArrayList<LocalDate> dates = new ArrayList<>();
    ArrayList<String> calendar = (ArrayList<String>) league.getOrDefault("calendar", new ArrayList<>());
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
