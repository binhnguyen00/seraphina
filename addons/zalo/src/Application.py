import os;
import json;
import requests;

from zalo_bot import Bot;
from pydantic import BaseModel;
from fastapi import FastAPI, status;
from typing import Optional;
from dotenv import load_dotenv;

load_dotenv()

BOT_TOKEN: Optional[str] = os.getenv("BOT_TOKEN")
if (not BOT_TOKEN):
  raise ValueError("BOT_TOKEN is not set")

bot = Bot(token=BOT_TOKEN)
app = FastAPI(title="Zalo Bot", version="1.0.0", description="Zalo Bot Microservice which send message to registered users")

class Request(BaseModel):
  user_id: str
  message: str

class Response(BaseModel):
  status: int
  success: bool
  message: str
  data: Optional[dict] = None

@app.post('/send-message')
async def send_message(request: Request):
  try:
    await bot.send_message(chat_id=request.user_id, text=request.message)
    return Response(
      status=status.HTTP_200_OK,
      success=True,
      message="Message sent successfully"
    )
  except Exception as e:
    return Response(
      status=status.HTTP_500_INTERNAL_SERVER_ERROR,
      success=False,
      message=str(e)
    )

@app.get('/me')
async def get_me():
  entrypoint: str = f"https://bot-api.zapps.me/bot{BOT_TOKEN}/getMe"
  response: requests.Response = requests.get(url=entrypoint)
  response_data: dict = response.json()
  result: dict = response_data.get("result", {})
  
  if (not result.get("ok", False)):
    return Response(
      status=status.HTTP_500_INTERNAL_SERVER_ERROR,
      success=False,
      message="Get me failed"
    )

  return Response(
    status=status.HTTP_200_OK,
    success=True, 
    message="Get me successfully",
    data=result
  )

@app.get('/health')
def health_check():
  return Response(
    status=status.HTTP_200_OK,
    success=True,
    message="Healthy"
  )

if (__name__ == '__main__'):
  PORT: Optional[str] = os.getenv("PORT")
  if (not PORT):
    raise ValueError("PORT is not set")

  import uvicorn;
  uvicorn.run(app, host='0.0.0.0', port=int(PORT))