# src/video_generator/utils.py
import requests
from requests.adapters import HTTPAdapter
from urllib3.util import Retry

_session = None

def _get_session():
    global _session
    if _session is None:
        retry = Retry(
            total=3,
            read=3,
            connect=3,
            backoff_factor=0.5,
            status_forcelist=[429, 500, 502, 503, 504],
            allowed_methods=["GET", "HEAD"],
        )
        adapter = HTTPAdapter(max_retries=retry)
        s = requests.Session()
        s.mount("http://", adapter)
        s.mount("https://", adapter)
        _session = s
    return _session

def download_image(image_url: str, save_path: str, timeout: float = 10.0) -> None:
    """
    Download image from image_url and save it to save_path, with retries and timeout.
    """
    session = _get_session()
    resp = session.get(image_url, timeout=timeout)
    resp.raise_for_status()
    with open(save_path, "wb") as f:
        f.write(resp.content)
