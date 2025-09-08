package me.binhnguyen.seraphina.controller;

import lombok.RequiredArgsConstructor;
import me.binhnguyen.seraphina.service.RegisterChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/zalo/chat")
public class ZaloChatController {
  private final RegisterChatService registerChatService;

  @PostMapping("/register")
  public void registerChat(@RequestBody Map<String, Object> request) {
    String lookupId   = String.valueOf(request.get("lookup_id"));
    String name       = String.valueOf(request.get("name"));
    registerChatService.registerChat(lookupId, name);
  }

  @PostMapping("/unregister")
  public void unregisterChat(@RequestParam("lookup_id") String lookupId) {
    registerChatService.unregisterChat(lookupId);
  }
}
