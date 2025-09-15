import os;
import secrets;

from typing import Optional;
from dotenv import load_dotenv;
from flask import Flask, request;
from zalo_bot import Bot, Update;
from zalo_bot.ext import Dispatcher, CommandHandler, MessageHandler, filters;

load_dotenv()

from Dto import Request, Response;
from EventCallBacks import (
  subscribe, unsubscribe, 
  get_schedule, 
  follow_premier_league, follow_laliga, 
  help, health_check
)

app = Flask(__name__)
with app.app_context():
  BOT_TOKEN: Optional[str] = os.getenv("MICROSERVICE_ZALO_BOT_TOKEN")
  if (not BOT_TOKEN):
    raise ValueError("MICROSERVICE_ZALO_BOT_TOKEN is not set")

  WEBHOOK_SECRET_TOKEN: Optional[str] = secrets.token_hex(32)
  WEBHOOK_URL: Optional[str] = os.getenv("MICROSERVICE_ZALO_WEBHOOK_URL")
  if (not WEBHOOK_URL):
    raise ValueError("MICROSERVICE_ZALO_WEBHOOK_URL is not set")

  bot = Bot(token=BOT_TOKEN)
  bot.set_webhook(url=WEBHOOK_URL, secret_token=WEBHOOK_SECRET_TOKEN)

  dispatcher = Dispatcher(bot, None, workers=0)
  dispatcher.add_handler(CommandHandler("dangky", subscribe))
  dispatcher.add_handler(CommandHandler("huydangky", unsubscribe))
  dispatcher.add_handler(CommandHandler("lichtuan", get_schedule))
  dispatcher.add_handler(CommandHandler("follow-premier-league", follow_premier_league))
  dispatcher.add_handler(CommandHandler("follow-laliga", follow_laliga))
  dispatcher.add_handler(CommandHandler("health", health_check))
  # catch all text messages except commands (start with /)
  dispatcher.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, help)) # type: ignore

@app.route('/webhook', methods=['POST'])
def webhook():
  update: Optional[Update] = Update.de_json(data=request.get_json(force=True), bot=bot)
  if (not update):
    return Response(
      status=400,
      success=False,
      message="Can not update webhook"
    ).to_dict()

  dispatcher.process_update(update)
  return Response(
    status=200,
    success=True,
    message="Update processed successfully"
  ).to_dict()

@app.route('/send-message', methods=['POST'])
async def send_message():
  data = request.get_json()
  req = Request(**data)

  try:
    await bot.send_message(chat_id=req.chat_id, text=req.message)
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

@app.route('/health', methods=['GET'])
def health():
  return Response(
    status=200,
    success=True,
    message="Healthy"
  ).to_dict()

if (__name__ == '__main__'):
  PORT: Optional[str] = os.getenv("MICROSERVICE_ZALO_PORT")
  if (not PORT):
    raise ValueError("MICROSERVICE_ZALO_PORT is not set")

  app.run(host="0.0.0.0", port=int(PORT))