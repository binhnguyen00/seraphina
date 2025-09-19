package me.binhnguyen.seraphina.controller;

import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.common.Response;
import me.binhnguyen.seraphina.service.LaligaService;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SeasonService;
import me.binhnguyen.seraphina.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** URL Endpoint <code> /api/v1/league/champion-league </code> */
@RestController
@RequestMapping(LeagueController.REST_URL + "/champion-league")
public class ChampionLeagueController extends LeagueController {

  @Autowired
  public ChampionLeagueController(
    SeasonService seasonService,
    SubscriberService subscriberService,
    LaligaService laligaService,
    PremierLeagueService premierLeagueService
  ) {
    super(seasonService, subscriberService, laligaService, premierLeagueService);
  }

  @PostMapping("/follow")
  public ResponseEntity<Response> followLeague(@RequestBody DataRecord request) {
    String lookupId = String.valueOf(request.get("user_id"));
    String leagueCode = String.valueOf(request.getOrDefault("league_code", "uefa.champion"));

    SubscriberService.ServiceResult result = subscriberService.followLeague(lookupId, leagueCode);
    if (!result.success()) {
      return ResponseEntity.ok(Response.FAIL(result.message()));
    }
    return ResponseEntity.ok(Response.SUCCESS(result.message()));
  }

  @PostMapping("/unfollow")
  public ResponseEntity<Response> unfollowLeague(@RequestBody DataRecord request) {
    String lookupId = String.valueOf(request.get("user_id"));
    String leagueCode = String.valueOf(request.getOrDefault("league_code", "uefa.champion"));

    SubscriberService.ServiceResult result = subscriberService.unfollowLeague(lookupId, leagueCode);
    if (!result.success()) {
      return ResponseEntity.ok(Response.FAIL(result.message()));
    }
    return ResponseEntity.ok(Response.SUCCESS(result.message()));
  }
}
