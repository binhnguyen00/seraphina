from flask import Blueprint;

from ..Dto import Response;

bp = Blueprint("Health", __name__)

@bp.route("/health", methods=["POST"])
def health():
  return Response(status=200, success=True, message="OK").to_dict()