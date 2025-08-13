# src/video_generator/db_client.py
from typing import Optional
import asyncpg
from config import DATABASE_URL

class DatabaseClient:
    def __init__(self):
        self.pool: Optional[asyncpg.pool.Pool] = None

    async def connect(self):
        self.pool = await asyncpg.create_pool(DATABASE_URL)

    async def disconnect(self):
        if self.pool:
            await self.pool.close()

    async def fetch_product_by_asin(self, asin: str):
        # Only select columns guaranteed to exist in the current schema
        query = "SELECT asin, title, image_url FROM products WHERE asin = $1"
        async with self.pool.acquire() as connection:
            return await connection.fetchrow(query, asin)

    async def update_product_video_url(self, asin: str, video_url: str):
        # Ensure the column exists, then update
        alter_sql = "ALTER TABLE products ADD COLUMN IF NOT EXISTS video_url TEXT"
        update_sql = "UPDATE products SET video_url = $1 WHERE asin = $2"
        async with self.pool.acquire() as connection:
            await connection.execute(alter_sql)
            await connection.execute(update_sql, video_url, asin)

db_client = DatabaseClient()
