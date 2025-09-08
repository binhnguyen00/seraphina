package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.service.ZaloChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/zalo/chat")
public class ZaloChatController {
  private final ZaloChatService zaloChatService;

  @PostMapping("/subscribe")
  public ResponseEntity<Map<String, Object>> subscribeChat(@RequestBody Map<String, Object> request) {
    String lookupId   = String.valueOf(request.get("chat_id"));
    String name       = String.valueOf(request.get("chat_name"));
    ZaloChat chat = zaloChatService.subscribeChat(lookupId, name);

    boolean success = !chat.isNew();
    if (!success) {
      return ResponseEntity.ok(Map.of(
        "success", false,
        "message", "Register chat failed"
      ));
    }
    return ResponseEntity.ok(Map.of(
      "success", true,
      "data", chat.getLookupId(),
      "message", "Chat registered successfully"
    ));
  }

  @PostMapping("/unsubscribe")
  public ResponseEntity<Map<String, Object>> unsubscribeChat(@RequestParam("chat_id") String chatId) {
    boolean success = zaloChatService.unsubscribeChat(chatId);
    if (!success) {
      ResponseEntity.ok(Map.of(
        "success", false,
        "message", "Unregister chat failed"
      ));
    }
    return ResponseEntity.ok(Map.of(
      "success", success,
      "message", "Chat unregistered successfully"
    ));
  }

  @GetMapping("/health")
  public String healthCheck() {
    return "Healthy";
  }
}
