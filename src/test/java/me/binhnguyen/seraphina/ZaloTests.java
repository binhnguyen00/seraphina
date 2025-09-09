package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.controller.ZaloChatController;
import me.binhnguyen.seraphina.entity.ZaloChat;
import me.binhnguyen.seraphina.service.ZaloChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZaloTests {

  @Autowired
  private ZaloChatController controller;

  @Autowired
  private ZaloChatService service;

  private ZaloChat chat;

  @BeforeEach
  void init() {
    this.chat = service.subscribe("306e6075fc20157e4c31", "Binh Nguyen");
  }

  @Test
  public void testSubscribeChat() {
    Assertions.assertNotNull(this.chat);
  }

  @Test
  public void testSendMessage() {
    service.sendMessage("This is a test message");
  }
}
