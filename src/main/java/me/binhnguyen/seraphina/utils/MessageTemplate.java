package me.binhnguyen.seraphina.utils;

import lombok.NoArgsConstructor;
import me.binhnguyen.seraphina.entity.Matchup;

import java.util.List;

@NoArgsConstructor
public class MessageTemplate {

  public static String ZALO(List<Matchup> matches) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < matches.size(); i++) {
      Matchup matchup = matches.get(i);
      String message = String.format("""
      %d ⚽️
      %s vs %s
      Giờ đá: %s
      Sân: %s
      """,
        i + 1,
        matchup.getHomeTeam().getName(),
        matchup.getAwayTeam().getName(),
        matchup.getFormatMatchDay(),
        matchup.getHomeStadium()
      );
      builder.append(message);
    }

    return builder.toString().trim();
  }

  // TODO: Implement
  public static String TELEGRAM(List<Matchup> matches) {
    return ZALO(matches);
  }
}
