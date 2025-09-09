package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Season;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import me.binhnguyen.seraphina.service.ZaloChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/premier-league")
public class PremierLeagueController {
  private final SeasonService seasonService;
  private final ZaloChatService zaloChatService;
  private final PremierLeagueService premierLeagueService;

  @GetMapping("/schedule/matches")
  public ResponseEntity<Map<String, Object>> getScheduleMatches(@RequestParam("chat_id") String chatId) {
    ZaloChat exist = zaloChatService.getSubscriber(chatId);
    if (Objects.isNull(exist)) {
      return ResponseEntity.ok(Map.of(
        "success", false,
        "message", "Bạn chưa đăng ký!"
      ));
    }

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
      %d ⚽️
      %s vs %s
      %s
      Sân %s
      
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
