from zalo_bot import Bot, Update;
from zalo_bot.ext import Dispatcher;
from flask import Blueprint, request, current_app;

from ..Dto import Response, Request;

bp = Blueprint("Webhook", __name__)

@bp.route("/webhook", methods=["POST"])
def webhook():
  bot: Bot = current_app.extensions["bot"]
  dispatcher: Dispatcher = current_app.extensions["dispatcher"]

  update = Update.de_json(data=request.get_json(force=True), bot=bot)
  if (not update):
    return Response(
      status=400, 
      success=False, 
      message="Cannot update webhook"
    ).to_dict()

  dispatcher.process_update(update) # type: ignore
  return Response(
    status=200, 
    success=True, 
    message="Update processed successfully"
  ).to_dict()

@bp.route("/send-message", methods=["POST"])
async def send_message():
  bot: Bot = current_app.extensions["bot"]
  data: dict = request.get_json()
  user_id: str = data.get("user_id", "")
  leagues: list[dict] = data.get("leagues", [])

  data: dict = {}
  success_matches: int = 0
  for league in leagues:
    name: str = league.get("name", "Không xác định")
    await bot.send_message(chat_id=user_id, text=name) # type: ignore

    matches: list[dict] = league.get("matches", [])
    for match in matches:
      message = f"""
{matches.index(match) + 1}. {match.get("home", "TBD")} vs {match.get("away", "TBD")} ⚽️
Giờ đá: {match.get("matchDay", "TBD")}
Sân: {match.get("stadium", "TBD")}
      """.strip()
      try:
        await bot.send_message(chat_id=user_id, text=message) # type: ignore
        success_matches += 1
      except Exception as e:
        print(str(e))
        continue

    data.update({name: success_matches})
    success_matches = 0

  return Response(
    status=200,
    success=True,
    message=f"Message sent successfully",
    data=data
  ).to_dict()