import logging;
import requests;

from zalo_bot import Update;
from typing import Optional;

logger = logging.getLogger(__name__)

async def subscribe(update: Update, context):
  logger.info(f"User {update.effective_user.id} subscribe") # type: ignore
  if (not update.effective_user):
    return

  await update.message.reply_text(f"Xin chào {update.effective_user.display_name}! Cảm ơn bạn đã đăng ký!") # type: ignore

  user_id: Optional[str] = update.effective_user.id
  chat_name: Optional[str] = update.effective_user.display_name

  response: requests.Response = requests.get(url="http://app:8080/api/v1/zalo/chat/subscribe/get", params={"user_id": user_id})
  response_data: dict = response.json()
  exist: bool = response_data.get("success", False)
  if (exist): 
    await update.message.reply_text(f"Tài khoản đã đăng ký trước đó!") # type: ignore
    return

  response = requests.post(url="http://app:8080/api/v1/zalo/chat/subscribe", json={"user_id": user_id, "chat_name": chat_name})
  response_data: dict = response.json()

  print(response_data)

  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Đăng ký thất bại!") # type: ignore
    return

  await update.message.reply_text(f"Đăng ký thành công!") # type: ignore

async def unsubscribe(update: Update, context):
  logger.info(f"User {update.effective_user.id} unsubscribe") # type: ignore
  if (not update.effective_user):
    return

  await update.message.reply_text(f"Tạm biệt {update.effective_user.display_name}! Cảm ơn bạn và hẹn gặp lại!") # type: ignore

  target_id: Optional[str] = update.effective_user.id
  response = requests.post(url="http://app:8080/api/v1/zalo/chat/unsubscribe", params={"user_id": target_id})
  response_data: dict = response.json()

  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Hủy đăng ký thất bại!") # type: ignore
    return

  await update.message.reply_text(f"Hủy đăng ký thành công!") # type: ignore

async def get_schedule(update: Update, context):
  logger.info(f"User {update.effective_user.id} get schedule") # type: ignore
  if (not update.effective_user):
    return

  response = requests.get(url="http://app:8080/api/v1/premier-league/schedule/matches", params={ "user_id": update.effective_user.id })
  response_data: dict = response.json()
  if (not response_data.get("success", False)):
    message: str = response_data.get("message", "")
    await update.message.reply_text(message) # type: ignore
    return

  matches: str = response_data.get("data", "")
  await update.message.reply_text(matches) # type: ignore

async def follow_premier_league(update: Update, context):
  logger.info(f"User {update.effective_user.id} follow premier league") # type: ignore
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/premier-league/follow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "eng.1"
  })
  response_data: dict = response.json()
  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Theo dõi thất bại!") # type: ignore
    return

  await update.message.reply_text(f"Theo dõi thành công!") # type: ignore

async def follow_laliga(update: Update, context):
  logger.info(f"User {update.effective_user.id} follow laliga") # type: ignore
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/laliga/follow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "esp.1"
  })
  response_data: dict = response.json()
  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Theo dõi thất bại!") # type: ignore
    return

  await update.message.reply_text(f"Theo dõi thành công!") # type: ignore

async def unfollow_premier_league(update: Update, context):
  logger.info(f"User {update.effective_user.id} unfollow premier league") # type: ignore
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/premier-league/unfollow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "eng.1"
  })
  response_data: dict = response.json()
  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Hủy theo dõi thất bại!") # type: ignore
    return

  await update.message.reply_text(f"Hủy theo dõi thành công!") # type: ignore

async def unfollow_laliga(update: Update, context):
  logger.info(f"User {update.effective_user.id} unfollow laliga") # type: ignore
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/laliga/unfollow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "esp.1"
  })
  response_data: dict = response.json()
  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Hủy theo dõi thất bại!") # type: ignore
    return

  await update.message.reply_text(f"Hủy theo dõi thành công!") # type: ignore

async def health_check(update: Update, context):
  logger.info(f"User {update.effective_user.id} is checking api health") # type: ignore
  response = requests.get(url="http://app:8080/api/v1/zalo/chat/health")
  response_data: dict = response.json()
  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Server is not ready!") # type: ignore
    return

  await update.message.reply_text(f"API is healthy!") # type: ignore

async def help(update: Update, context):
  if (not update.effective_user):
    return
  
  message: str = f"""👋 Xin chào {update.effective_user.display_name}!

Chúng tôi cung cấp thông tin lịch ⚽️ đá bóng hàng tuần của các đội tại giải Ngoại Hạng Anh và Laliga.

Đăng ký nhận thông báo, gửi tin nhắn:
    đăng ký

Hủy đăng ký nhận thông báo, gửi tin nhắn:
    hủy đăng ký

Theo dõi/ hủy Ngoại Hạng Anh, gửi tin nhắn:
    theo dõi ngoại hạng anh
    hủy theo dõi ngoại hạng anh

Theo dõi/ hủy Laliga, gửi tin nhắn:
    theo dõi laliga
    hủy theo dõi laliga

Xem lịch tuần này, gửi tin nhắn:
    lịch tuần
  """
  await update.message.reply_text(message) # type: ignore