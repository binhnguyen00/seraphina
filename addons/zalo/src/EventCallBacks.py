import requests;
import logging;

from zalo_bot import Update;
from typing import Optional;

logger = logging.getLogger(__name__)

async def subscribe(update: Update, context):
  logger.info(f"User {update.effective_user.id} subscribe") # type: ignore
  if (not update.effective_user):
    return

  await update.message.reply_text(f"Xin chào {update.effective_user.display_name}! Cảm ơn bạn đã đăng ký!") # type: ignore

  chat_id: Optional[str] = update.effective_user.id
  chat_name: Optional[str] = update.effective_user.display_name

  response: requests.Response = requests.get(url="http://localhost:8080/zalo/chat/subscribe/get", params={"chat_id": chat_id})
  response_data: dict = response.json()
  exist: bool = response_data.get("success", False)
  if (exist): 
    await update.message.reply_text(f"Tài khoản đã đăng ký trước đó!") # type: ignore
    return

  response = requests.post(url="http://localhost:8080/zalo/chat/subscribe", json={"chat_id": chat_id, "chat_name": chat_name})
  response_data: dict = response.json()

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
  response = requests.post(url="http://localhost:8080/zalo/chat/unsubscribe", params={"chat_id": target_id})
  response_data: dict = response.json()

  if (not response_data.get("success", False)):
    await update.message.reply_text(f"Hủy đăng ký thất bại!") # type: ignore
    return

  await update.message.reply_text(f"Hủy đăng ký thành công!") # type: ignore

async def get_schedule(update: Update, context):
  logger.info(f"User {update.effective_user.id} get schedule") # type: ignore
  if (not update.effective_user):
    return

  response = requests.get(url="http://localhost:8080//premier-league/schedule/matches", params={ "chat_id": update.effective_user.id })
  response_data: dict = response.json()
  if (not response_data.get("success", False)):
    message: str = response_data.get("message", "")
    await update.message.reply_text(message) # type: ignore
    return

  matches: str = response_data.get("data", "")
  await update.message.reply_text(matches) # type: ignore

async def help(update: Update, context):
  if (not update.effective_user):
    return
  
  message: str = f"""👋 Xin chào {update.effective_user.display_name}!

Chúng tôi cung cấp thông tin lịch đá bóng hàng tuần của các đội tại giải Ngoại Hạng Anh. Lịch đá sẽ được gửi hàng tuần.

Đăng ký nhận thông báo bằng cú pháp:
    /dangky

Hủy đăng ký nhận thông báo bằng cú pháp:
    /huydangky

Xem lịch tuần này bằng cú pháp:
    /lichtuan
  """
  await update.message.reply_text(message) # type: ignore