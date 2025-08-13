import builtins
from io import BytesIO
import types
import os

import pytest

from src.video_generator import utils


class DummyResponse:
    def __init__(self, content: bytes, status_code: int = 200):
        self.content = content
        self.status_code = status_code

    def raise_for_status(self):
        if not (200 <= self.status_code < 400):
            raise Exception("HTTP error")


def test_download_image_writes_file(tmp_path, monkeypatch):
    # Arrange
    data = b"image-bytes"

    class DummySession:
        def get(self, url, timeout=None):
            return DummyResponse(data, 200)

    # Patch session creator
    monkeypatch.setattr(utils, "_session", DummySession())

    # Act
    out_path = tmp_path / "img.jpg"
    utils.download_image("http://example.com/img.jpg", str(out_path), timeout=1.0)

    # Assert
    assert out_path.exists()
    assert out_path.read_bytes() == data
