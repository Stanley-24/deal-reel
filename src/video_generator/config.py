# Centralized configuration for environment variables
from dotenv import load_dotenv
import os

load_dotenv()

# Database
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://user:password@localhost:5432/mydb")

# AWS / S3
AWS_ACCESS_KEY_ID = os.getenv("AWS_ACCESS_KEY_ID")
AWS_SECRET_ACCESS_KEY = os.getenv("AWS_SECRET_ACCESS_KEY")
AWS_REGION = os.getenv("AWS_REGION", "us-east-1")
S3_BUCKET_NAME = os.getenv("S3_BUCKET_NAME")
USE_PRESIGNED_URLS = os.getenv("USE_PRESIGNED_URLS", "false").lower() in {"1", "true", "yes"}

# Hugging Face / Models (if using open-source fallback)
HF_MODEL_REPO = os.getenv("HF_MODEL_REPO", "stabilityai/stable-video-diffusion-img2vid-xt")

# Google Veo 3 (configurable endpoint)
VEO_API_URL = os.getenv("VEO_API_URL")
VEO_API_KEY = os.getenv("VEO_API_KEY")
VEO_MODEL = os.getenv("VEO_MODEL", "veo-3")

# Video backend selector: "veo" (default) or "svd"
VIDEO_BACKEND = os.getenv("VIDEO_BACKEND", "veo").lower()

# Optional external services (kept for completeness)
SORA_API_URL = os.getenv("SORA_API_URL")
SORA_API_KEY = os.getenv("SORA_API_KEY")
