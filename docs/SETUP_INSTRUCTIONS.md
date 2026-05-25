# ViralForge AI - Complete Setup Guide

## Prerequisites

Before starting, ensure you have installed:

```bash
# Check versions
java -version          # Should be 21+
node -v               # Should be 18+
npm -v                # Should be 9+
psql --version        # Should be 12+
```

### Download & Install

- **Java 21**: https://www.oracle.com/java/technologies/downloads/
- **Node.js 18+**: https://nodejs.org/
- **PostgreSQL 12+**: https://www.postgresql.org/download/
- **Git**: https://git-scm.com/ (optional but recommended)

---

## 1. Database Setup

### Windows (PowerShell)

```powershell
# Start PostgreSQL service
Get-Service postgres | Start-Service

# Connect to PostgreSQL
psql -U postgres

# In PostgreSQL CLI:
CREATE DATABASE viralforge_ai;
\c viralforge_ai
\i 'C:/path/to/database/schema.sql'
\q
```

### Mac/Linux

```bash
# Start PostgreSQL (Homebrew)
brew services start postgresql

# Connect and create database
psql postgres

# In PostgreSQL CLI:
CREATE DATABASE viralforge_ai;
\c viralforge_ai
\i /path/to/database/schema.sql
\q
```

### Verify Database

```bash
# Check tables created
psql -U postgres -d viralforge_ai -c "\dt"

# Output should show:
# Schema |           Name            | Type  | Owner
#--------+---------------------------+-------+----------
# public | ai_usage_logs             | table | postgres
# public | content_requests          | table | postgres
# public | generated_content         | table | postgres
# public | model_performance_logs    | table | postgres
# public | users                     | table | postgres
```

---

## 2. Hugging Face API Setup

1. **Create Account**: https://huggingface.co/
2. **Generate Token**:
   - Go to: https://huggingface.co/settings/tokens
   - Click "New token"
   - Select "Read" access
   - Copy token (keep secret!)
3. **Save for later** (needed in .env)

---

## 3. Backend Setup

### Step 1: Create .env File

In project root:

```bash
cd /path/to/Influencer Reels/backend
cp ../.env.example .env
```

Edit `.env`:

```ini
# Database
DB_URL=jdbc:postgresql://localhost:5432/viralforge_ai
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

# JWT (Generate a strong secret key)
JWT_SECRET=YourSuperSecureSecretKeyWithAtLeast256BitsForHS256AlgorithmHere1234567890
JWT_EXPIRATION=86400000

# Hugging Face API (from previous step)
HUGGINGFACE_API_KEY=hf_YOUR_TOKEN_HERE

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### Step 2: Build Backend

```bash
cd backend

# Clean and build
mvn clean install

# This will:
# - Download dependencies (takes 2-3 minutes)
# - Compile Java code
# - Run tests
# - Package JAR file
```

### Step 3: Run Backend

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Direct JAR (after build)
java -jar target/viralforge-ai-1.0.0.jar

# Check if running:
# Visit http://localhost:8080/api/health
# Should return: {"status": "UP", "service": "ViralForge AI"}
```

**Keep terminal open!** Backend continues running.

---

## 4. Frontend Setup

### Step 1: Install Dependencies

```bash
cd frontend

# Install npm packages (takes 1-2 minutes)
npm install

# Should complete without errors
```

### Step 2: Check Configuration

Verify in `frontend/src/app/core/services/api.service.ts`:

```typescript
private apiUrl = 'http://localhost:8080/api';  // Should match backend
```

### Step 3: Run Frontend

```bash
# Start Angular dev server
npm start

# This will:
# - Compile Angular code
# - Start local server
# - Open http://localhost:4200 in browser
# - Auto-reload on code changes
```

**Keep terminal open!** Frontend continues running.

---

## 5. Verify Everything Works

### Test API Endpoints

Open in browser or Postman:

```
1. Health Check
   GET http://localhost:8080/api/health
   
2. Register (in Postman)
   POST http://localhost:8080/api/auth/register
   Body (JSON):
   {
     "username": "testcreator",
     "email": "test@example.com",
     "password": "SecurePass123!",
     "fullName": "Test Creator"
   }
   
3. Login
   POST http://localhost:8080/api/auth/login
   Body (JSON):
   {
     "email": "test@example.com",
     "password": "SecurePass123!"
   }
   
   Save the token from response!
   
4. Generate Content
   POST http://localhost:8080/api/content/generate
   Header: Authorization: Bearer <TOKEN_FROM_LOGIN>
   Body (JSON):
   {
     "niche": "Tech",
     "targetAudience": "Developers",
     "platform": "TikTok",
     "topicIdea": "Quick Python tips",
     "contentType": "educational"
   }
```

### Test Frontend

1. Open http://localhost:4200
2. Click "Sign up"
3. Create test account
4. Navigate to "Create" (generator page)
5. Fill form and click "Generate Content"
6. Wait 10-30 seconds
7. See AI-generated results!

---

## 6. Troubleshooting

### Backend Issues

**Error: Cannot connect to database**
```
Solution:
1. Check PostgreSQL is running: sudo systemctl status postgresql
2. Verify credentials in .env
3. Verify database exists: psql -l | grep viralforge_ai
4. Check connection string: jdbc:postgresql://localhost:5432/viralforge_ai
```

**Error: HUGGINGFACE_API_KEY not set**
```
Solution:
1. Verify .env file exists in backend/
2. Check API key in .env
3. Restart Maven: mvn spring-boot:run
4. Or set env var: export HUGGINGFACE_API_KEY=hf_xxx
```

**Error: Port 8080 already in use**
```
Solution:
1. Kill existing process: lsof -i :8080 | kill -9 <PID>
2. Or change port in application.yml: server.port=8081
```

