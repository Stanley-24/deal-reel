# Video Generator Service (Python/FastAPI)

This service generates short product teaser videos and stores them in S3, persisting the S3 URL back to the database so downstream agents can schedule posts.

Status
- Mock mode: Enabled by default when Veo credentials are missing or failing. Generates a small dummy MP4 to exercise the full flow (upload + DB persistence).
- Veo mode: Planned. When VEO_API_URL and VEO_API_KEY are provided, the service will generate a 6-second prompt-to-video using your product details. Until those credentials are set, the service falls back to mock mode automatically.

Environment
- Python 3.10+
- FastAPI + Uvicorn
- PostgreSQL (DATABASE_URL)
- AWS S3 (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION, S3_BUCKET_NAME)

Environment variables (.env)
- DATABASE_URL=postgresql://user:password@host:5432/db
- AWS_ACCESS_KEY_ID=...
- AWS_SECRET_ACCESS_KEY=...
- AWS_REGION=us-east-1
- S3_BUCKET_NAME=your-bucket
- VEO_API_URL=your-veo3-endpoint (leave unset for mock)
- VEO_API_KEY=your-veo-api-key (leave unset for mock)
- VEO_MODEL=veo-3 (optional)

Run
- Start the API (src/video_generator):
  uvicorn --reload main:app

API
- POST /generate-video
  - Body: { "asin": "B07PGL2ZSL" }
  - Behavior: fetch product, build chipmunk-themed 6s prompt (using only title/description/price), generate video (mock or Veo), upload to S3, persist URL to DB.
  - Response: { asin, title, video_url, mock }

- GET /videos?limit=50&offset=0
  - Lists products having a saved video_url.
  - Response: [{ asin, title, video_url }, ...]

- GET /products/{asin}
  - Fetches one product’s saved video_url.
  - Response: { asin, title, video_url }

Notes on S3 access
- Uploader attempts public-read ACL. If bucket disables ACLs (ObjectOwnership=BucketOwnerEnforced), it retries without ACL. The object URL is still returned.
- If you need public access, configure a bucket policy to allow public reads, or implement a presigned URL endpoint. We can add GET /videos/presigned on request.

Prompt generation
- The service uses script_generator.build_chipmunk_veo_prompt(title, description, price) to create a 6-second, vertical, chipmunk-presenter prompt with a short CTA.
- Until description/price exist in your DB, the service uses NULL placeholders; you can add those columns later for richer prompts.

Data persistence
- After upload, the service ensures products.video_url exists and updates the row for the provided ASIN.

Next Phase (Phase 4): post_mint (TypeScript Social Posting Agent)
- Build a Node/TypeScript service named post_mint that:
  1) Periodically (cron) calls GET /videos to fetch ready-to-post items.
  2) For each item, constructs a caption that MUST include a strong CTA and uses the product title and product link from this server.
     - Example caption template: "{title} — Buy now: {product_url} #DealReel"
     - CTA examples: "Buy now", "Shop today", "Grab yours now"
  3) Posts the video to Meta (Facebook/Instagram) and TikTok using their APIs and your stored credentials.
  4) Records posting status/results for observability and retries.
- Considerations:
  - If S3 is private, fetch a presigned URL (future endpoint) just-in-time before posting.
  - Use environment variables for API credentials and never log secrets.
  - Add retry/backoff and idempotency keys to avoid double posts.
  - If product links are not yet exposed by this API, add a field (e.g., product_url) to the products table or an endpoint that returns it so post_mint can compose captions.

Suggested TS project structure
- post_mint/
  - src/
    - index.ts (entry, cron)
    - clients/video-service.ts (calls /videos, /products/{asin})
    - clients/meta.ts (upload/post)
    - clients/tiktok.ts (upload/post)
    - services/caption.ts (builds CTA captions from title + product URL)
    - utils/logger.ts
  - .env (VIDEO_SERVICE_URL, META/TIKTOK creds)
  - package.json, tsconfig.json
- Use node-cron or a workflow runner (GitHub Actions/Cloud scheduler) if you prefer.

Planned Improvements
- Enable Veo generation by setting VEO_API_URL and VEO_API_KEY.
- Optional endpoint to mint presigned URLs for private buckets.
- Migrations to add description and price columns to products if needed for richer prompts.

License
- MIT (if applicable).

