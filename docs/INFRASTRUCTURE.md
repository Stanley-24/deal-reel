# Infrastructure Setup

## Install Dependencies
- **Python 3.11+**
- **Java 17+**
- **Node.js 20+**
- **PostgreSQL 15+**

## Setup Git
```bash
git init
git branch -M main
git checkout -b develop



## Free Deployment Options
  -Render
  -Railway
  -Fly.io

## Phase 2: Live Mode Switch Preparation

### Pre-requisites:
- Obtain approved Amazon Product Advertising API credentials
- Store keys securely in environment variables
- Ensure PostgreSQL database is accessible with proper credentials and SSL mode

### Implementation:
1. Change `AMAZON_API_MODE` from "mock" to "live" in `.env`
2. Validate network connectivity and API availability
3. Enable enhanced error handling and logging for live API calls
4. Run integration tests with live product data
5. Monitor API quota usage and implement retry/backoff logic

### Testing & Deployment:
1. Run full regression tests covering fetching, parsing, storage, and retrieval
2. Perform load/performance testing to ensure stability under live conditions
3. Update CI/CD pipelines to automate live mode tests

### Post-Deployment:
1. Continuously monitor system logs for API errors or failures
2. Adjust retry and rate-limit handling as necessary
3. Document any API changes or throttling behaviors from Amazon

### Environment Variables for Live Mode:
```bash
# Amazon API Configuration
AMAZON_API_MODE=live
AMAZON_API_KEY=your_approved_api_key
AMAZON_API_SECRET=your_approved_secret_key
AMAZON_API_HOST=webservices.amazon.com
AMAZON_API_REGION=us-east-1

# Database Configuration
DB_URL=jdbc:postgresql://your-db-host:5432/your-db-name?sslmode=require
DB_USER=your_db_user
DB_PASS=your_db_password
```

### Enhanced Error Handling for Live Mode:
- Rate limiting with exponential backoff
- API quota monitoring and alerts
- Retry logic for transient failures
- Comprehensive logging for debugging
- Graceful degradation when API is unavailable