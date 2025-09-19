from zalo_bot.ext import Dispatcher, CommandHandler, MessageHandler, filters;

from .EventsCallBack import *;

def register_handlers(dispatcher: Dispatcher):
  dispatcher.add_handler(CommandHandler("health", health_check))

  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "đăng ký")), subscribe)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "hủy đăng ký")), unsubscribe)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "lịch tuần")), get_schedule)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "theo dõi ngoại hạng anh")), follow_premier_league)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "hủy theo dõi ngoại hạng anh")), unfollow_premier_league)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "theo dõi laliga")), follow_laliga)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "hủy theo dõi laliga")), unfollow_laliga)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "theo dõi c1")), follow_champion_league)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "hủy theo dõi c1")), unfollow_champion_league)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "trạng thái")), status)) # type: ignore
  dispatcher.add_handler(MessageHandler(filters.BaseFilter(
    lambda u: bool(u.message and u.message.text and u.message.text.lower().strip() == "hướng dẫn")), help)) # type: ignore

  dispatcher.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, unknown_command)) # type: ignore