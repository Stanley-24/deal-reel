# System Design

## Architecture Diagram (Mermaid)
```mermaid
flowchart LR
    API[Amazon PA-API] --> DB[(PostgreSQL)]
    DB --> VideoGen[Video Generation Service]
    VideoGen --> Social[Meta & TikTok Posting Service]
    Social --> Tracker[Click & Conversion Tracker]


## Design Patterns

**Repository** — for database access abstraction.

**Factory** — for video generation object creation.

**Observer** — for event-driven posting.

**Command** — for automating posting and tracking actions.