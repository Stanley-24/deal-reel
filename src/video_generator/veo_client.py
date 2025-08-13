import os
import logging
import requests
from config import VEO_API_URL, VEO_API_KEY, VEO_MODEL

logger = logging.getLogger(__name__)

class VeoConfigurationError(Exception):
    pass


def generate_video_with_veo(prompt: str, output_path: str, *, duration_seconds: int = 40, resolution: str = "1080x1920") -> str:
    """
    Call a Google Veo 3-compatible endpoint to generate a video from a text prompt.

    Expects environment variables:
      - VEO_API_URL: Endpoint to call (e.g., a proxy or official API endpoint)
      - VEO_API_KEY: Bearer token/API key for authentication
      - VEO_MODEL: Model name, default "veo-3"

    The endpoint is expected to accept JSON payload with fields: prompt, model, duration, resolution.
    Response is streamed binary (mp4) or provides a downloadable URL.
    This function supports both cases: direct stream or JSON with a url field.
    """
    if not VEO_API_URL or not VEO_API_KEY:
        raise VeoConfigurationError("VEO_API_URL and VEO_API_KEY must be set in the environment.")

    headers = {
        "Authorization": f"Bearer {VEO_API_KEY}",
        "Content-Type": "application/json",
        "Accept": "application/octet-stream, application/json"
    }

    payload = {
        "prompt": prompt,
        "model": VEO_MODEL,
        "duration": duration_seconds,
        "resolution": resolution,
    }

    logger.info("[VEO] Requesting video: model=%s duration=%ss resolution=%s", VEO_MODEL, duration_seconds, resolution)

    try:
        # Try streaming binary first
        resp = requests.post(VEO_API_URL, json=payload, headers=headers, stream=True, timeout=180)
        content_type = resp.headers.get("Content-Type", "")
        # Raise for status after getting headers for logging
        resp.raise_for_status()
    except requests.RequestException as e:
        status = getattr(e.response, "status_code", None) if hasattr(e, "response") and e.response is not None else None
        body_snippet = None
        try:
            if hasattr(e, "response") and e.response is not None:
                body_snippet = e.response.text[:500]
        except Exception:
            pass
        logger.error("[VEO] Request failed status=%s error=%s body_snippet=%s", status, str(e), body_snippet)
        raise

    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)

    # If JSON, assume it returns a URL to download
    if "application/json" in content_type:
        try:
            data = resp.json()
        except Exception:
            logger.error("[VEO] JSON response parse failed")
            raise
        video_url = data.get("video_url") or data.get("url")
        if not video_url:
            logger.error("[VEO] JSON response missing video_url/url: %s", data)
            raise RuntimeError("VEO API returned JSON without a video URL.")
        try:
            r2 = requests.get(video_url, stream=True, timeout=180)
            r2.raise_for_status()
            with open(output_path, "wb") as f:
                for chunk in r2.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)
        except requests.RequestException as e:
            logger.error("[VEO] Download failed from URL=%s error=%s", video_url, str(e))
            raise
        logger.info("[VEO] Video saved to %s via download URL", output_path)
        return output_path

    # Otherwise, assume binary stream (mp4)
    try:
        with open(output_path, "wb") as f:
            for chunk in resp.iter_content(chunk_size=8192):
                if chunk:
                    f.write(chunk)
    except Exception as e:
        logger.error("[VEO] Writing streamed video failed: %s", str(e))
        raise

    logger.info("[VEO] Video saved to %s via direct stream", output_path)
    return output_path
