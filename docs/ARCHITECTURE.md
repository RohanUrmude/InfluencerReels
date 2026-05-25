# ViralForge AI - System Architecture

## Overview

ViralForge AI is a **multi-tier, AI-orchestrated content generation platform** that leverages Hugging Face LLM APIs to create viral short-form social media content.

### Architecture Pattern: Clean Architecture + Microservice Principles

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND TIER                         │
│         Angular 17 (Standalone Components)              │
│    TailwindCSS + Material Design + RxJS Reactivity     │
└──────────────────────┬──────────────────────────────────┘
                       │
           ┌───────────┴────────────┐
           │ HTTP/REST (CORS)       │
           │ JWT Bearer Auth        │
           ▼                        │
┌─────────────────────────────────────────────────────────┐
│              API GATEWAY / SECURITY LAYER               │
│         CORS Config | Auth Filter | Exception Handler  │
└─────────────────────┬──────────────────────────────────┘
                      │
        ┌─────────────┴──────────────┐
        │                            │
        ▼                            ▼
┌──────────────────────┐   ┌──────────────────────┐
│  REST Controllers    │   │  Exception Handlers  │
│  (Auth, Content)     │   │  (Global)            │
└──────────┬───────────┘   └──────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────┐
│              SERVICE LAYER (Business Logic)             │
│                                                          │
│  ┌────────────────┐      ┌─────────────────────────┐   │
│  │ AuthService    │      │ AI Orchestrator Service │   │
│  ├────────────────┤      ├─────────────────────────┤   │
│  │ ✓ Register     │      │ ✓ Multi-model routing   │   │
│  │ ✓ Login        │      │ ✓ Failover handling     │   │
│  │ ✓ Token Mgmt   │      │ ✓ Performance tracking  │   │
│  └────────────────┘      └──────┬──────────────────┘   │
│                                 │                       │
│                    ┌────────────┴────────────┐          │
│                    ▼                         ▼          │
│            ┌──────────────────┐    ┌──────────────┐    │
│            │ AI Router Service │    │ Retry Service│    │
│            ├──────────────────┤    ├──────────────┤    │
│            │ ✓ Model selection │    │ ✓ Exp backoff   │    │
│            │ ✓ Health tracking │    │ ✓ 3x retries    │    │
│            └──────────────────┘    └──────────────┘    │
│                                                          │
└──────────────────────┬─────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
┌──────────────────────┐   ┌──────────────────────────┐
│   AI Services        │   │  Repository Layer (JPA) │
│                      │   │                          │
│ ┌────────────────┐   │   │ ┌────────────────────┐   │
│ │ Phi Service    │   │   │ │ UserRepository     │   │
│ │ (Audience)     │   │   │ └────────────────────┘   │
│ └────────────────┘   │   │ ┌────────────────────┐   │
│ ┌────────────────┐   │   │ │ ContentRepository  │   │
│ │ Llama Service  │   │   │ └────────────────────┘   │
│ │ (Education)    │   │   │ ┌────────────────────┐   │
│ └────────────────┘   │   │ │ AIUsageLogRepo     │   │
│ ┌────────────────┐   │   │ └────────────────────┘   │
│ │ Mistral Service│   │   │ ┌────────────────────┐   │
│ │ (Entertainment)│   │   │ │ ModelPerformance   │   │
│ └────────────────┘   │   │ └────────────────────┘   │
└──────────┬───────────┘   └──────────────────────────┘
           │
           │ Hugging Face API Calls
           │ (3 calls max per request)
           ▼
┌──────────────────────────────────────────────────────────┐
│        External AI Services (Hugging Face)               │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Phi Model    │  │ Llama Model  │  │ Mistral Model│  │
│  │ (3.5B)       │  │ (8B)         │  │ (7B)         │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└──────────────────────────────────────────────────────────┘
           │
           │ Persists usage data
           ▼
┌──────────────────────────────────────────────────────────┐
│              PostgreSQL Database                         │
│                                                          │
│  Tables:                                                 │
│  ├── users (authentication & profiles)                   │
│  ├── content_requests (generation requests)              │
│  ├── generated_content (AI outputs)                       │
│  ├── ai_usage_logs (API call tracking)                    │
│  └── model_performance_logs (model metrics)              │
└──────────────────────────────────────────────────────────┘
```

---

## 1. Frontend Architecture

### Technology Stack
- **Framework**: Angular 17 (Standalone Components)
- **Styling**: TailwindCSS + Custom CSS
- **State Management**: RxJS Observables
- **HTTP Client**: Angular HttpClient with Interceptors
- **Routing**: Standalone routes with Guards

### Key Components

```
AppComponent (Root)
├── Navigation (Shared)
├── Routes:
│   ├── /login → LoginComponent
│   ├── /register → RegisterComponent
│   ├── /dashboard → DashboardComponent
│   ├── /generator → GeneratorComponent (Protected)
│   ├── /history → HistoryComponent (Protected)
│   ├── /analytics → AnalyticsComponent (Protected)
│   └── /profile → ProfileComponent (Protected)
```

### Authentication Flow

```
1. User enters credentials
   │
