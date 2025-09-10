package me.binhnguyen.seraphina.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  @Qualifier("premierLeague")
  public WebClient premierLeagueWebClient() {
    return WebClient.builder()
      .baseUrl("https://site.api.espn.com/apis/site/v2/sports/soccer")
      .defaultHeader("Content-Type", "application/json; charset=utf-8")
      .build();
  }
}
