# 🎮 Gaming Arena - Real-time Oyun Platforması

**Redis, Spring Boot və PostgreSQL ilə hazırlanmış müasir oyun platforması**

---

## 📋 Mündəricat

- [Layihə Haqqında](#-layihə-haqqında)
- [Texnologiyalar](#-texnologiyalar)
- [Arxitektura](#-arxitektura)
- [Xüsusiyyətlər](#-xüsusiyyətlər)
- [Quraşdırma](#-quraşdırma)
- [API Endpoints](#-api-endpoints)
- [Redis Data Strukturları](#-redis-data-strukturları)
- [Authentication Flow](#-authentication-flow)
- [Database Schema](#-database-schema)

---

## 🎯 Layihə Haqqında

**Gaming Arena** - istifadəçilərin müxtəlif oyunlar oynaya, reytinq toplaya və bir-biri ilə yarışa biləcəyi real-time oyun platformasıdır.

### 🎮 Əsas Funksiyalar

✅ **Email ilə OTP-based Authentication** (şifrəsiz giriş)  
✅ **Real-time Leaderboard** (Sorted Set)  
✅ **Online Users Tracking** (Set)  
✅ **Activity Logging** (List)  
✅ **Like System** (Set)  
✅ **User Profil Cache** (Hash)  
✅ **Session Management** (String + TTL)  
✅ **Friend System** (Set)  

### 🎓 Təhsil Məqsədi

Bu layihə **Redis-in bütün data type-larını** real ssenaridə öyrətmək üçün hazırlanıb:

| Redis Type | İstifadə yeri |
|------------|---------------|
| STRING | OTP kodları, Session tokens |
| HASH | User profil məlumatları |
| LIST | Activity log (son 20 hərəkət) |
| SET | Likes, Online users, Friends |
| SORTED SET | Leaderboard (reytinq lövhəsi) |

---

## 🛠️ Texnologiyalar

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

## 🏗️ Arxitektura

### Hibrid Database Yanaşması

```
┌─────────────────────────────────────┐
│      POSTGRESQL (Permanent)         │
│  • User məlumatları                 │
│  • Game kataloqu                    │
│  • Match history (arxiv)            │
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

### Nə vaxt hansı DB?

| Ssenari | Primary | Reason |
|---------|---------|--------|
| User qeydiyyat | PostgreSQL | Permanent data |
| User profil (read) | Redis → PG | Cache-first |
| OTP | Redis | Müvəqqəti (60s) |
| Session | Redis | Tez yoxlama |
| Leaderboard | Redis | Real-time |
| Activity Log | Redis | Son 20 kifayət |
| Online users | Redis | Real-time tracking |

---

## ✨ Xüsusiyyətlər

### 1️⃣ Authentication System

**Email + OTP-based (şifrəsiz)**

```
User qeydiyyat → Email ilə OTP alır → 
OTP daxil edir → JWT token alır → Login olur
```

#### Təhlükəsizlik

- ✅ OTP 60 saniyə yaşayır
- ✅ JWT token 24 saat valid
- ✅ Session Redis-də saxlanılır
- ✅ Hər login-də yeni token
- ✅ Email verification required

### 2️⃣ Leaderboard System

**Redis Sorted Set ilə real-time reytinq**

```redis
ZADD leaderboard:global 15000 user:1001
ZREVRANGE leaderboard:global 0 9 WITHSCORES  # Top 10
ZREVRANK leaderboard:global user:1001         # User-in yeri
```

#### Xüsusiyyətlər

- ⚡ Real-time yeniləmə
- 🏆 Top 10 / Top 100
- 📊 User-in rank-ını görmə
- 🎯 Ətraf user-ləri görmə (±5)

### 3️⃣ Activity Logging

**Redis List ilə son hərəkətlərin saxlanması**

```redis
LPUSH logs:user:1001 "Played Chess Master - Won 150pts"
LTRIM logs:user:1001 0 19  # Yalnız 20 log saxla
LRANGE logs:user:1001 0 19 # Bütün logları oxu
```

### 4️⃣ Online Users Tracking

**Redis Set ilə real-time online users**

```redis
SADD online:users user:1001      # Login
SREM online:users user:1001      # Logout
SMEMBERS online:users            # Hamısını göstər
SCARD online:users               # Say
```

### 5️⃣ Like System

**Redis Set ilə unique likes**

```redis
SADD likes:game:501 user:1001    # Like
SREM likes:game:501 user:1001    # Unlike
SISMEMBER likes:game:501 user:1001  # Check
SCARD likes:game:501             # Total count
```

---

## 🚀 Quraşdırma

### Tələblər

```bash
Java 17+
PostgreSQL 15+
Redis 7+
Maven 3.8+
```

### 1️⃣ Repository Clone

```bash
git clone https://github.com/username/gaming-arena.git
cd gaming-arena
```

### 2️⃣ PostgreSQL Konfiqurasiyası

```sql
CREATE DATABASE gaming_arena;
CREATE USER arena_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE gaming_arena TO arena_user;
```

### 3️⃣ Redis Başlatma

```bash
# Docker ilə
docker run -d -p 6379:6379 redis:7-alpine

# Yerli quraşdırma
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
  expiration: 86400000  # 24 saat
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

#### 1. Qeydiyyat

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
  "message": "Qeydiyyat uğurlu. OTP email-ə göndərildi.",
  "data": {
    "userId": 1001,
    "email": "user@example.com",
    "otpExpiresIn": 60
  }
}
```

#### 2. OTP Göndərmə

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
  "message": "OTP email-ə göndərildi",
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
  "message": "Login uğurlu",
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

## 🔴 Redis Data Strukturları

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
# Yeni activity əlavə et
LPUSH logs:user:1001 "Played Chess - Won 150pts [2024-02-12 15:30]"

# Yalnız 20 log saxla
LTRIM logs:user:1001 0 19

# Hamısını oxu
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
# Xal əlavə et
ZADD leaderboard:global 15000 user:1001
ZINCRBY leaderboard:global 150 user:1001

# Top 10
ZREVRANGE leaderboard:global 0 9 WITHSCORES

# User rank
ZREVRANK leaderboard:global user:1001

# Score aralığı
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
         │ PostgreSQL-ə User yaz│
         │ OTP generate: "5482" │
         │ Redis: SET otp:... EX│
         │ Email göndər         │
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
         │ OTP düzgündür? ✅    │
         │ JWT token yarat      │
         │ Redis: SET token:... │
         │ Online users-ə əlavə │
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
         │ User load            │
         │ SecurityContext set  │
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

## 🧪 Test Etmə

### Postman Collection

```bash
# Collection import et
postman/gaming-arena.postman_collection.json
```

### Manual Test Flow

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"tester"}'

# 2. Email-dən OTP al (console-da görsənəcək development-də)

# 3. Verify OTP
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","otp":"5482"}'

# 4. Token ilə protected endpoint
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer eyJhbGci..."
```

---

## 📚 Redis Komandaları Referansı

### Debugging üçün

```bash
# Redis CLI
redis-cli

# Bütün key-ləri gör
KEYS *

# Specific pattern
KEYS otp:*
KEYS user:*

# Key type
TYPE leaderboard:global

# TTL yoxla
TTL otp:user@example.com

# Sil
DEL otp:user@example.com
FLUSHALL  # HƏR ŞEYİ SİL (DİQQƏTLİ!)
```

---

## 🤝 Contribution

Pull requests qəbul edilir. Böyük dəyişikliklər üçün əvvəlcə issue açın.

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

**⭐ Bu layihəni bəyəndinizsə ulduz verməyi unutmayın!**