2. LoginComponent calls AuthService.login()
   │
3. AuthService calls ApiService.login()
   │
4. Backend validates & returns JWT + User data
   │
5. AuthService stores token in localStorage
   │
6. Token automatically added to headers via authInterceptor
   │
7. User redirected to /dashboard
```

### HTTP Interceptor

```typescript
authInterceptor adds Bearer token to all requests:
Authorization: Bearer <JWT_TOKEN>

Also handles errors globally via errorInterceptor
```

---

## 2. Backend Architecture

### Layer 1: REST Controller Layer
- **Purpose**: HTTP endpoint handling
- **Classes**: AuthController, ContentController, HealthController
- **Responsibility**: Request validation, routing, response formatting

### Layer 2: Service Layer (Business Logic)
- **Purpose**: Core business logic orchestration
- **Classes**:
  - **AuthService**: Registration, login, password management
  - **AIOrchestratorService**: Main AI workflow coordinator
  - **AIRouterService**: Model selection and health tracking

### Layer 3: AI Service Layer (Specialized)
- **Purpose**: Individual AI model integration
- **Classes**:
  - **PhiService**: Audience analysis
  - **LlamaService**: Educational script generation
  - **MistralService**: Entertainment scripts + growth strategy

### Layer 4: Repository Layer (Data Access)
- **Purpose**: Database interaction via Spring Data JPA
- **Classes**: UserRepository, ContentRequestRepository, etc.
- **Pattern**: Repository Pattern with JpaRepository

### Layer 5: Entity/DTO Layer
- **Entity**: JPA-mapped database objects
- **DTO**: Data transfer objects for API requests/responses
- **Purpose**: Separation of internal and external representations

---

## 3. AI Orchestration Workflow

### Step 1: Audience Analysis (Phi Model)

```
Input:
├── niche
├── vibe
├── topic
├── targetAudience
└── platform

Process:
1. PromptSanitizer cleans inputs
2. Build structured prompt
3. Call Phi via Hugging Face API
4. Parse JSON response
5. Validate structure

Output: AudienceAnalysisDTO
├── audienceType
├── viralPotential (0-10 score)
├── confidenceScore (0-100%)
├── recommendedTone
├── contentStyle
├── engagementTriggers[]
├── hashtags[]
├── trendAlignment
├── viralHooks[]
└── recommendedCTA

Timeout: 30 seconds
Retries: 3 with exponential backoff
```

### Step 2: Content Type Routing (IF/ELSE)

```
IF contentType == "educational"
    ├─ Use: Llama Model (8B)
    └─ Reason: Better for long-form education
    
ELSE
    ├─ Use: Mistral Model (7B)
    └─ Reason: Better for viral entertainment
```

### Step 2A: Educational Script Generation (Llama)

```
Input: topic, audience, platform, vibe, goal

Prompt Template:
Create a viral educational short-form social media script:
[Hook - grab attention]
[Main Content - education]
[Retention - keep watching]
[CTA - call to action]
[Carousel Idea]
[Storytelling Flow]

Output: Educational script with structure
Max Length: 1024 tokens
Temperature: 0.7 (balanced creativity)
```

### Step 2B: Entertainment Script Generation (Mistral)

```
Input: topic, audience, platform, vibe, goal

Prompt Template:
Create a VIRAL entertainment short-form script:
[HOOK - emotional grab]
[ENGAGEMENT BAIT]
[HUMOR/ENTERTAINMENT]
[RETENTION - twist]
[EMOTIONAL PAYOFF]
[CTA]

Includes: Meme phrases, TikTok trends, storytelling

Output: Viral entertainment script
Max Length: 1024 tokens
Temperature: 0.7
```

### Step 3: Growth Strategy (Mistral)

```
Input: niche, platform, topic, contentType

Generates:
1. Thumbnail & Caption Hooks
2. SEO Hashtags (15x)
3. Posting Strategy (time + frequency)
4. Audience Engagement Tactics
5. Growth Hacks (audio, collabs, series)
6. Analytics KPIs

Output: Comprehensive growth strategy
```

### Failover Strategy

```
If Llama fails:
├─ Log failure
├─ Update model health (is_healthy = false)
├─ Switch to Mistral
└─ Tag as "fallback used"