### Frontend Issues

**Error: Cannot GET http://localhost:4200**
```
Solution:
1. Check npm install completed: npm list
2. Restart: Ctrl+C then npm start
3. Clear cache: rm -rf node_modules package-lock.json && npm install
```

**Error: API requests fail**
```
Solution:
1. Check backend is running: http://localhost:8080/api/health
2. Verify API URL in api.service.ts
3. Check CORS headers in browser console
```

**Error: Login redirects to blank page**
```
Solution:
1. Open browser console (F12)
2. Check for errors
3. Verify localStorage: localStorage.getItem('viralforge_token')
4. Check JWT token in login response
```

### Database Issues

**Error: relation "users" does not exist**
```
Solution:
1. Check schema.sql ran: psql -d viralforge_ai -c "\dt"
2. Manually run schema: psql -d viralforge_ai -f database/schema.sql
3. Verify no errors in schema execution
```

**Error: Connection refused**
```
Solution:
1. Start PostgreSQL: brew services start postgresql
2. Check psql works: psql postgres
3. Check credentials work: psql -U postgres -d viralforge_ai
```

---

## 7. Development Workflow

### Edit Backend Code

```bash
# Terminal 1: Backend (auto-reloads)
cd backend
mvn spring-boot:run

# Make changes to src/main/java/com/viralforge/**
# Saves trigger auto-compilation and restart
```

### Edit Frontend Code

```bash
# Terminal 2: Frontend (auto-reloads)
cd frontend
npm start

# Make changes to src/app/**
# Saves trigger auto-compilation in browser
```

### View Logs

**Backend logs**: Terminal where you ran `mvn spring-boot:run`
**Frontend logs**: Terminal where you ran `npm start`
**Database logs**: Varies by OS (check PostgreSQL logs directory)

---

## 8. Run Tests

### Backend Tests

```bash
cd backend

# Run all tests
mvn test

# Run specific test
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn test jacoco:report
```

### Frontend Tests

```bash
cd frontend

# Run tests
npm test

# Run tests with coverage
npm test -- --code-coverage

# Run e2e tests (if configured)
npm run e2e
```

---

## 9. Build for Production

### Backend

```bash
cd backend

# Clean build
mvn clean package

# JAR file created: target/viralforge-ai-1.0.0.jar

# Run:
java -jar target/viralforge-ai-1.0.0.jar
```

### Frontend

```bash
cd frontend

# Production build
npm run build:prod

# Output: dist/ folder (upload to server)

# Or serve locally:
npx http-server dist/ -p 3000
```

---

## 10. Environment Files Reference

### Backend: application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/viralforge_ai
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
      
jwt:
  secret: your-secret-key
  expiration: 86400000
  
huggingface:
  api:
    key: hf_YOUR_KEY
    url: https://api-inference.huggingface.co/models/

server:
  port: 8080
```

### Frontend: environment.ts

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## 11. Common Commands Cheatsheet

```bash
# Backend
mvn clean install          # Full rebuild
mvn spring-boot:run        # Run dev server
mvn test                   # Run tests
mvn dependency:tree        # View dependencies
mvn compile                # Compile only

# Frontend
npm install                # Install dependencies
npm start                  # Run dev server
npm run build              # Production build
npm test                   # Run tests
npm run lint               # Lint code
npm audit fix              # Fix vulnerabilities

# Database
psql postgres              # Connect to PostgreSQL
\dt                        # List tables
\d users                   # Describe table
psql -d viralforge_ai -f schema.sql  # Run SQL file

# Git (optional)
git clone <repo>           # Clone repository
git status                 # Check status
git add .                  # Stage changes
git commit -m "message"    # Commit
git push                   # Push to remote
```

---

## 12. First Time Checklist

- [ ] Java 21 installed
- [ ] Node.js 18+ installed
- [ ] PostgreSQL installed and running
- [ ] Created database `viralforge_ai`
- [ ] Ran `database/schema.sql`
- [ ] Created `.env` file with settings
- [ ] Got Hugging Face API key
- [ ] Backend builds without errors (`mvn clean install`)
- [ ] Backend runs (`mvn spring-boot:run`)
- [ ] Frontend installs dependencies (`npm install`)
- [ ] Frontend runs (`npm start`)
- [ ] Can access http://localhost:4200
- [ ] Can register new user
- [ ] Can login with user
- [ ] Can generate content (wait for AI)

---

## 13. Performance Tips

- **Backend**: Initial startup takes 10-15 seconds
- **Frontend**: First load takes 3-5 seconds
- **Content Generation**: 15-30 seconds (depends on Hugging Face API)
- **Database**: Should return queries < 100ms with indexes

If slow, check:
1. Internet connection (API calls)
2. CPU usage (compilation)
3. RAM usage (> 2GB recommended)
4. Disk space (> 5GB free)

---

## 14. Next Steps

1. ✅ Complete setup (steps 1-5)
2. ✅ Test everything (step 5)
3. 📖 Read ARCHITECTURE.md for system design
4. 🔐 Read SECURITY_REVIEW.md for security
5. 📚 Read API_DOCUMENTATION.md for endpoints
6. 🚀 Deploy to production (when ready)

---

## Support

**Issues?**
1. Check troubleshooting section above
2. Check console logs (F12 in browser)
3. Check terminal where backend/frontend runs
4. Check PostgreSQL logs
5. Re-read error message carefully

**Still stuck?**
- Check all environment variables
- Verify all services running (backend, frontend, database)
- Try complete restart (close all terminals, restart services)
- Check firewall isn't blocking ports 8080, 4200, 5432

---

**Last Updated**: 2025 | **Version**: 1.0.0

Good luck! 🚀 Enjoy building with ViralForge AI!
