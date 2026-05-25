# 🏗️ ViralForge AI - System Architecture

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Diagram](#architecture-diagram)
3. [Technology Stack](#technology-stack)
4. [Data Flow](#data-flow)
5. [API Design](#api-design)
6. [Database Schema](#database-schema)
7. [AI Model Orchestration](#ai-model-orchestration)
8. [Security Architecture](#security-architecture)
9. [Scalability & Performance](#scalability--performance)
10. [Deployment Architecture](#deployment-architecture)

---

## System Overview

ViralForge AI is a **three-tier distributed system** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│           Frontend Layer (Angular 17)                     │
│  - Auth UI, Generator, Analytics, History, Trending     │
│  - Responsive Design, Real-time Updates                 │
└──────────────────┬──────────────────────────────────────┘
                   │ HTTP/REST
┌──────────────────▼──────────────────────────────────────┐
│        Business Logic Layer (Spring Boot 3)              │
│  - Content Generation, Language Adaptation              │
│  - AI Orchestration, Analytics Processing               │
│  - User Management, Security                            │
└──────────────────┬──────────────────────────────────────┘
                   │ SQL/JDBC
┌──────────────────▼──────────────────────────────────────┐
│        Data Layer (PostgreSQL + Cache)                  │
│  - User Data, Content, Analytics                        │
│  - Audit Logs, API Usage Tracking                       │
└─────────────────────────────────────────────────────────┘
                   │ HTTP
         ┌─────────▼─────────┐
         │  Hugging Face API  │
         │  (Phi, Llama, etc) │
         └───────────────────┘
```

---

## Architecture Diagram

### High-Level System Architecture

```
User → Angular Frontend → REST API → Spring Boot Backend → PostgreSQL
                ↓                           ↓                    ↓
              JWT Auth              JWT Validation          User/Content
              State Mgmt            Business Logic          Data Storage
              Components            AI Orchestration
                                   Analytics Engine
                                          ↓
                                   Hugging Face API
                                   (Phi/Llama/Mistral)
```

### Content Generation Flow

```
User Input
   ↓
[Form Submission] → REST API: /content/generate
   ↓
[Backend Processing]
   ├─ Phi Service: Audience Analysis
   │  ├─ Input: Topic, Niche, Audience, Platform
   │  └─ Output: AudienceAnalysisDTO (viralScore, confidence)
   ├─ Content Type Routing
   │  ├─ Educational → Llama Service
   │  └─ Entertainment → Mistral Service
   │     └─ Output: Full Script
   └─ Mistral Service: Growth Strategy
      └─ Output: PostingStrategy, Hashtags, CTA
   ↓
[Language Adaptation]
   ├─ Mistral Service: Translate to Indian Languages
   ├─ Cultural Nuance Adaptation
   └─ Generate SRT Subtitles
   ↓
[Save to Database]
   ├─ GeneratedContent Entity
   ├─ Update User API Usage
   └─ Log Analytics
   ↓
Response → Angular Frontend
   ↓
Display Results (Scripts in 10 languages)
```

---

## Technology Stack

### Backend Stack
| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Spring Boot | 3.3.0 | REST API & DI |
| **Language** | Java | 21 | Core Application |
| **Security** | Spring Security | 6.x | Auth & Authorization |
| **ORM** | JPA/Hibernate | 6.x | Database Mapping |
| **Database** | PostgreSQL | 15+ | Data Storage |
| **JSON** | Gson | 2.10 | JSON Processing |
| **Build** | Maven | 3.9+ | Dependency Management |
| **Logging** | SLF4J/Logback | 2.0 | Application Logging |

### Frontend Stack
| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Angular | 17 | SPA Framework |
| **Language** | TypeScript | 5.2+ | Type Safety |
| **HTTP Client** | HttpClient | 17 | API Communication |
| **Forms** | Reactive Forms | 17 | Form Handling |
| **Styling** | Tailwind CSS | 3.3+ | Utility-first CSS |
| **Build** | Angular CLI | 17 | Project Build |
| **Package Manager** | npm | 9+ | Dependency Mgmt |

### AI & External Services
| Service | Model | Provider | Purpose |
|---------|-------|----------|---------|
| **Audience Analysis** | Phi-3-mini | Hugging Face (featherless-ai) | Viral Scoring |
| **Script Generation** | Llama 3.8B | Hugging Face (novita) | Educational Content |
| **Entertainment** | Mistral 7B | Hugging Face (featherless-ai) | Viral Scripts |
| **Language Adaptation** | Mistral 7B | Hugging Face (featherless-ai) | Multi-language |

---

## Data Flow

### 1. Authentication Flow

```
User Credentials
   ↓
POST /auth/register or /auth/login
   ↓
AuthService.java
   ├─ Validate Input
   ├─ Hash Password (BCrypt)
   ├─ Check Database
   └─ Generate JWT Token
   ↓
JWT Token Response
   ↓
Frontend stores in localStorage
   ↓
Subsequent requests include JWT in Authorization header
   ↓
AuthFilter validates token
```

### 2. Content Generation Flow

```
User Input
   ↓
GeneratorComponent.ts
   ├─ Validate Form
   ├─ Submit to apiService.generateContent()
   └─ Show Loading State
   ↓
REST POST /api/content/generate
   ↓
ContentController.generateContent()
   ├─ Get Current User
   ├─ Create ContentGenerationRequest
   └─ Call AIOrchestratorService
   ↓
AIOrchestratorService.orchestrateContentGeneration()
   ├─ Call PhiService.analyzeAudience()
   │  └─ HTTP → Hugging Face API
   │     ↓ Parse JSON Response
   │     ↓ Return AudienceAnalysisDTO
   │
   ├─ Route by ContentType
   │  ├─ EDUCATIONAL → LlamaService.generateEducationalScript()
   │  └─ ENTERTAINMENT → MistralService.generateEntertainmentScript()
   │     ↓ Parse JSON Response
   │     ↓ Return ScriptDTO
   │
   ├─ Call MistralService.buildGrowthStrategy()
   │  ↓ Parse JSON Response
   │  ↓ Return GrowthStrategyDTO
   │
   └─ Merge results → ContentGenerationResponseDTO
   ↓
Save to Database
   ├─ GeneratedContent entity
   ├─ Increment user.apiUsageCount
   └─ Save to PostgreSQL
   ↓
Response to Frontend
   ↓
GeneratorComponent displays results
   ├─ Show Viral Score
   ├─ Display Script
   ├─ Show Language Tabs
   └─ Adapt to Selected Languages
```

### 3. Multi-Language Adaptation Flow

```
Generated Content (English)
   ↓
Frontend: adaptContentToLanguages()
   ├─ Collect selected languages
   └─ POST /api/content/adapt-languages
   ↓
LanguageAdaptationService.adaptContentToLanguages()
   ├─ For each language:
   │  └─ Call translateContent()
   │     ├─ Build prompt with cultural context
   │     ├─ HTTP → Mistral API
   │     ├─ Parse response
   │     └─ Return translated text
   │
   └─ Return translations Map
   ↓
Response with all language versions
   ↓
Frontend displays in language tabs
```

### 4. Trending Data Flow

```
User navigates to /trending
   ↓
TrendingComponent.ngOnInit()
   └─ Call apiService.getTrendingContent()
   ↓
REST GET /api/trending/all
   ↓
TrendingController.getTrendingContent()
   └─ Call TrendingService.getTrendingContent()
   ↓
TrendingService.getTrendingContent()
   ├─ Call TrendingService.getTrendingTopicsFromAI()
   │  ├─ HTTP → Mistral API
   │  ├─ Parse trending JSON
   │  └─ Return topics, hashtags, challenges
   │
   ├─ Parse for each platform
   │  ├─ TikTok Trends
   │  ├─ Instagram Reels Trends
   │  └─ YouTube Shorts Trends
   │
   └─ Return trends Map
   ↓
Frontend displays tabs for each platform
```

---

## API Design

### REST API Principles
- **Stateless**: All state in JWT token
- **RESTful**: Standard HTTP methods
- **Versioned**: Future-proof with `/api/v1/`
- **Documented**: Swagger/OpenAPI ready

### Base URL
```
http://localhost:8081/api
```

### Request Headers (Protected Endpoints)
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Standard Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* response data */ },
  "error": null,
  "timestamp": 1716734400000,
  "statusCode": 200
}
```

### Error Response Format
```json
{
  "success": false,
  "message": "Operation failed",
  "data": null,
  "error": "Detailed error message",
  "timestamp": 1716734400000,
  "statusCode": 400
}
```

### Key Endpoints

#### Authentication
```
POST   /auth/register          Register new user
POST   /auth/login             User login
GET    /auth/me                Get current user
GET    /auth/health            Auth health check
```

#### Content
```
POST   /content/generate               Generate content
GET    /content/history                Get user's content
POST   /content/adapt-languages        Adapt to languages
GET    /content/languages              List available languages
POST   /content/generate-subtitles     Generate SRT subtitles
GET    /content/health                 Content service health
```

#### Analytics
```
GET    /analytics/dashboard    Get dashboard analytics
```

#### Trending
```
GET    /trending/all           Get all trending data
GET    /trending/{platform}    Get platform trends
```

---

## Database Schema

### Core Entities

#### User Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(255),
  avatar_url VARCHAR(500),
  api_usage_count INT DEFAULT 0,
  max_monthly_api_calls INT DEFAULT 1000,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_active BOOLEAN DEFAULT true
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_created_at ON users(created_at DESC);
```

#### GeneratedContent Table
```sql
CREATE TABLE generated_content (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL REFERENCES users(id),
  content_request JSON,
  script_content TEXT NOT NULL,
  viral_score DECIMAL(3,1),
  confidence_score DECIMAL(5,2),
  primary_model_used VARCHAR(255),
  fallback_model_used VARCHAR(255),
  audience_type TEXT,
  recommended_tone TEXT,
  content_style TEXT,
  trend_alignment TEXT,
  platform_optimization TEXT,
  posting_strategy JSON,
  hashtags JSON,
  viral_hooks JSON,
  recommended_cta VARCHAR(500),
  generation_latency_ms BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_content_user ON generated_content(user_id);
CREATE INDEX idx_content_created ON generated_content(created_at DESC);
```

#### Analytics Table (Optional)
```sql
CREATE TABLE analytics_snapshot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL REFERENCES users(id),
  total_generated INT,
  avg_viral_score DECIMAL(3,1),
  avg_confidence DECIMAL(5,2),
  platform_stats JSON,
  model_stats JSON,
  recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Relationships
```
User (1) ──── (Many) GeneratedContent
User (1) ──── (Many) AnalyticsSnapshot
```

---

## AI Model Orchestration

### Three-Step Content Generation Pipeline

#### Step 1: Audience Analysis (Phi Model)
```
Input: {
  niche, vibe, topic, audience, platform
}

Process:
- Analyze audience behavior patterns
- Identify viral potential factors
- Assess content fit for platform
- Calculate confidence score

Output: AudienceAnalysisDTO {
  viralScore: 7.5,
  confidenceScore: 87.5,
  audienceType: "Gen Z Fitness Enthusiasts",
  recommendedTone: "Energetic and Motivational",
  contentStyle: "Visual Storytelling",
  engagementTriggers: ["Urgency", "Inspiration"],
  hashtags: ["#FitnessGoals", "#Motivation"],
  trendAlignment: "High",
  viralHooks: ["Transformation Story"],
  recommendedCta: "Follow for more tips"
}
```

#### Step 2: Conditional Script Generation

**If Educational:**
```
Llama 3.8B Model
- Longer, structured content
- Focus on learning retention
- Step-by-step breakdowns
- Authority building tone
```

**If Entertainment:**
```
Mistral 7B Model
- Shorter, punchy scripts
- Emotional hooks
- Trending format alignment
- Viral engagement focus
```

#### Step 3: Growth Strategy (Mistral Model)
```
Output: GrowthStrategyDTO {
  bestPostingTime: "4-8 PM peak hours",
  postingSchedule: "Post 3-5 times weekly",
  seoHashtags: ["#Trending", "#FitnessLife"],
  thumbnailText: "Instant 6 Pack?",
  captionHook: "Swipe for the secret...",
  engagementTriggers: ["Surprise Reveals"],
  engagementStrategy: "Duet and Stitch optimized",
  platformOptimization: "TikTok vertical 9:16",
  crossPlatformStrategy: "Repurpose for all 3 platforms",
  collaborationOpportunities: "Fitness influencer collab",
  seriesIdea: "7-day transformation",
  trendingAudio: "Current viral sounds",
  analyticsToTrack: "Watch time, shares, saves",
  growthHacks: "First 3 seconds critical"
}
```

### Failover Strategy

```
Generate(Llama) 
├─ Success → Return result
├─ Fail (Retry 3x) → Fallback to Mistral
│  ├─ Success → Return + Log failure
│  └─ Fail → Return error + Mock data
└─ Max Latency 30s → Timeout
```

---

## Security Architecture

### Authentication Layer
```
┌─ User Input (Credentials)
│
├─ Validation
│  └─ Email format, Password strength
│
├─ Database Lookup
│  └─ Find user by email
│
├─ Password Verification
│  └─ BCrypt.matches(input, stored_hash)
│
├─ Token Generation
│  ├─ JWT Claims: userId, email, roles
│  ├─ Secret: HMAC-SHA256(secret_key)
│  ├─ Expiry: 24 hours
│  └─ Signed Token
│
└─ Response: JWT Token to Client
```

### Authorization Layer
```
Request with JWT
   ↓
JwtFilter extracts token
   ↓
Validate signature
   ├─ Invalid signature → 401 Unauthorized
   ├─ Token expired → 401 Unauthorized
   └─ Valid → Continue
   ↓
Extract userId from claims
   ↓
@PreAuthorize("isAuthenticated()") checks
   ├─ Missing token → 403 Forbidden
   └─ Valid → Allow access
```

### Input Validation
```
User Input → PromptSanitizer
   ├─ Remove SQL injection patterns
   ├─ Remove script/XSS patterns
   ├─ Limit length constraints
   ├─ Validate format/type
   └─ Return sanitized input
   ↓
Use in AI prompts & database
```

### Data Security
```
Sensitive Data:
├─ Passwords → BCrypt hashed
├─ Tokens → HMAC signed
├─ API Keys → Environment variables
└─ User Data → User-isolated queries

Database:
├─ Connections over SSL/TLS
├─ SQL injection prevention (Parameterized queries)
└─ Access logs/audit trails
```

---

## Scalability & Performance

### Horizontal Scaling

```
Load Balancer (Nginx)
     ↓
   ┌─┴─┐
   │   │
Backend 1, Backend 2, Backend 3 (stateless Spring Boot)
   │   │
   └─┬─┘
     ↓
Database (PostgreSQL with replication)
```

### Vertical Optimization

**Frontend Optimization:**
- Code splitting (lazy loading)
- Tree shaking (unused code removal)
- Minification & compression
- Caching strategies

**Backend Optimization:**
- Connection pooling (HikariCP)
- Query optimization (indexes)
- Pagination (limit results)
- Caching layer (Redis ready)

### Database Performance

```sql
-- Indexes for common queries
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_content_user_created ON generated_content(user_id, created_at DESC);
CREATE INDEX idx_analytics_user_date ON analytics_snapshot(user_id, recorded_at DESC);

-- Connection pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

-- Query timeout
spring.datasource.hikari.connection-timeout=30000
```

### AI Model Optimization

```
Timeout: 30 seconds max per call
Retry: 3 attempts with exponential backoff (1s, 2s, 4s)
Batch: Process in parallel when possible
Cache: Recently used prompts (optional Redis)
```

---

## Deployment Architecture

### Development Environment
```
Developer Machine
├─ Angular Dev Server (port 4200)
├─ Spring Boot Dev (port 8081)
├─ PostgreSQL Local
└─ Hugging Face API (free tier)
```

### Production Environment (AWS Example)

```
┌─────────────────────────────────────────┐
│        AWS Infrastructure                │
├─────────────────────────────────────────┤
│                                          │
│  ┌──────────────────────────────────┐  │
│  │      CloudFront CDN              │  │
│  │  (Static Angular Assets)         │  │
│  └──────────┬───────────────────────┘  │
│             │                           │
│  ┌──────────▼───────────────────────┐  │
│  │   Application Load Balancer      │  │
│  │   (HTTPS/TLS Termination)        │  │
│  └──────────┬───────────────────────┘  │
│             │                           │
│  ┌──────────▼───────────────────────┐  │
│  │      ECS/EC2 Cluster             │  │
│  │  ├─ Spring Boot Container 1      │  │
│  │  ├─ Spring Boot Container 2      │  │
│  │  └─ Spring Boot Container N      │  │
│  └──────────┬───────────────────────┘  │
│             │                           │
│  ┌──────────▼───────────────────────┐  │
│  │   RDS PostgreSQL Database        │  │
│  │   ├─ Multi-AZ (HA)               │  │
│  │   └─ Automated Backups           │  │
│  └──────────────────────────────────┘  │
│             │                           │
│  ┌──────────▼───────────────────────┐  │
│  │   ElastiCache (Optional)         │  │
│  │   (Redis for session store)      │  │
│  └──────────────────────────────────┘  │
│                                          │
└─────────────────────────────────────────┘
        │
        ├─ CloudWatch Logs
        ├─ CloudWatch Metrics
        ├─ SNS Alerts
        └─ VPC Security Groups
```

### Docker Deployment

```dockerfile
# Backend Docker
FROM openjdk:21-slim
WORKDIR /app
COPY backend/target/*.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]

# Frontend Docker
FROM node:18-alpine AS builder
WORKDIR /app
COPY frontend . 
RUN npm install && npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Kubernetes Deployment (Optional)

```yaml
# Spring Boot Service
apiVersion: apps/v1
kind: Deployment
metadata:
  name: viralforge-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: viralforge-backend
  template:
    metadata:
      labels:
        app: viralforge-backend
    spec:
      containers:
      - name: viralforge-backend
        image: viralforge:latest
        ports:
        - containerPort: 8081
        env:
        - name: DB_HOST
          value: postgres-service
        - name: HF_API_KEY
          valueFrom:
            secretKeyRef:
              name: hf-api-key
              key: api-key
```

---

## Summary

ViralForge AI uses a **modern, scalable, three-tier architecture** with:

✅ **Clear separation of concerns** (Frontend, API, Database)
✅ **AI orchestration** for intelligent model routing
✅ **Security-first** approach with JWT and prompt sanitization
✅ **Multi-language support** for Indian languages
✅ **Real-time trending** insights
✅ **Performance optimized** with indexing and pagination
✅ **Production-ready** with proper error handling and logging
✅ **Cloud-agnostic** deployment (AWS, Azure, GCP)

---

**Last Updated**: May 26, 2026
**Architecture Version**: 1.0
