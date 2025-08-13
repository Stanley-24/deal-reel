import asyncio
import pytest

from src.video_generator.db_client import DatabaseClient


class FakeConnection:
    def __init__(self):
        self.executed = []

    async def fetchrow(self, query, asin):
        self.executed.append(("fetchrow", query, asin))
        if asin == "FOUND":
            return {"asin": asin, "title": "T", "image_url": "http://img"}
        return None

    async def execute(self, query, video_url, asin):
        self.executed.append(("execute", query, video_url, asin))
        return "OK"


class FakePool:
    def __init__(self, conn: FakeConnection):
        self._conn = conn

    async def close(self):
        pass

    def acquire(self):
        conn = self._conn

        class _CM:
            async def __aenter__(self_inner):
                return conn

            async def __aexit__(self_inner, exc_type, exc, tb):
                return False

        return _CM()


@pytest.mark.asyncio
async def test_db_client_fetch_and_update(monkeypatch):
    client = DatabaseClient()
    fake_conn = FakeConnection()

    async def fake_create_pool(url):
        return FakePool(fake_conn)

    # Patch asyncpg.create_pool inside module
    import src.video_generator.db_client as dbm
    monkeypatch.setattr(dbm, "asyncpg", types.SimpleNamespace(create_pool=fake_create_pool))

    await client.connect()

    # fetch found
    rec = await client.fetch_product_by_asin("FOUND")
    assert rec["asin"] == "FOUND"

    # fetch missing
    rec2 = await client.fetch_product_by_asin("MISSING")
    assert rec2 is None

    # update
    await client.update_product_video_url("FOUND", "http://video")

    await client.disconnect()
