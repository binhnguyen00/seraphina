import os, secrets, asyncio;

from flask import Flask;
from zalo_bot import Bot;
from dotenv import load_dotenv;
from zalo_bot.ext import Dispatcher;

load_dotenv()

def init_bot(app: Flask) -> tuple[Bot, Dispatcher]:
  bot_token = os.getenv("MICROSERVICE_ZALO_BOT_TOKEN")
  if (not bot_token):
    raise ValueError("MICROSERVICE_ZALO_BOT_TOKEN is not set")

  webhook_url = os.getenv("MICROSERVICE_ZALO_WEBHOOK_URL")
  if (not webhook_url):
    raise ValueError("MICROSERVICE_ZALO_WEBHOOK_URL is not set")

  webhook_secret = secrets.token_hex(32)

  bot = Bot(token=bot_token)
  dispatcher = Dispatcher(bot=bot, update_queue=None, workers=0)

  async def setup_webhook():
    await bot._set_webhook_async(webhook_url, webhook_secret) # type: ignore

  asyncio.get_event_loop().create_task(setup_webhook())

  app.extensions["bot"] = bot
  app.extensions["dispatcher"] = dispatcher

  return bot, dispatcher