import asyncio;

from zalo_bot import Bot;

async def main():
  bot: Bot = Bot("3517148398721224564:bgTtGnPvOOFYavNVGcBnRzWkQImxsceNqaKrZylqygDlLwXGmDRroYkbPFLjgchF")
  async with bot:
    me = await bot.get_me()
    print(f"{me.display_name}, ID: {me.id}")

if (__name__ == "__main__"):
  asyncio.run(main())