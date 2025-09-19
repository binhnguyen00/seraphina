import logging;
import requests;

from zalo_bot import Update;
from typing import Optional;

logger = logging.getLogger(__name__)

async def subscribe(update: Update, context):
  if (not update.effective_user):
    return

  await update.message.reply_text(f"Xin chào {update.effective_user.display_name}! Cảm ơn bạn đã đăng ký!") # type: ignore

  user_id: Optional[str] = update.effective_user.id
  chat_name: Optional[str] = update.effective_user.display_name

  response = requests.post(url="http://app:8080/api/v1/zalo/chat/subscribe", json={"user_id": user_id, "chat_name": chat_name})
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore


async def unsubscribe(update: Update, context):
  if (not update.effective_user):
    return

  await update.message.reply_text(f"Tạm biệt {update.effective_user.display_name}! Cảm ơn bạn và hẹn gặp lại!") # type: ignore

  target_id: Optional[str] = update.effective_user.id
  response = requests.post(url="http://app:8080/api/v1/zalo/chat/unsubscribe", params={"user_id": target_id})
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore


async def get_schedule(update: Update, context):
  if (not update.effective_user):
    return

  response = requests.get(url="http://app:8080/api/v1/league/schedule/matches", params={ "user_id": update.effective_user.id })
  response_data: dict = response.json()
  success: bool = response_data.get("success", False)
  server_message: str = response_data.get("message", "")
  leagues: list[dict] = response_data.get("data", [])

  if (not success):
    await update.message.reply_text(server_message) # type: ignore
    return

  for league in leagues:
    name: str = league.get("name", "Không xác định")
    await update.message.reply_text(name) # type: ignore

    matches: list[dict] = league.get("matches", [])
    for match in matches:
      message = f"""
{matches.index(match) + 1}. {match.get("home", "TBD")} vs {match.get("away", "TBD")} ⚽️
Giờ đá: {match.get("matchDay", "TBD")}
Sân: {match.get("stadium", "TBD")}
      """.strip()
      try:
        await update.message.reply_text(message) # type: ignore
      except Exception as e:
        print(str(e))
        continue

async def follow_premier_league(update: Update, context):
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/league/premier-league/follow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "eng.1"
  })
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore

async def follow_laliga(update: Update, context):
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/league/laliga/follow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "esp.1"
  })
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore


async def unfollow_premier_league(update: Update, context):
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/league/premier-league/unfollow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "eng.1"
  })
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore


async def unfollow_laliga(update: Update, context):
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/league/laliga/unfollow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "esp.1"
  })
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore


async def follow_champion_league(update: Update, context):
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/league/champion-league/follow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "uefa.champions"
  })
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore


async def unfollow_champion_league(update: Update, context):
  if (not update.effective_user):
    return

  response = requests.post(url="http://app:8080/api/v1/league/champion-league/unfollow", json={ 
    "user_id": update.effective_user.id,
    "league_code": "uefa.champions"
  })
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore


async def status(update: Update, context):
  if (not update.effective_user):
    return

  response = requests.get(url="http://app:8080/api/v1/zalo/chat/status", params={ "user_id": update.effective_user.id })
  response_data: dict = response.json()
  success: bool = response_data.get("success", False)
  server_message: str = response_data.get("message", "")
  data: dict = response_data.get("data", "")

  if (not success):
    await update.message.reply_text(server_message) # type: ignore
    return

  following: list[str] = data.get("following", [])
  if (not following):
    await update.message.reply_text("Bạn chưa theo dõi giải đấu nào. Hãy nhắn \"theo dõi ngoại hạng anh\" hoặc \"theo dõi laliga\"") # type: ignore
    return

  await update.message.reply_text(f"Bạn đang theo dõi: {', '.join(following)}") # type: ignore


async def health_check(update: Update, context):
  logger.info(f"User {update.effective_user.id} is checking api health") # type: ignore
  response = requests.get(url="http://app:8080/api/v1/zalo/chat/health")
  response_data: dict = response.json()
  message: str = response_data.get("message", "")
  await update.message.reply_text(message) # type: ignore


async def help(update: Update, context):
  if (not update.effective_user):
    return

  message: str = f"👋 Xin chào {update.effective_user.display_name}!"
  await update.message.reply_text(message) # type: ignore

  message = "Chúng tôi cung cấp thông tin lịch ⚽️ đá bóng hàng tuần của các đội tại giải Ngoại Hạng Anh, Laliga và Champions League."
  await update.message.reply_text(message) # type: ignore

  message = """
Đăng ký nhận thông báo, gửi tin nhắn:
  - đăng ký

Hủy đăng ký nhận thông báo, gửi tin nhắn:
  - hủy đăng ký
  """.strip()
  await update.message.reply_text(message) # type: ignore

  message = """
Theo dõi/ hủy Ngoại Hạng Anh 🏆, gửi tin nhắn:
  - theo dõi ngoại hạng anh
  - hủy theo dõi ngoại hạng anh
""".strip()
  await update.message.reply_text(message) # type: ignore

  message = """
Theo dõi/ hủy Laliga 🏆, gửi tin nhắn:
  - theo dõi laliga
  - hủy theo dõi laliga
""".strip()
  await update.message.reply_text(message) # type: ignore

  message = """
Theo dõi/ hủy Champions League 👑, gửi tin nhắn:
  - theo dõi c1
  - hủy theo dõi c1
""".strip()
  await update.message.reply_text(message) # type: ignore

  message = """
Xem lịch tuần này, gửi tin nhắn:
  - lịch tuần
  """.strip()
  await update.message.reply_text(message) # type: ignore

  message = """
Xem trạng thái của bạn, gửi tin nhắn:
  - trạng thái
""".strip()
  await update.message.reply_text(message) # type: ignore


async def unknown_command(update: Update, context):
  if (not update.effective_user):
    return

  message: str = f"""🤷‍♂️ Lệnh của bạn chưa đúng! Hãy nhắn "hướng dẫn" để xem các lệnh có thể sử dụng"""
  await update.message.reply_text(message) # type: ignore