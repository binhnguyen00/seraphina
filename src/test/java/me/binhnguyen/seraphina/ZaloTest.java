package me.binhnguyen.seraphina;

import me.binhnguyen.seraphina.controller.SubscriberController;
import me.binhnguyen.seraphina.entity.Subscriber;
import me.binhnguyen.seraphina.service.SubscriberService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class ZaloTest {

  @Autowired
  private SubscriberController controller;

  @Autowired
  private SubscriberService service;

  private Subscriber chat;

  @BeforeEach
  void init() {
    SubscriberService.SubscribeResult result = service.subscribe("306e6075fc20157e4c31", "Binh Nguyen");
    this.chat = result.subscriber();
  }

  @Test
  public void testSubscribeChat() {
    Assertions.assertNotNull(this.chat);
  }

  @Test
  public void testSendMessage() {
    List<Subscriber> subscribers = service.getAllSubscribers();
    service.sendMessageTo(subscribers, "This is a test message");
  }
}
