package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.common.Response;
import me.binhnguyen.seraphina.entity.League;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.service.PremierLeagueService;
import me.binhnguyen.seraphina.service.SubscriberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/zalo/chat")
public class SubscriberController extends BaseController {
  private final SubscriberService subscriberService;
  private final PremierLeagueService premierLeagueService;

  @PostMapping("/subscribe")
  public ResponseEntity<Response> subscribe(@RequestBody DataRecord request) {
    String lookupId = String.valueOf(request.get("user_id"));
    String name = String.valueOf(request.get("chat_name"));

    Subscriber exist = subscriberService.getSubscriber(lookupId);
    if (Objects.nonNull(exist)) {
      return ResponseEntity.ok(new Response(
        false,
        "Chat already registered",
        null
      ));
    }

    SubscriberService.SubscribeResult result = subscriberService.subscribe(lookupId, name);
    League premierLeague = premierLeagueService.get();
    subscriberService.follow(lookupId, premierLeague.getCode());

    boolean success = !result.isNew();
    if (!success) {
      return ResponseEntity.ok(new Response(
        false,
        "Register user failed",
        null
      ));
    }
    return ResponseEntity.ok(new Response(
      true,
      "Registered successfully",
      result.subscriber().getLookupId()
    ));
  }

  @GetMapping("/subscribe/get")
  public ResponseEntity<DataRecord> getSubscriber(@RequestParam("user_id") String lookupId) {
    Subscriber subscriber = subscriberService.getSubscriber(lookupId);
    DataRecord response = new DataRecord();
    if (Objects.isNull(subscriber)) {
      return ResponseEntity.ok(
        response
          .with("success", false)
          .with("message", "Chat not found")
      );
    }
    return ResponseEntity.ok(
      response
        .with("success", true)
        .with("data", subscriber.getLookupId())
        .with("message", "Chat found")
    );
  }

  @PostMapping("/unsubscribe")
  public ResponseEntity<DataRecord> unsubscribeChat(@RequestParam("user_id") String lookupId) {
    boolean success = subscriberService.unsubscribe(lookupId);
    DataRecord response = new DataRecord();
    if (!success) {
      ResponseEntity.ok(
        response
          .with("success", false)
          .with("message", "Unregister chat failed")
      );
    }
    return ResponseEntity.ok(
      response
        .with("success", true)
        .with("message", "Chat unregistered successfully")
    );
  }
  
  @PostMapping("/follow")
  public ResponseEntity<DataRecord> followLeague(
    @RequestParam("user_id") String lookupId,
    @RequestParam("league_code") String leagueCode
  ) {
    return null;
  }
}
