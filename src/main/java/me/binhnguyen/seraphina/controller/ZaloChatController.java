package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.entity.RegisterChat;
import me.binhnguyen.seraphina.service.RegisterChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/zalo/chat")
public class ZaloChatController {
  private final RegisterChatService registerChatService;

  @PostMapping("/register")
  public boolean registerChat(@RequestBody Map<String, Object> request) {
    String lookupId   = String.valueOf(request.get("lookup_id"));
    String name       = String.valueOf(request.get("name"));
    RegisterChat chat = registerChatService.registerChat(lookupId, name);
    return !chat.isNew();
  }

  @PostMapping("/unregister")
  public boolean unregisterChat(@RequestParam("lookup_id") String lookupId) {
    return registerChatService.unregisterChat(lookupId);
  }

  @GetMapping("/health")
  public String healthCheck() {
    return "Healthy";
  }
}
