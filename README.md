# 🚀 ViralForge AI - Viral Short-Form Content Generator

An advanced AI-powered platform for generating viral short-form content scripts for TikTok, Instagram Reels, and YouTube Shorts with **multi-language support (10 Indian languages)** and real-time trending analytics. Built with Angular 17, Spring Boot 3, PostgreSQL, and Hugging Face AI models.

## 🚀 Quick Start

### Prerequisites
- **Java 21+**
- **Node.js 18+**
- **PostgreSQL 12+**
- **Hugging Face API Key** (Free tier available)

### Backend Setup

```bash
# 1. Navigate to backend directory
cd backend

# 2. Create .env file in project root
cp ../.env.example .env

# 3. Update .env with your values
# - DB_USERNAME, DB_PASSWORD
# - HUGGINGFACE_API_KEY (get from https://huggingface.co/settings/tokens)
# - JWT_SECRET (generate a secure key)

# 4. Build and run
mvn clean install
mvn spring-boot:run

# Backend runs on http://localhost:8080
```

### Database Setup

```bash
# 1. Create PostgreSQL database
createdb viralforge_ai

# 2. Run schema
psql viralforge_ai < database/schema.sql

# 3. Run seed data (optional)
psql viralforge_ai < database/seed-data.sql
```

### Frontend Setup

```bash
# 1. Navigate to frontend directory
cd frontend

# 2. Install dependencies
npm install

# 3. Start development server
npm start

# Frontend runs on http://localhost:4200
```

## 🏗️ Project Structure

```
ViralForgeAI/
├── backend/                  # Spring Boot application
│   ├── src/main/java/com/viralforge/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST API endpoints
│   │   ├── service/         # Business logic
│   │   │   └── ai/         # AI services (Phi, Llama, Mistral)
│   │   ├── entity/         # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # JWT and auth
│   │   └── exception/      # Exception handling
│   └── pom.xml
│
├── frontend/                # Angular 17 application
│   ├── src/app/
│   │   ├── core/           # Auth, services, guards
│   │   ├── features/       # Feature modules
│   │   │   ├── auth/      # Login/Register
│   │   │   ├── dashboard/ # Main dashboard
│   │   │   ├── generator/ # Content generator
│   │   │   ├── analytics/ # Analytics dashboard
│   │   │   ├── history/   # Content history
│   │   │   └── profile/   # User profile
│   │   ├── shared/        # Shared components
│   │   └── styles/        # Global styles
│   └── package.json
│
├── database/               # Database scripts
│   ├── schema.sql         # Database schema
│   └── seed-data.sql      # Sample data
│
└── docs/                  # Documentation
```

## 🤖 AI Models

### Model Selection Strategy

**LLM Call 1 - Audience Analyzer (Phi Model)**
- Analyzes creator niche, audience behavior, viral potential
- Returns structured JSON with audience insights
- Input: niche, vibe, topic, audience, platform
- Output: AudienceAnalysisDTO with viral metrics

**IF/ELSE Content Type Routing**

**For Educational Content → Llama Model**
- Generates long-form educational scripts
- Focuses on retention and learning value
- Best for explanatory content

**For Entertainment Content → Mistral Model**
- Generates viral meme-style scripts
- Focuses on engagement and emotional hooks
- Best for trending/viral content

**LLM Call 3 - Growth Strategist (Mistral Model)**
- Generates creator growth strategy
- Returns: hashtags, posting schedule, engagement tactics
- Optimization for specific platform

### Model API Calls - Max 3 Calls

1. **Audience Analysis** (Phi) - Always required
2. **Script Generation** (Llama or Mistral) - Based on content type
3. **Growth Strategy** (Mistral) - Always generated

### Failover Strategy

If a model fails:
- Automatic retry with exponential backoff
- Falls back to alternative model
- Logs failure for performance tracking
- Updates model health status

## 🔑 Key Features

### 1. Multi-Model AI Orchestration
- Intelligent model routing based on content type
- Automatic failover handling
- Model performance tracking

### 2. Content Generation Workflow
- Audience analysis and viral potential scoring
- Conditional script generation based on content type
- Growth strategy and posting optimization

### 3. Security
- JWT authentication with 24hr expiry
- BCrypt password hashing
- Prompt sanitization (SQL/XSS prevention)
- Rate limiting capability
- CORS configuration

### 4. Data Persistence
- PostgreSQL with optimized indexes
- Content request tracking
- AI usage logging
- Model performance metrics
- User analytics snapshots

### 5. Frontend Experience
- Modern Gen Z aesthetic
- Dark mode with neon gradients
- Glassmorphism components
- Real-time generation with loading states
- Responsive design (mobile-friendly)

