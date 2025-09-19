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
  data = request.get_json()
  req = Request(**data)
  bot: Bot = current_app.extensions["bot"]

  try:
    await bot.send_message(chat_id=req.user_id, text=req.message) # type: ignore
    return Response(
      status=200, 
      success=True, 
      message="Message sent successfully"
    ).to_dict()
  except Exception as e:
    return Response(
      status=500, 
      success=False, 
      message=str(e)
    ).to_dict()
