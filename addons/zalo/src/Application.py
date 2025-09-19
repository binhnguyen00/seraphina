import os;

from flask import Flask;
from dotenv import load_dotenv;

from .Extensions import init_bot;
from .routes import Webhook, Health;
from .events.Handlers import register_handlers;

load_dotenv()

def create_app():
  app = Flask(__name__)
  bot, dispatcher = init_bot(app)
  register_handlers(dispatcher) # type: ignore
  app.register_blueprint(Webhook.bp)
  app.register_blueprint(Health.bp)
  return app

app: Flask = create_app()

if (__name__ == "__main__"):
  port = int(os.getenv("MICROSERVICE_ZALO_PORT", "8081"))
  app.run(host="0.0.0.0", port=port)