
# Backend Engineering Assignment
## Core API & Guardrails Microservice

## Overview

*This project implements a high-performance, stateless Spring Boot microservice that acts as a central API layer with strict guardrails enforced via Redis.*  
_The system is designed to:_

* Handle high concurrency safely
* Prevent AI compute runaway via guardrails
* Maintain data integrity between PostgreSQL and Redis
* Support event-driven notification batching

### Tech Stack

* Java 17
* Spring Boot 3.x
* PostgreSQL
* Redis (Spring Data Redis)
* Docker / Docker Compose

## Setup Instructions
1. Clone Repository
```
git clone <your-repo-url> cd <project-folder>
```
2. Start Dependencies (Postgres + Redis)
```
docker-compose up -d
```
3. Run Application

```
./mvnw spring-boot:run
```

## Database Schema

## Entities
## User
```
id
username
is_premium
```
## Bot
```
id
name
persona_desciption
```
## Post
```
id
author_id
content
created_at
```
## Comment
```
id
post_id
author_id
content
depth_level
created_at
```
## API Endpoints
## Create Post

* POST 
```
/api/posts

```
## Add Comment
* POST
```
 /api/posts/{id}/comments
```
## Like 

* POST 
```
/api/posts/{id}/like

```

## Phase 2: Redis Virality Engine
## Virality Score Rules


| Interaction | Score |
| -------- | -------- |
| Bot | +1 |
| Human Like | +20 |
| Human Comment | +50 |

## Thread Safety & Atomic Locks (Phase 2 Approach)
* I relied entirely on Redis atomic operations as the single source of coordination.

### 1. Horizontal Cap (Max 100 Bot Replies)
* **Problem :** Multiple bots may attempt to comment on the same post simultaneously, causing race conditions.

* **Solution:** We use Redis atomic increment to enforce the limit.

* Implementation Flow

*Increment bot counter in Redis:
```
Long count = redisTemplate.opsForValue()
    .increment("post:" + postId + ":bot_count");
```
#### Validate limit:
```
if (count > 100) {
    redisTemplate.opsForValue()
        .decrement("post:" + postId + ":bot_count");
    throw new TooManyRequestsException("Bot limit reached");
}
```
* Only proceed to DB write if valid
#### Why this is thread-safe
* increment in Redis is atomic
* Even under 200 concurrent requests:
    * Only first 100 increments succeed logically
    * Remaining requests are rejected immediately
### 2. Vertical Cap (Max Depth = 20)
* **Problem:** Deep recursive comment chains can cause performance issues.

* **Solution **Validated at application level before DB write.
```
if (depthLevel > 20) {
    throw new BadRequestException("Max depth exceeded");
}
```
#### Why this is safe
* Depth is request-scoped (no shared state)
* No concurrency risk
### 3. Cooldown Cap (Bot ↔ Human Interaction)
* **Problem** :Prevent a bot from repeatedly interacting with the same user within 10 minutes.

* **Solution** * :Use Redis SET with TTL (atomic lock with expiry).

#### Implementation
```
String key = "cooldown:bot_" + botId + ":human_" + userId;

Boolean success = redisTemplate.opsForValue().setIfAbsent(
    key,
    "1",
    Duration.ofMinutes(10)
);

if (Boolean.FALSE.equals(success)) {
    throw new TooManyRequestsException("Cooldown active");
}
```
#### Why this is thread-safe
* SET atomic in Redis
* Guarantees:
    * Only one request succeeds
    * Others fail immediately
* TTL ensures automatic expiration (no manual cleanup)

## Phase 3: Notification Engine
### Redis Throttler

* Key:
```
user:{id}:notif_cooldown
```
* If cooldown exists:

    * Push to:
```
user:{id}:pending_notifs (Redis List)
```
* Else:
    * Send notification immediately
    * Set 15-minute cooldown
### CRON Sweeper
* Runs every 5 minutes
* Logic:
    * Scan users with pending notifications
    * Aggregate messages
    * Log:
```
"Bot X and [N] others interacted with your posts"
```
* Clear Redis list

## Phase 4: Corner Cases & Testing Criteria
* To simulate real-world concurrency, we generated 200 parallel bot requests targeting a single post.

* Tools Used
```
Windows Batch Script (.bat)
PowerShell Script (.ps1)
Native HTTP client (Invoke-RestMethod)
```

## How to Run the Test
#### Step 1: Start Application

* Ensure the backend is running:
```
http://localhost:8080
```
#### Step 2: Execute Load Test

* Run the batch file:
```
testing/run-test.bat
```
#### This will:

* Launch PowerShell
* Execute ``` loadtest.ps1```
* Send 200 concurrent POST requests
* Limit concurrency to 20 parallel threads

##What We Validate
1. Horizontal Cap Enforcement

* Expected Result:

    * Only 100 bot comments should be stored

#### Verification Query (PostgreSQL):
```
SELECT COUNT(*) FROM comments WHERE post_id = 1;
```
* Expected Output:
```
100
```
2. API Rejection Behavior

* Requests beyond limit should return:
```
HTTP 429 Too Many Requests
```
* This confirms:

    * Redis guardrail triggered correctly
    * No overflow into database
3. Redis Counter Accuracy

* Check Redis key:
```
GET post:1:bot_count
```
* Expected:
```
100
```
4. Data Integrity Guarantee

* Flow enforced:
```
Request → Redis Guard Check → DB Insert → Response
```
* If Redis rejects → DB is untouched and Ensures no invalid writes

