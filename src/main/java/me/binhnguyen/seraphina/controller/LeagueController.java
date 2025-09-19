package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.common.Response;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
  protected final ChampionLeagueService championLeagueService;

  /** Get current week's matches */
  @GetMapping("/schedule/matches")
  public ResponseEntity<Response> getScheduleMatches(@RequestParam("user_id") String lookupId) {
    Subscriber exist = subscriberService.getSubscriber(lookupId);
    if (Objects.isNull(exist)) {
      return ResponseEntity.ok(Response.FAIL("Bạn chưa đăng ký", lookupId));
    }

    List<League> following = exist.getFollowingLeagues().stream().toList();
    if (following.isEmpty()) {
      return ResponseEntity.ok(Response.FAIL("Bạn chưa theo dõi giải đấu nào"));
    }

    final LocalDate today = LocalDate.now();
    final LocalDate MONDAY = today.with(DayOfWeek.MONDAY);
    final LocalDate SUNDAY = today.with(DayOfWeek.SUNDAY);
    List<DataRecord> leagues = subscriberService.getLeagues(exist, MONDAY, SUNDAY);
    if (leagues.isEmpty()) {
      return ResponseEntity.ok(Response.FAIL("Không có trận đấu nào"));
    }

    return ResponseEntity.ok(
      Response.SUCCESS("Get Leagues's matches success", leagues)
    );
  }
}
