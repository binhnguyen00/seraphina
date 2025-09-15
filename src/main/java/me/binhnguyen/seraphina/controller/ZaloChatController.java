package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.common.DataRecord;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.service.ZaloChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/zalo/chat")
public class ZaloChatController extends BaseController {
  private final ZaloChatService zaloChatService;

  @PostMapping("/subscribe")
  public ResponseEntity<DataRecord> subscribeChat(@RequestBody DataRecord request) {
    String lookupId = String.valueOf(request.get("chat_id"));
    String name = String.valueOf(request.get("chat_name"));
    DataRecord response = new DataRecord();

    ZaloChat exist = zaloChatService.getSubscriber(lookupId);
    if (Objects.nonNull(exist)) {
      return ResponseEntity.ok(
        response
          .with("success", false)
          .with("message", "Chat already registered")
      );
    }

    ZaloChat chat = zaloChatService.subscribe(lookupId, name);

    boolean success = !chat.isNew();
    if (!success) {
      return ResponseEntity.ok(
        response
          .with("success", false)
          .with("message", "Register chat failed")
        );
    }
    return ResponseEntity.ok(
      response
        .with("success", true)
        .with("data", chat.getLookupId())
        .with("message", "Chat registered successfully")
    );
  }

  @GetMapping("/subscribe/get")
  public ResponseEntity<DataRecord> getSubscriber(@RequestParam("chat_id") String chatId) {
    ZaloChat chat = zaloChatService.getSubscriber(chatId);
    DataRecord response = new DataRecord();
    if (Objects.isNull(chat)) {
      return ResponseEntity.ok(
        response
          .with("success", false)
          .with("message", "Chat not found")
      );
    }
    return ResponseEntity.ok(
      response
        .with("success", true)
        .with("data", chat.getLookupId())
        .with("message", "Chat found")
    );
  }

  @PostMapping("/unsubscribe")
  public ResponseEntity<DataRecord> unsubscribeChat(@RequestParam("chat_id") String chatId) {
    boolean success = zaloChatService.unsubscribe(chatId);
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
}
