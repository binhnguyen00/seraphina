import os;
import requests;
import secrets;

from flask import Flask, request;
from zalo_bot import Bot, Update;
from zalo_bot.ext import Dispatcher, CommandHandler, MessageHandler, filters;
from pydantic import BaseModel;
from typing import Optional;
from dotenv import load_dotenv;

load_dotenv()

BOT_TOKEN: Optional[str] = os.getenv("BOT_TOKEN")
if (not BOT_TOKEN):
  raise ValueError("BOT_TOKEN is not set")

bot = Bot(token=BOT_TOKEN)
app = Flask(__name__)

class Request(BaseModel):
  chat_id: str
  message: str

class Response(BaseModel):
  status: int
  success: bool
  message: str
  data: Optional[dict] = None

  def to_dict(self):
    return {
      "status"  : self.status,
      "success" : self.success,
      "message" : self.message,
      "data"    : self.data
    }

async def start(update: Update, context):
  """ idk what event is this """
  if (not update.effective_user):
    return

  await update.message.reply_text(f"Xin chào {update.effective_user.display_name}!\n Đã đăng ký nhận thông báo thành công!") # type: ignore

  chat_id: Optional[str] = update.effective_user.id
  chat_name: Optional[str] = update.effective_user.display_name
  response: requests.Response = requests.post(url="http://localhost:8080/zalo/chat/subscribe", json={"chat_id": chat_id, "chat_name": chat_name})
  response_data: dict = response.json()

  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Đăng ký thất bại!") # type: ignore
    return

async def echo(update: Update, context):
  """ user sent message event """
  if (not update.message):
    return

  chat_id: Optional[str] = update.effective_user.id # type: ignore
  chat_name: Optional[str] = update.effective_user.display_name # type: ignore
  response: requests.Response = requests.post(url="http://localhost:8080/zalo/chat/subscribe", json={"chat_id": chat_id, "chat_name": chat_name})
  response_data: dict = response.json()

  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Đăng ký thất bại!") # type: ignore
    return

with app.app_context():
  webhook_url: Optional[str] = os.getenv("WEBHOOK_URL")
  if (not webhook_url):
    raise ValueError("WEBHOOK_URL is not set")

  secret_token: Optional[str] = secrets.token_hex(32)
  bot.set_webhook(url=webhook_url, secret_token=secret_token)

  dispatcher = Dispatcher(bot, None, workers=0)
  dispatcher.add_handler(CommandHandler("start", start))
  dispatcher.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, echo)) # type: ignore

@app.route('/webhook', methods=['POST'])
def webhook():
  update: Optional[Update] = Update.de_json(request.get_json(force=True), bot)
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

@app.route('/me', methods=['GET'])
async def get_me():
  entrypoint: str = f"https://bot-api.zapps.me/bot{BOT_TOKEN}/getMe"
  response: requests.Response = requests.get(url=entrypoint)
  response_data: dict = response.json()
  result: dict = response_data.get("result", {})
  
  if (not result.get("ok", False)):
    return Response(
      status=500,
      success=False,
      message="Get me failed"
    ).to_dict()

  return Response(
    status=200,
    success=True, 
    message="Get me successfully",
    data=result
  ).to_dict()

@app.route('/health', methods=['GET'])
def health_check():
  return Response(
    status=200,
    success=True,
    message="Healthy"
  ).to_dict()

if (__name__ == '__main__'):
  PORT: Optional[str] = os.getenv("PORT")
  if (not PORT):
    raise ValueError("PORT is not set")

  app.run(port=int(PORT))