If primary AND fallback fail:
├─ Throw AIServiceException
├─ Rollback content request
├─ Return error to frontend
└─ Alert engineering
```

---

## 4. Database Schema

### Core Tables

```sql
users
├── id (PK)
├── email (UNIQUE, INDEXED)
├── username (UNIQUE)
├── passwordHash
├── apiUsageCount (tracks monthly calls)
├── maxMonthlyApiCalls (limit)
└── created_at, updated_at

content_requests
├── id (PK)
├── user_id (FK, INDEXED)
├── niche
├── platform
├── topicIdea
├── contentType
├── status (PENDING, PROCESSING, COMPLETED, FAILED)
└── created_at (INDEXED)

generated_content
├── id (PK)
├── content_request_id (FK)
├── user_id (FK, INDEXED)
├── scriptContent (TEXT)
├── audienceAnalysis (JSONB)
├── hashtags (TEXT[])
├── viralScore (DECIMAL 0-10)
├── confidenceScore (DECIMAL 0-100)
├── primaryModelUsed
├── fallbackModelUsed
└── generationLatencyMs

ai_usage_logs
├── id (PK)
├── user_id (FK, INDEXED)
├── modelName (INDEXED)
├── inputTokens, outputTokens, totalTokens
├── latencyMs
├── status (SUCCESS, FAILED)
├── retryCount
├── fallbackUsed
└── created_at (INDEXED)

model_performance_logs
├── id (PK)
├── modelName (UNIQUE)
├── requestCount
├── successCount, failureCount
├── averageLatencyMs
├── reliabilityScore (0-1)
├── isHealthy (INDEXED)
└── lastUsed
```

### Indexes

```sql
-- Performance indexes
CREATE INDEX idx_user_id ON content_requests(user_id);
CREATE INDEX idx_created_at ON generated_content(created_at DESC);
CREATE INDEX idx_user_platform ON content_requests(user_id, platform);
CREATE INDEX idx_model_health ON model_performance_logs(is_healthy);
```

---

## 5. Security Architecture

### Authentication Flow

```
User Login
    │
    ▼
[Spring Security Filter Chain]
    │
    ├─ CorsConfig: Validate origin
    ├─ CSRF Protection: Disable (stateless API)
    ├─ SessionCreationPolicy: STATELESS
    │
    ▼
[AuthController.login()]
    │
    ▼
[AuthService.login()]
    │
    ├─ AuthenticationManager.authenticate()
    │   └─ DaoAuthenticationProvider
    │       └─ UserDetailsServiceImpl.loadUserByUsername()
    │           └─ BCrypt.matches(password, hash)
    │
    ▼
[JwtTokenProvider.generateToken()]
    │
    ├─ Sign with HMAC-SHA512
    ├─ Expiration: 24 hours
    └─ Subject: user.email
    │
    ▼
Return: JWT Token + User Data

Protected Request Flow:
    │
    ├─ Client adds: Authorization: Bearer <TOKEN>
    │
    ▼
[JwtAuthenticationFilter]
    │
    ├─ Extract token from header
    ├─ Validate signature
    ├─ Check expiration
    ├─ Load UserDetails
    │
    ▼
[SecurityContext.setAuthentication()]
    │
    ▼
[Controller] - User authenticated
```

### Input Validation & Sanitization

```
PromptSanitizer.sanitize():
├─ Remove dangerous SQL patterns (DROP, DELETE, UNION)
├─ Remove XSS patterns (<script>, onclick=)
├─ Enforce max length (500 chars)
├─ Normalize whitespace
└─ Validate pattern

DTOs:
├─ @NotBlank, @Size, @Email annotations
├─ Bean Validation (JSR-303)
└─ Global exception handler for violations
```

### Password Security

```
Registration:
├─ Require: Uppercase + lowercase + numbers + special char
├─ Minimum 8 characters
├─ BCrypt hash with salt
└─ Never store plaintext

Login:
├─ Find by email
├─ Use BCrypt.matches() for comparison
├─ Never compare plaintext
└─ Log attempt
```

---

## 6. Performance Optimization

### Caching Strategy

```
Frontend:
├─ LocalStorage: JWT token, user profile
├─ RxJS: Subject-based observable cache
└─ HTTP: Angular HttpClient caching headers

Backend:
├─ Database: Indexes on frequent queries
├─ Connection Pool: HikariCP
├─ Batch inserts for logs
└─ Lazy loading for related entities
```

### Database Optimization

```
Query Patterns:
├─ User by email: O(1) via UNIQUE index
├─ Content by user: O(log n) via user_id + created_at
├─ Recent logs: O(log n) via created_at DESC
└─ Model health: O(log n) via is_healthy

