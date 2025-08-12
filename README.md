# Deal on Reels

## 1. Brand Vision
**Deal on Reels** is a fully automated affiliate marketing system that:
- Fetches trending products from Amazon via the official **Product Advertising API**.
- Creates 40-second storytelling videos for each product using free AI tools.
- Posts **3 videos daily** to **Meta** and **TikTok** with affiliate links.
- Tracks clicks and conversions automatically.

We are a **team of one founder-engineer**, starting with:
- **One laptop**
- **Zero budget**
- **Only free/open-source tools**
- **Clear documentation from Day 0**

---

## 2. Core Principles
- **No scraping** — official API partnerships only.
- **Automation-first** — once deployed, no daily manual work.
- **Open knowledge** — every step documented in Markdown.
- **Scalable design** — modular microservices.

---

## 3. Goals
- 🚀 Automate affiliate content creation & posting end-to-end.
- 💸 Launch with zero subscriptions or paid tools.
- 📊 Implement click & conversion tracking.
- 📅 Deliver 3 high-quality posts every day.

---

## 4. Microservice Naming
- **Deal on Reels** → Brand & entire system.
- **Post Mint** → Content-generation microservice.

---

## 5. System Overview
**High-level flow:**
1. **Post Mint** fetches trending products via Amazon PA-API.
2. Products stored in **PostgreSQL**.
3. Video worker generates product storytelling videos.
4. Posting agent publishes videos to Meta & TikTok daily.
5. Click tracker monitors affiliate link performance.

---

## 6. Documentation
All project documents are stored in `/docs`:
- **STYLE_GUIDE.md** — Coding, commit, and documentation standards.
- **SYSTEM_DESIGN.md** — Architecture diagrams & explanation.
- **ROADMAP.md** — Phase-by-phase development plan.
- **PROGRESS.md** — Daily progress log.
- **INFRASTRUCTURE.md** — Dev environment setup instructions.
- **RESEARCH.md** — Free tools, APIs, and technical notes.
- **PHASE2_LIVE_SWITCH.md** — Live Amazon API integration tracking.

---

## 7. Roadmap Snapshot
- **Phase 0** — Documentation & repo setup ✅
- **Phase 1** — Infrastructure setup (local Postgres, Python, Java, TypeScript)
- **Phase 2** — Amazon API integration ✅ (Mock mode complete, Live mode pending API approval)
- **Phase 3** — Video generation pipeline
- **Phase 4** — Posting automation
- **Phase 5** — Click tracking & optimization

---

## 8. License
Open source under MIT License.
