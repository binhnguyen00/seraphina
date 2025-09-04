package me.binhnguyen.seraphina.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class CrawlerService {
  private final String baseUrl = "https://site.api.espn.com/apis/site/v2/sports/soccer";
  private final String leagueId = "eng.1";
  private final RestTemplate restTemplate = new RestTemplate();

  @Scheduled(cron = "0 * * * * *")
  public void pullScheduleMatches() {
    String url = String.format("%s/%s/scoreboard", this.baseUrl, this.leagueId);
    ResponseEntity<HashMap> response = restTemplate.getForEntity(url, HashMap.class);

    HashMap<String, Object> body = response.getBody();
    if (body == null) log.error("Data is not found");

    ArrayList<HashMap<String, Object>> leagues = (ArrayList<HashMap<String, Object>>) body.getOrDefault("leagues", new ArrayList<>());
    if (leagues.isEmpty()) log.error("Has no leagues");

    HashMap<String, Object> league = leagues.getFirst();
    ArrayList<LocalDate> matchDays = this.getScheduleDates(league);
    matchDays.forEach(System.out::println);
  }

  private ArrayList<LocalDate> getScheduleDates(HashMap<String, Object> league) {
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
