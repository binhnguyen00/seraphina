package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.common.DataRecord;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/premier-league")
public class PremierLeagueController extends BaseController {
  private final SeasonService seasonService;
  private final ZaloChatService zaloChatService;
  private final PremierLeagueService premierLeagueService;

  @GetMapping("/schedule/matches")
  public ResponseEntity<DataRecord> getScheduleMatches(@RequestParam("chat_id") String chatId) {
    DataRecord response = new DataRecord();
    ZaloChat exist = zaloChatService.getSubscriber(chatId);
    if (Objects.isNull(exist)) {
      return ResponseEntity.ok(
        response
          .with("success", false)
          .with("message", "Bạn chưa đăng ký!")
      );
    }

    int year = LocalDate.now().getYear();
    Season season = seasonService.getSeason(year);
    List<Matchup> thisWeekMatches = premierLeagueService.getCurrentWeekMatches(season);
    if (thisWeekMatches.isEmpty()) {
      return ResponseEntity.ok(
        response
          .with("success", false)
          .with("message", "Tuần này chưa có trận đấu")
      );
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
    return ResponseEntity.ok(
      response
        .with("success", true)
        .with("message", String.format("Tuần này có %s trận đấu", thisWeekMatches.size()))
        .with("data", builder.toString())
    );
  }
}
