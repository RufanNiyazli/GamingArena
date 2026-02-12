# 🎮 Gaming Arena - Real-time Gaming Platform

**Modern gaming platform built with Redis, Spring Boot, and PostgreSQL**

---

## 📋 Table of Contents

- [About the Project](#-about-the-project)
- [Technologies](#-technologies)
- [Architecture](#-architecture)
- [Features](#-features)
- [Installation](#-installation)
- [API Endpoints](#-api-endpoints)
- [Redis Data Structures](#-redis-data-structures)
- [Authentication Flow](#-authentication-flow)
- [Database Schema](#-database-schema)

---

## 🎯 About the Project

**Gaming Arena** is a real-time gaming platform where users can play various games, earn rankings, and compete with each other.

### 🎮 Core Features

✅ **Email-based OTP Authentication** (passwordless login)  
✅ **Real-time Leaderboard** (Sorted Set)  
✅ **Online Users Tracking** (Set)  
✅ **Activity Logging** (List)  
✅ **Like System** (Set)  
✅ **User Profile Cache** (Hash)  
✅ **Session Management** (String + TTL)  
✅ **Friend System** (Set)  

### 🎓 Educational Purpose

This project is designed to teach **all Redis data types** in a real-world scenario:

| Redis Type | Use Case |
|------------|----------|
| STRING | OTP codes, Session tokens |
| HASH | User profile data |
| LIST | Activity log (last 20 actions) |
| SET | Likes, Online users, Friends |
| SORTED SET | Leaderboard (ranking board) |

---

## 🛠️ Technologies

### Backend Stack

```
├── Java 17
├── Spring Boot 3.x
│   ├── Spring Web
│   ├── Spring Security
│   ├── Spring Data JPA
│   ├── Spring Data Redis
│   └── Spring Mail
├── PostgreSQL 15
├── Redis 7.x
├── JWT (jjwt 0.12.3)
├── Lombok
└── Jakarta Validation
```

### External Services

- **Email**: SMTP (Gmail)
- **Cache**: Redis (Lettuce client)
- **Database**: PostgreSQL

---

## 🏗️ Architecture

### Hybrid Database Approach

```
┌─────────────────────────────────────┐
│      POSTGRESQL (Permanent)         │
│  • User data                        │
│  • Game catalog                     │
│  • Match history (archive)          │
│  • Achievements                     │
└─────────────────────────────────────┘
              ↕
┌─────────────────────────────────────┐
│       REDIS (Cache + Real-time)     │
│  • OTP codes (TTL: 60s)             │
│  • Session tokens (TTL: 24h)        │
│  • User profile cache (TTL: 5m)     │
│  • Leaderboard (real-time)          │
│  • Online users (SET)               │
│  • Activity logs (LIST)             │
└─────────────────────────────────────┘
```

### When to Use Which DB?

| Scenario | Primary | Reason |
|---------|---------|--------|
| User registration | PostgreSQL | Permanent data |
| User profile (read) | Redis → PG | Cache-first |
| OTP | Redis | Temporary (60s) |
| Session | Redis | Fast lookup |
| Leaderboard | Redis | Real-time |
| Activity Log | Redis | Last 20 sufficient |
| Online users | Redis | Real-time tracking |

---

## ✨ Features

### 1️⃣ Authentication System

**Email + OTP-based (passwordless)**

```
User registers → Receives OTP via email → 
Enters OTP → Gets JWT token → Logged in
```

#### Security

- ✅ OTP expires in 60 seconds
- ✅ JWT token valid for 24 hours
- ✅ Session stored in Redis
- ✅ New token on each login
- ✅ Email verification required

### 2️⃣ Leaderboard System

**Real-time ranking with Redis Sorted Set**

```redis
ZADD leaderboard:global 15000 user:1001
ZREVRANGE leaderboard:global 0 9 WITHSCORES  # Top 10
ZREVRANK leaderboard:global user:1001         # User's rank
```

#### Features

- ⚡ Real-time updates
- 🏆 Top 10 / Top 100
- 📊 View user's rank
- 🎯 View nearby users (±5)

### 3️⃣ Activity Logging

**Store recent activities with Redis List**

```redis
LPUSH logs:user:1001 "Played Chess Master - Won 150pts"
LTRIM logs:user:1001 0 19  # Keep only 20 logs
LRANGE logs:user:1001 0 19 # Read all logs
```

### 4️⃣ Online Users Tracking

**Real-time online users with Redis Set**

```redis
SADD online:users user:1001      # Login
SREM online:users user:1001      # Logout
SMEMBERS online:users            # Get all
SCARD online:users               # Count
```

### 5️⃣ Like System

**Unique likes with Redis Set**

```redis
SADD likes:game:501 user:1001    # Like
SREM likes:game:501 user:1001    # Unlike
SISMEMBER likes:game:501 user:1001  # Check
SCARD likes:game:501             # Total count
```

---

## 🚀 Installation

### Requirements

```bash
Java 17+
PostgreSQL 15+
Redis 7+
Maven 3.8+
```

### 1️⃣ Clone Repository

```bash
git clone https://github.com/username/gaming-arena.git
cd gaming-arena
```

### 2️⃣ PostgreSQL Configuration

```sql
CREATE DATABASE gaming_arena;
CREATE USER arena_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE gaming_arena TO arena_user;
```

### 3️⃣ Start Redis

```bash
# With Docker
docker run -d -p 6379:6379 redis:7-alpine

# Local installation
redis-server
```

### 4️⃣ application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/gaming_arena
    username: arena_user
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

jwt:
  secret: your-super-secret-key-min-256-bit
  expiration: 86400000  # 24 hours
```

### 5️⃣ Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Server: `http://localhost:8080`

---

## 📡 API Endpoints

### 🔐 Authentication

#### 1. Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "pro_gamer"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful. OTP sent to email.",
  "data": {
    "userId": 1001,
    "email": "user@example.com",
    "otpExpiresIn": 60
  }
}
```

#### 2. Send OTP

```http
POST /api/auth/send-otp
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent to email",
  "data": {
    "expiresIn": 60
  }
}
```

#### 3. OTP Verification + Login

```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "5482"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1001,
      "username": "pro_gamer",
      "email": "user@example.com",
      "level": 1,
      "totalScore": 0
    }
  }
}
```

### 👤 User Operations

```http
GET /api/users/me
Authorization: Bearer {token}

GET /api/users/{id}
GET /api/users/{id}/activity
PUT /api/users/{id}
```

### 🎮 Game Operations

```http
GET /api/games
GET /api/games/{id}
POST /api/games/{id}/like
POST /api/games/{id}/play
```

### 🏆 Leaderboard

```http
GET /api/leaderboard/top10
GET /api/leaderboard/rank/{userId}
GET /api/leaderboard/around/{userId}
POST /api/leaderboard/add-score
```

### 👥 Social

```http
GET /api/users/online
POST /api/users/{id}/friends/add
GET /api/users/{id}/friends
```

---

## 🔴 Redis Data Structures

### 1️⃣ STRING - OTP & Sessions

```redis
# OTP
SET otp:user@example.com "5482" EX 60

# Session Token
SET token:user:1001 "eyJhbGci..." EX 86400
```

### 2️⃣ HASH - User Profile Cache

```redis
HMSET user:1001 
  username "pro_gamer"
  email "user@example.com"
  level "1"
  totalScore "0"
  avatar "default.png"

HGETALL user:1001
HINCRBY user:1001 totalScore 150
```

### 3️⃣ LIST - Activity Log

```redis
# Add new activity
LPUSH logs:user:1001 "Played Chess - Won 150pts [2024-02-12 15:30]"

# Keep only 20 logs
LTRIM logs:user:1001 0 19

# Read all
LRANGE logs:user:1001 0 -1
```

### 4️⃣ SET - Online Users & Likes

```redis
# Online users
SADD online:users user:1001 user:1002
SMEMBERS online:users
SCARD online:users

# Likes
SADD likes:game:501 user:1001
SISMEMBER likes:game:501 user:1001
SCARD likes:game:501
```

### 5️⃣ SORTED SET - Leaderboard

```redis
# Add score
ZADD leaderboard:global 15000 user:1001
ZINCRBY leaderboard:global 150 user:1001

# Top 10
ZREVRANGE leaderboard:global 0 9 WITHSCORES

# User rank
ZREVRANK leaderboard:global user:1001

# Score range
ZREVRANGEBYSCORE leaderboard:global 15000 10000
```

---

## 🔐 Authentication Flow

```
┌─────────────────────────────────────────────────┐
│         1. REGISTER                             │
│  POST /api/auth/register                        │
│  { email, username }                            │
└─────────────────────────────────────────────────┘
                    ↓
         ┌──────────────────────┐
         │ Save user to PG      │
         │ Generate OTP: "5482" │
         │ Redis: SET otp:... EX│
         │ Send email           │
         └──────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│         2. VERIFY OTP                           │
│  POST /api/auth/verify-otp                      │
│  { email, otp: "5482" }                         │
└─────────────────────────────────────────────────┘
                    ↓
         ┌──────────────────────┐
         │ Redis: GET otp:...   │
         │ OTP valid? ✅        │
         │ Generate JWT token   │
         │ Redis: SET token:... │
         │ Add to online users  │
         └──────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│         3. AUTHENTICATED REQUESTS               │
│  Authorization: Bearer {token}                  │
└─────────────────────────────────────────────────┘
                    ↓
         ┌──────────────────────┐
         │ JwtAuthFilter        │
         │ Token valid? ✅      │
         │ Load user            │
         │ Set SecurityContext  │
         └──────────────────────┘
```

---

## 🗄️ Database Schema

### PostgreSQL Tables

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    level INTEGER DEFAULT 1,
    total_score BIGINT DEFAULT 0,
    avatar VARCHAR(255) DEFAULT 'default.png',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Games table
CREATE TABLE games (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    difficulty VARCHAR(20),
    max_score INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Matches table
CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    game_id BIGINT REFERENCES games(id),
    score INTEGER,
    result VARCHAR(20), -- WIN, LOSE, DRAW
    duration_seconds INTEGER,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Achievements table
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(100),
    description TEXT,
    achieved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🧪 Testing

### Postman Collection

```bash
# Import collection
postman/gaming-arena.postman_collection.json
```

### Manual Test Flow

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"tester"}'

# 2. Get OTP from email (will show in console during development)

# 3. Verify OTP
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","otp":"5482"}'

# 4. Access protected endpoint with token
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGci..."
```

---

## 📚 Redis Commands Reference

### For Debugging

```bash
# Redis CLI
redis-cli

# See all keys
KEYS *

# Specific pattern
KEYS otp:*
KEYS user:*

# Key type
TYPE leaderboard:global

# Check TTL
TTL otp:user@example.com

# Delete
DEL otp:user@example.com
FLUSHALL  # DELETE EVERYTHING (CAREFUL!)
```

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first.

---

## 📄 License

MIT License

---

## 👨‍💻 Developer

**Gaming Arena Team**

📧 Email: info@gamingarena.com  
🌐 Website: https://gamingarena.com  
📱 GitHub: https://github.com/gamingarena

---

## 🙏 Acknowledgments

- Redis Documentation
- Spring Boot Guides
- Baeldung Tutorials

---

**⭐ If you like this project, don't forget to give it a star!**
