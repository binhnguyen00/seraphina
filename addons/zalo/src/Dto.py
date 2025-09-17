from pydantic import BaseModel;
from typing import Optional;

class Request(BaseModel):
  user_id: str
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
