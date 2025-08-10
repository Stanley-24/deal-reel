# 📐 System Design

## 1. Overview
Deal Reel is a fully automated affiliate marketing system that:

- Fetches trending products from Amazon via the Product Advertising API using a Java microservice.
- Stores product details in PostgreSQL.
- Generates short storytelling videos (40s) with AI models via Python (FastAPI).
- Posts 3 videos daily to Meta and TikTok using a TypeScript automation agent.
- Tracks affiliate link clicks and conversions.

We operate with:

- One founder-engineer  
- One laptop  
- Zero budget  
- Only free and open-source tools  

---

## 2. Architecture Diagram (Mermaid)

```mermaid
flowchart TD
    %% === Styles ===
    classDef backend fill=#f6d365,stroke=#f39c12,stroke-width=2px,color=#000,border-radius=8px;
    classDef frontend fill=#d4fc79,stroke=#27ae60,stroke-width=2px,color=#000,border-radius=8px;
    classDef service fill=#cfd9df,stroke=#7f8c8d,stroke-width=2px,color=#000,border-radius=8px;
    classDef external fill=#a1c4fd,stroke=#2980b9,stroke-width=2px,color=#000,border-radius=8px;

    %% === Nodes ===
    JavaFetcher["☕ Java Product Fetcher\n(Amazon PA-API)"]:::backend
    DB[(🗄️ PostgreSQL Database)]:::backend
    VideoGen["🎬 AI Video Generator\n(Python FastAPI)"]:::backend
    VideoStorage["💾 Video Storage\n(Local / Cloud)"]:::service
    Poster["📢 Social Posting Agent\n(TypeScript + React)"]:::frontend
    SocialAPIs["🌍 Meta & TikTok APIs"]:::external
    Users["👥 Viewers"]:::external
    Amazon["🛒 Amazon Product Page\n(Affiliate Link)"]:::external
    TrackingDB[(📊 Click Tracking DB)]:::backend

    %% === Flow ===
    JavaFetcher -->|Fetch products| DB
    DB -->|Product details| VideoGen
    VideoGen -->|Generated videos| VideoStorage
    VideoStorage --> Poster
    Poster --> SocialAPIs
    SocialAPIs -->|Affiliate links| Users
    Users -->|Click link| Amazon
    Users -->|Tracking pixel| TrackingDB

    
## 3. Component Breakdown

**Java Product Fetcher**

Uses Amazon PA-API to fetch:

    -Product title

    -Description

    -Images

    -Price

Affiliate link with tracking ID

Runs as a scheduled microservice.

Stores results in PostgreSQL.

**Python AI Video Generator (FastAPI)**

Pulls product details from the database.

Generates 40-second storytelling videos using free/open-source AI video & TTS tools.

Stores final videos in Video Storage (local or cloud bucket).

TypeScript Social Posting Agent

Fetches the day’s videos.

Posts 3 videos daily to:

Facebook Reels (Meta API)

TikTok

Captions include affiliate links.

Runs via CRON or event trigger.

Click Tracking Service
Every affiliate link redirects through a tracking endpoint.

Stores click events in Tracking Database.

Fetches conversion data from Amazon Affiliate Reports.

## 4. Data Flow

    - Fetch Products → Java microservice calls Amazon API → stores product info in PostgreSQL.

    - Generate Video → Python AI service creates storytelling videos from stored product details.

    - Post to Social → TypeScript service schedules and posts videos to Meta & TikTok.

    - Track Clicks → Users click affiliate links → redirect to Amazon → record click event.

    - Get Conversions → Amazon reports sales attributed to our affiliate links.

## 5. Tech Stack

Layer	Technology

Product Fetcher	Java + Amazon PA-API
API Layer / Video Generation	Python (FastAPI)
Storage	PostgreSQL
Social Posting	TypeScript + React
Tracking	PostgreSQL + Amazon Affiliate Reports
Hosting (free tier)	Railway / Render / Fly.io
Version Control	Git + GitHub

## 6. Design Patterns
Repository — abstract database operations.

Factory — generate AI video tasks dynamically.

Observer — trigger posting after video generation completes.

Command — encapsulate posting jobs for retry.

7. Scalability
Stateless microservices allow independent scaling.

Can migrate video storage to cloud (S3-compatible) as traffic grows.

Posting agent can expand to more social platforms.

8. Constraints & Assumptions
Zero budget: rely on free-tier services and open-source AI tools.

One-person development team.

All automation must require minimal manual intervention.

No internal payment handling — all sales happen via Amazon.