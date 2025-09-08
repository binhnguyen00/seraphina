import requests;

from requests import Response;
from typing import Optional;
from datetime import datetime, timedelta;

class ESPNPremierLeagueCrawler():
  def __init__(self):
    self.base_url = "https://site.api.espn.com/apis/site/v2/sports/soccer"
    self.league_id = "eng.1"  # Premier League

  def get_schedule(self, date: Optional[str] = None):
    print(date)
    """
    Get Premier League schedule
    date format: YYYYMMDD (optional)
    """
    url: str = f"{self.base_url}/{self.league_id}/scoreboard"
    params: dict = {}
    if (date):
      params.update({"dates": date})

    response: Response = requests.get(url=url, params=params)
    response_data: dict = response.json()

    print(response_data)

    events: list[dict] = response_data.get("events", [])

    for event in events:
      print("==================================")
      label: str = event.get("name", ""); print(label)
      start_time: str = event.get("date", ""); print(start_time)
      home_stadium: str = dict(event.get("venue", {})).get("displayName", ""); print(home_stadium)
      competitions: list[dict] = event.get("competitions", [])
      competitors: list[dict] = event.get("competitors", [])
      # for competitor in competitors:
      #   home_away: str = competitor.get("homeAway", ""); print(home_away)
      #   team: dict = competitor.get("team", {})
      #   team_name: str = team.get("name", ""); print(team_name)

if __name__ == "__main__":
  crawler = ESPNPremierLeagueCrawler()
  today_matches = crawler.get_schedule("20250913")