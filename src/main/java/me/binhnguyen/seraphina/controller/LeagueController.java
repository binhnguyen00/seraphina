package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.common.Response;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Matchup;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.service.LaligaService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import me.binhnguyen.seraphina.service.SubscriberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(LeagueController.REST_URL)
public class LeagueController extends BaseController {
  /** URL Endpoint <code> /api/v1/league </code> */
  public static final String REST_URL = BaseController.REST_URL + "/league";

  protected final SeasonService seasonService;
  protected final SubscriberService subscriberService;
  protected final LaligaService laligaService;
  protected final PremierLeagueService premierLeagueService;

  @GetMapping("/schedule/matches")
  public ResponseEntity<Response> getScheduleMatches(@RequestParam("user_id") String lookupId) {
    Subscriber exist = subscriberService.getSubscriber(lookupId);
    if (Objects.isNull(exist)) {
      return ResponseEntity.ok(Response.FAIL("Bạn chưa đăng ký", lookupId));
    }

    List<League> following = exist.getFollowingLeagues().stream().toList();
    List<Matchup> matchups = new ArrayList<>();
    for (League league : following) {
      switch (league.getCode()) {
        case "eng.1" -> matchups.addAll(premierLeagueService.getCurrentWeekMatches());
        case "esp.1" -> matchups.addAll(laligaService.getCurrentWeekMatches());
        default -> log.warn("League {} is not supported", league.getCode());
      }
    }
    if (matchups.isEmpty()) {
      return ResponseEntity.ok(Response.FAIL("Tuần này chưa có lịch thi đấu"));
    }

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < matchups.size(); i++) {
      Matchup match = matchups.get(i);
      builder.append(String.format("""
      %d ⚽️
      %s vs %s
      %s
      Sân %s
      
      """,
        i + 1,
        match.getHomeTeam().getName(),
        match.getAwayTeam().getName(),
        match.getFormatMatchDay(),
        match.getHomeStadium()
      ));
    }
    return ResponseEntity.ok(Response.SUCCESS(
      String.format("Tuần này có %s trận đấu", matchups.size()),
      builder.toString()
    ));
  }
}
