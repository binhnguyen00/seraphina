package me.binhnguyen.seraphina.service;

import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.entity.Season;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
  private final String baseUrl;
  private final String leagueId;
  private final RestTemplate restTemplate;

  @Autowired
  public CrawlerService(RestTemplate restTemplate) {
    this.baseUrl = "https://site.api.espn.com/apis/site/v2/sports/soccer";
    this.leagueId = "eng.1"; // Premier League
    this.restTemplate = restTemplate;
  }

  private DataRecord callApi(String url) {
    ResponseEntity<DataRecord> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
    });
    DataRecord body = response.getBody();
    if (Objects.isNull(body)) {
      log.error("API response is undefined");
      return new DataRecord();
    }
    return body;
  }

  @SuppressWarnings("unchecked")
  public List<DataRecord> pullScheduleMatches(String dates) {
    Objects.requireNonNull(dates, "Dates is required");
    if (dates.isBlank()) {
      log.error("Dates is empty or blank");
      return new ArrayList<>();
    }

    final String url = String.format("%s/%s/scoreboard", this.baseUrl, this.leagueId);
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
    builder.queryParam("dates", dates);
    DataRecord body = this.callApi(builder.build().toUriString());
    ArrayList<DataRecord> matches = (ArrayList<DataRecord>) body.getOrDefault("events", new ArrayList<>());
    if (matches.isEmpty()) {
      log.error("Has no schedule matches");
      return new ArrayList<>();
    }

    List<DataRecord> matchesHolder = new ArrayList<>();
    matches.forEach(match -> {
      DataRecord simplified = new DataRecord();

      String label = match.getOrDefault("name", "").toString();
      String startTime = match.getOrDefault("date", "").toString();

      // competitors
      ArrayList<DataRecord> competitions = (ArrayList<DataRecord>) match.getOrDefault("competitions", new ArrayList<>());
      ArrayList<DataRecord> competitors = (ArrayList<DataRecord>) competitions.getFirst().getOrDefault("competitors", new ArrayList<>());

      List<DataRecord> competitorsList = new ArrayList<>();
      competitors.forEach(competitor -> {
        DataRecord competitorMap = new DataRecord();
        String homeAway = competitor.getOrDefault("homeAway", "").toString();
        DataRecord team = (DataRecord) competitor.getOrDefault("team", new DataRecord());
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

  /**
   * pull this week matches
   */
  public List<DataRecord> pullMatches(Season season) {
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

  /**
   * pull matches by date range
   */
  public List<DataRecord> pullMatchesByDate(Season season, LocalDate from, LocalDate to) {
    Objects.requireNonNull(season, "Season is required");
    Objects.requireNonNull(from, "From date is required");
    Objects.requireNonNull(to, "To date is required");
    // target result: "20250913-20250914"
    String dates = String.format("%s-%s", from.toString().replace("-", ""), to.toString().replace("-", ""));
    return this.pullScheduleMatches(dates);
  }

  @SuppressWarnings("unchecked")
  public List<LocalDate> getCurrentSeasonScheduleMatchDays() {
    String url = String.format("%s/%s/scoreboard", this.baseUrl, this.leagueId);
    ResponseEntity<DataRecord> response = restTemplate.exchange(
      url,
      HttpMethod.GET,
      null,
      new ParameterizedTypeReference<>() {
      }
    );
    DataRecord body = response.getBody();
    if (Objects.isNull(body)) {
      log.error("Schedule dates are not found");
      return Collections.emptyList();
    }

    List<DataRecord> leagues = (List<DataRecord>) body.getOrDefault("leagues", new ArrayList<>());
    if (leagues.isEmpty()) log.error("Has no leagues");

    DataRecord league = leagues.getFirst();
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
