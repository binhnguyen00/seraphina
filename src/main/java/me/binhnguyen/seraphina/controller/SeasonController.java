package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/season")
public class SeasonController {
  private final SeasonService seasonService;
  private final PremierLeagueService premierLeagueService;

  @GetMapping("/schedule/matches")
  public ResponseEntity<Map<String, Object>> getScheduleMatches() {
    Season season = seasonService.getOrCreate();
    List<Matchup> thisWeekMatches = premierLeagueService.getMatches(season);
    if (thisWeekMatches.isEmpty()) {
      return ResponseEntity.ok(Map.of(
        "success", false,
        "message", "Tuần này chưa có trận đấu"
      ));
    }

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < thisWeekMatches.size(); i++) {
      Matchup match = thisWeekMatches.get(i);
      builder.append(String.format("""
      %d ⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️⚽️
      Đội nhà    %s
      Đội khách  %s
      Giờ đá     %s
      Địa điểm   %s
      
      """,
        i + 1,
        match.getHome(),
        match.getAway(),
        match.getFormatMatchDay(),
        match.getHomeStadium()
      ));
    }
    return ResponseEntity.ok(Map.of(
      "success", true,
      "message", String.format("Tuần này có %s trận đấu", thisWeekMatches.size()),
      "data", builder.toString()
    ));
  }
}
