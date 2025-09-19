package me.binhnguyen.seraphina.controller;

import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.common.Response;
import me.binhnguyen.seraphina.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** URL Endpoint <code> /api/v1/league/laliga </code> */
@RestController
@RequestMapping(LeagueController.REST_URL + "/laliga")
public class LaligaController extends LeagueController {

  @Autowired
  public LaligaController(
    SeasonService seasonService,
    SubscriberService subscriberService,
    LaligaService laligaService,
    PremierLeagueService premierLeagueService,
    ChampionLeagueService championLeagueService
  ) {
    super(seasonService, subscriberService, laligaService, premierLeagueService, championLeagueService);
  }

  @PostMapping("/follow")
  public ResponseEntity<Response> followLeague(@RequestBody DataRecord request) {
    String lookupId = String.valueOf(request.get("user_id"));
    String leagueCode = String.valueOf(request.getOrDefault("league_code", "esp.1"));

    SubscriberService.ServiceResult result = subscriberService.followLeague(lookupId, leagueCode);
    if (!result.success()) {
      return ResponseEntity.ok(Response.FAIL(result.message()));
    }
    return ResponseEntity.ok(Response.SUCCESS(result.message()));
  }

  @PostMapping("unfollow")
  public ResponseEntity<Response> unfollowLeague(@RequestBody DataRecord request) {
    String lookupId = String.valueOf(request.get("user_id"));
    String leagueCode = String.valueOf(request.getOrDefault("league_code", "esp.1"));

    SubscriberService.ServiceResult result = subscriberService.unfollowLeague(lookupId, leagueCode);
    if (!result.success()) {
      return ResponseEntity.ok(Response.FAIL(result.message()));
    }
    return ResponseEntity.ok(Response.SUCCESS(result.message()));
  }
}