## 📊 API Endpoints

### Authentication
- `POST /api/auth/register` - Create account
- `POST /api/auth/login` - Login with email/password
- `GET /api/health` - Health check

### Content Generation
- `POST /api/content/generate` - Generate viral content
- `GET /api/content/history` - Get user's content history
- `GET /api/analytics/dashboard` - Get analytics data

## 🔐 Security Considerations

### Implemented Security Measures

1. **Input Validation**
   - Prompt sanitization removes SQL injection patterns
   - XSS prevention in API responses
   - Length limits on all inputs

2. **Authentication**
   - JWT tokens with secure signature
   - Token validation on every protected endpoint
   - Stateless session management

3. **Password Security**
   - BCrypt hashing with salt
   - Strong password requirements (uppercase, lowercase, numbers, special chars)
   - Password validation rules enforced

4. **API Security**
   - CORS restricted to configured origins
   - CSRF protection via token validation
   - Rate limiting ready (configurable)

5. **Data Security**
   - Sensitive fields in database
   - User isolation - only access own content
   - Audit logging for AI API calls

### Potential Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| AI Hallucination | Model is prompted for factual content; user reviews before publishing |
| Prompt Injection | PromptSanitizer removes dangerous patterns |
| API Abuse | Rate limiting and monthly call limits per user |
| Data Breach | Password hashing, secure token storage |
| Model Misuse | Terms of service, content moderation ready |
| Toxic Content | Model selection favors safety; user review recommended |

## 🧪 Testing

### Run Tests
```bash
# Backend unit tests
cd backend
mvn test

# Backend integration tests
mvn test -DintTest

# Frontend unit tests
cd frontend
npm test

# Frontend e2e tests
npm run e2e
```

### Test Coverage
- AuthService tests (login, register, token validation)
- AIOrchestrator tests (model routing, failover)
- Controller integration tests (API endpoints)
- Angular component tests (UI interactions)

## 📈 Performance Metrics

### Latency Targets
- Audience Analysis: < 5 seconds
- Script Generation: < 10 seconds
- Total Orchestration: < 15 seconds

### Model Performance Tracking
- Request count per model
- Success/failure rates
- Average latency per model
- Auto-disable unhealthy models

### Database Optimization
- Indexed queries on user_id, created_at, status
- Pagination for content listing
- Connection pooling configured

## 🚀 Deployment Ready

The application is production-ready with:
- Environment-based configuration
- Structured logging (SLF4J with Logback)
- Exception handling and error responses
- Database migrations via Flyway
- Health check endpoints
- Metrics collection ready

## 📚 Documentation

- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - System design and architecture
- **[SECURITY_REVIEW.md](docs/SECURITY_REVIEW.md)** - Security analysis and mitigations
- **[API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)** - Complete API reference
- **[AI_WORKFLOW.md](docs/AI_WORKFLOW.md)** - AI orchestration details
- **[TESTING_GUIDE.md](docs/TESTING_GUIDE.md)** - Testing procedures

## 🛠️ Development

### Environment Variables
```bash
# Backend
DB_URL=jdbc:postgresql://localhost:5432/viralforge_ai
DB_USERNAME=postgres
DB_PASSWORD=postgres
HUGGINGFACE_API_KEY=hf_xxxxxxxxxxxx
JWT_SECRET=your_secure_secret_key

# Frontend
ANGULAR_API_URL=http://localhost:8080/api
```

### Available Commands

**Backend**
```bash
mvn clean install          # Build project
mvn spring-boot:run        # Run dev server
mvn test                   # Run tests
mvn compile                # Compile only
```

**Frontend**
```bash
npm install                # Install dependencies
npm start                  # Run dev server
npm run build              # Build for production
npm test                   # Run unit tests
npm run lint               # Lint code
```

## 📱 Supported Platforms

- Instagram Reels
- TikTok
- YouTube Shorts

## 🎨 UI/UX Features

- **Dark Mode**: Slate-900 base with purple/pink accents
- **Glassmorphism**: Frosted glass effects with backdrop blur
- **Neon Gradients**: Purple to pink color scheme
- **Responsive Grid**: Mobile to desktop optimization
- **Loading States**: Animated spinners and feedback
- **Toast Notifications**: User feedback system ready

## 📞 Support

For issues or questions:
1. Check [API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)
2. Review [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
3. Check application logs in `logs/` directory

## 📄 License

MIT License - See LICENSE file

---

**Built with ❤️ for creators who want to go viral**

Version: 1.0.0 | Last Updated: 2025