Pagination:
├─ Content history: 20 items per page
├─ Usage logs: 50 items per page
└─ Prevents N+1 problems
```

### API Response Times

```
Target Latency:
├─ Audience Analysis (Phi): < 5s
├─ Script Generation (Llama/Mistral): < 10s
├─ Growth Strategy: < 10s
└─ Total orchestration: < 30s

Monitored in:
├─ generationLatencyMs (GeneratedContent table)
├─ latencyMs (AIUsageLog table)
└─ averageLatencyMs (ModelPerformanceLog table)
```

---

## 7. Error Handling & Recovery

### Global Exception Handler

```
@RestControllerAdvice
├─ AIServiceException → 503 Service Unavailable
├─ UnauthorizedException → 401 Unauthorized
├─ ValidationException → 400 Bad Request
├─ MethodArgumentNotValidException → 400 Bad Request
├─ BadCredentialsException → 401 Unauthorized
└─ Exception → 500 Internal Server Error

All return: ApiResponseDTO with error details
```

### Retry Strategy

```
RetryService.executeWithRetry():
├─ Max attempts: 3
├─ Initial delay: 1000ms
├─ Backoff formula: delay * 2^(attempt-1)
│   ├─ Attempt 1: 1000ms
│   ├─ Attempt 2: 2000ms
│   └─ Attempt 3: 4000ms
└─ Total max wait: 7000ms

Used for:
├─ AI API calls
├─ Database operations (transient failures)
└─ External service calls
```

---

## 8. Logging & Monitoring

### Logging Configuration (Logback)

```
Levels:
├─ DEBUG: Service method calls, AI decisions
├─ INFO: User actions, content generation
├─ WARN: Retries, model failures, validation
└─ ERROR: Exceptions, failures

Logged Events:
├─ User registration/login
├─ Content generation start/end
├─ Model selection decisions
├─ API call latency
├─ Failures and retries
└─ Model health changes
```

### Metrics Collection

```
ModelPerformanceLog tracks:
├─ Request count per model
├─ Success/failure rate
├─ Average latency
├─ Average tokens used
├─ Reliability score
└─ Health status

AIUsageLog tracks:
├─ User API calls
├─ Tokens consumed
├─ Latency per call
├─ Cost estimates
└─ Retry counts
```

---

## 9. Deployment Architecture (Ready for Production)

### Configuration Management

```
application.yml profiles:
├─ dev: Local development (H2, debug logging)
├─ prod: Production (PostgreSQL, info logging)
└─ test: Testing (in-memory database)

Environment Variables:
├─ SPRING_PROFILES_ACTIVE
├─ DB_URL, DB_USERNAME, DB_PASSWORD
├─ HUGGINGFACE_API_KEY
├─ JWT_SECRET
└─ SERVER_PORT
```

### Health Checks

```
GET /api/health
├─ Returns 200 OK if service is running
├─ Database connectivity check ready
└─ Hugging Face API status check ready
```

---

## Technology Decisions

| Component | Choice | Reason |
|-----------|--------|--------|
| Language (Backend) | Java 21 | Modern features, performance, JVM ecosystem |
| Framework | Spring Boot 3 | Enterprise-grade, security, maturity |
| Database | PostgreSQL | Robust, JSONB support, indexes, reliability |
| ORM | Spring Data JPA | Abstraction, query methods, transaction management |
| Authentication | JWT | Stateless, scalable, no session storage |
| Frontend Framework | Angular 17 | Type-safe, component-based, standalone |
| Styling | TailwindCSS | Utility-first, rapid development, customizable |
| AI Integration | Hugging Face | Free tier, multiple models, excellent documentation |
| Reactive | RxJS | Async handling, observables, Angular native |

---

## Scalability Considerations

```
Current Design (Single Server):
├─ Suitable for: < 1000 concurrent users
├─ Database: Single PostgreSQL instance
├─ API: Single Spring Boot instance
└─ Frontend: Single static server

Future Scaling:
├─ Load balancer (Nginx/HAProxy)
├─ Database replication
├─ Connection pooling
├─ Cache layer (Redis)
├─ API gateway (Kong/AWS API Gateway)
└─ Container orchestration (Kubernetes)
```

---

## Code Quality Standards

```
Adherence To:
├─ Clean Architecture principles
├─ SOLID principles
├─ DRY (Don't Repeat Yourself)
├─ KISS (Keep It Simple, Stupid)
└─ Logging best practices

Testing:
├─ Unit tests for services
├─ Integration tests for controllers
├─ Angular component tests
└─ API integration tests
```

---

*Last Updated: 2025 | Version: 1.0.0*
