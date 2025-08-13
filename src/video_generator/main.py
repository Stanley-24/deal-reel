from fastapi import FastAPI, HTTPException, Query
from pydantic import BaseModel
from sqlalchemy import create_engine, text
import os
import uuid
import logging
from typing import List, Optional

from veo_client import generate_video_with_veo
from config import DATABASE_URL, VEO_API_KEY, VEO_API_URL
from storage import S3Storage
from script_generator import build_chipmunk_veo_prompt

engine = create_engine(DATABASE_URL)
storage = S3Storage()

app = FastAPI(title="Product Video Generator")
logger = logging.getLogger(__name__)

class ProductRequest(BaseModel):
    asin: str

class VideoItem(BaseModel):
    asin: str
    title: Optional[str] = None
    video_url: str

def fetch_product_by_asin(asin: str):
    # Some deployments may not yet have description/price columns. Use NULL placeholders to avoid errors.
    query = text(
        """
        SELECT
            asin,
            title,
            image_url,
            NULL::text AS description,
            NULL::text AS price
        FROM products
        WHERE asin = :asin
        """
    )
    with engine.connect() as conn:
        row = conn.execute(query, {"asin": asin}).fetchone()
    return dict(row._mapping) if row is not None else None

@app.get("/videos", response_model=List[VideoItem])
def list_videos(limit: int = Query(50, ge=1, le=500), offset: int = Query(0, ge=0)):
    """List products that have a saved video_url for scheduler consumption."""
    query = text(
        """
        SELECT asin, title, video_url
        FROM products
        WHERE video_url IS NOT NULL AND video_url != ''
        ORDER BY asin
        LIMIT :limit OFFSET :offset
        """
    )
    with engine.connect() as conn:
        rows = conn.execute(query, {"limit": limit, "offset": offset}).fetchall()
    return [VideoItem(asin=r[0], title=r[1], video_url=r[2]) for r in rows]

@app.get("/products/{asin}", response_model=VideoItem)
def get_product_video(asin: str):
    """Fetch a single product's saved video URL."""
    query = text("SELECT asin, title, video_url FROM products WHERE asin = :asin")
    with engine.connect() as conn:
        row = conn.execute(query, {"asin": asin}).fetchone()
    if not row or not row[2]:
        raise HTTPException(status_code=404, detail="Video not found for this ASIN")
    return VideoItem(asin=row[0], title=row[1], video_url=row[2])

@app.post("/generate-video")
def generate_product_video(data: ProductRequest):
    # 1. Get product from DB
    product = fetch_product_by_asin(data.asin)
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")

    # 2. Build a 6s Veo prompt using only title, description, price
    title = product.get('title') or ''
    description = product.get('description') or ''
    price = product.get('price') or ''
    prompt = build_chipmunk_veo_prompt(title, description, price)

    # 3. Choose local output path
    output_dir = os.path.join("/tmp", "videos")
    os.makedirs(output_dir, exist_ok=True)
    local_output_path = os.path.join(output_dir, f"{product['asin']}_{uuid.uuid4().hex}.mp4")

    # 4. Generate video
    use_mock = not (VEO_API_URL and VEO_API_KEY)
    video_path = None
    if use_mock:
        # Create a small mock file to simulate a generated video
        try:
            with open(local_output_path, "wb") as f:
                f.write(b"MOCK_MP4\n" + os.urandom(1024))
            video_path = local_output_path
            logger.info("[Mock] Generated mock video at %s (VEO creds missing)", video_path)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Failed to create mock video: {str(e)}")
    else:
        # Generate video with Veo backend (prompt-to-video, 6 seconds)
        try:
            video_path = generate_video_with_veo(prompt, local_output_path, duration_seconds=6, resolution="1080x1920")
        except Exception as e:
            # Graceful fallback to mock mode if VEO fails
            logger.warning("[VEO] Generation failed, falling back to mock. Error: %s", str(e))
            try:
                with open(local_output_path, "wb") as f:
                    f.write(b"MOCK_MP4\n" + os.urandom(1024))
                video_path = local_output_path
                use_mock = True
                logger.info("[Mock] Fallback mock video generated at %s", video_path)
            except Exception as ie:
                raise HTTPException(status_code=500, detail=f"VEO failed and mock fallback failed: {str(ie)}")

    # 5. Upload to S3 and return URL
    s3_key = f"videos/{product['asin']}/{os.path.basename(video_path)}"
    try:
        video_url = storage.upload_file(video_path, s3_key)
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Upload to S3 failed: {str(e)}")

    # 6. Persist S3 URL in database so other agents can use it later
    try:
        with engine.begin() as conn:
            conn.execute(text("ALTER TABLE products ADD COLUMN IF NOT EXISTS video_url TEXT"))
            conn.execute(
                text("UPDATE products SET video_url = :url WHERE asin = :asin"),
                {"url": video_url, "asin": product["asin"]},
            )
    except Exception as e:
        # Not fatal for the API response, but report failure clearly
        raise HTTPException(status_code=500, detail=f"Saved to S3 but failed to persist URL in DB: {str(e)}")

    return {
        "asin": product['asin'],
        "title": product['title'],
        "video_url": video_url,
        "mock": use_mock
    }
