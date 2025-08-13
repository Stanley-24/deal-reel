from pydantic import BaseModel

class VideoRequest(BaseModel):
    product_asin: str
    narration_text: str = None

class VideoResponse(BaseModel):
    video_url: str
    message: str
