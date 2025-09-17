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
      return ResponseEntity.ok(Response.SUCCESS("Bạn đã đăng ký", lookupId));
    }

    SubscriberService.ServiceResult result = subscriberService.subscribe(lookupId, name);
    League premierLeague = premierLeagueService.get();
    subscriberService.followLeague(lookupId, premierLeague.getCode());

    if (!result.success()) {
      return ResponseEntity.ok(Response.FAIL(result.message()));
    }
    return ResponseEntity.ok(Response.SUCCESS(result.message(), result.subscriber().getLookupId()));
  }

  @PostMapping("/unsubscribe")
  public ResponseEntity<Response> unsubscribe(@RequestParam("user_id") String lookupId) {
    boolean success = subscriberService.unsubscribe(lookupId);
    if (!success) {
      return ResponseEntity.ok(Response.FAIL("Hủy đăng ký không thành công", lookupId));
    }
    return ResponseEntity.ok(Response.SUCCESS("Hủy đăng ký thành công", lookupId));
  }

  @GetMapping("/status")
  public ResponseEntity<Response> getSubscriberStatus(@RequestParam("user_id") String lookupId) {
    DataRecord status = DataRecord.spawn();
    Subscriber subscriber = subscriberService.getSubscriber(lookupId);
    if (Objects.isNull(subscriber)) {
      return ResponseEntity.ok(Response.FAIL("Bạn chưa đăng ký"));
    }
    status
      .with("exist", true)
      .with("name", subscriber.getName())
      .with("following", subscriber.getFollowingLeagues().stream().map(League::getName).toList());
    return ResponseEntity.ok(Response.SUCCESS(String.format("Trạng thái người dùng %s", lookupId), status));
  }
}
