# ViralForge AI - API Documentation

## Base URL
```
Development: http://localhost:8080/api
Production: https://api.viralforge.ai/api
```

## Authentication
All protected endpoints require JWT token in header:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## Authentication Endpoints

### 1. Register User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "creator_name",
  "email": "creator@example.com",
  "password": "SecurePass123!",
  "fullName": "Creator Name",
  "niche": "Tech",
  "targetAudience": "Gen Z",
  "preferredPlatform": "TikTok"
}

Response (201):
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGc...",
    "type": "Bearer",
    "userId": 1,
    "username": "creator_name",
    "email": "creator@example.com",
    "fullName": "Creator Name",
    "apiUsageCount": 0,
    "maxMonthlyApiCalls": 1000
  }
}
```

### 2. Login User
```http
POST /auth/login
Content-Type: application/json

{
  "email": "creator@example.com",
  "password": "SecurePass123!"
}

Response (200):
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGc...",
    "type": "Bearer",
    "userId": 1,
    "username": "creator_name",
    "email": "creator@example.com",
    "fullName": "Creator Name",
    "apiUsageCount": 150,
    "maxMonthlyApiCalls": 1000
  }
}
```

---

## Content Generation Endpoints

### 3. Generate Viral Content
```http
POST /content/generate
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "niche": "Tech Tutorials",
  "targetAudience": "Software Developers",
  "vibe": "funny",
  "platform": "TikTok",
  "topicIdea": "Quick Python tips for beginners",
  "contentType": "educational",
  "creatorGoal": "Build audience and establish expertise"
}

Response (200):
{
  "success": true,
  "message": "Content generated successfully",
  "data": {
    "contentId": 42,
    "contentRequestId": 41,
    "scriptContent": "HOOK: Want to learn Python in 10 seconds? ...",
    "hashtags": ["#python", "#coding", "#programming"],
    "thumbnailText": "5 Python Tricks You Didn't Know",
    "captions": "These 5 Python tricks will blow your mind...",
    "seoHashtags": ["#softwaredev", "#webdevelopment"],
    "postingSchedule": "Tuesday & Thursday at 6 PM EST",
    "engagementStrategy": "Reply to every comment in first hour",
    "growthTips": "Post similar content in series",
    "bestPostingTime": "6:00 PM EST",
    "platformOptimization": "Use trending sounds and transitions",
    "viralScore": 8.5,
    "confidenceScore": 92.0,
    "primaryModelUsed": "meta-llama/Meta-Llama-3-8B-Instruct",
    "fallbackModelUsed": null,
    "generationLatencyMs": 8234,
    "recommendedTone": "Conversational and engaging",
    "contentStyle": "Quick tips with visuals",
    "trendAlignment": "Aligns with current Python tutorial trends",
    "recommendedCTA": "Follow for more Python tips!",
    "generatedAt": "2025-05-25T14:30:00"
  }
}
```

**Error Responses:**

```http
400 Bad Request:
{
  "success": false,
  "message": "Invalid input parameters",
  "error": "VALIDATION_ERROR",
  "statusCode": 400
}

503 Service Unavailable:
{
  "success": false,
  "message": "AI service temporarily unavailable",
  "error": "AI_SERVICE_ERROR",
  "statusCode": 503
}

401 Unauthorized:
{
  "success": false,
  "message": "Unauthorized access",
  "error": "UNAUTHORIZED",
  "statusCode": 401
}
```

### 4. Get Content History
```http
GET /content/history
Authorization: Bearer <JWT_TOKEN>

Query Parameters:
- page: 0 (default)
- size: 20 (default)
- sortBy: created_at (default)

Response (200):
{
  "success": true,
  "message": "Content history retrieved",
  "data": [
    {
      "id": 42,
      "createdAt": "2025-05-25T14:30:00",
      "scriptContent": "...",
      "viralScore": 8.5,
      "confidenceScore": 92.0,
      "primaryModelUsed": "meta-llama/Meta-Llama-3-8B-Instruct",
      "isFavorited": false,
      "isPublished": false
    }
  ]
}
```

---

## Analytics Endpoints

### 5. Get Analytics Dashboard
```http
GET /analytics/dashboard
Authorization: Bearer <JWT_TOKEN>

Response (200):
{
  "success": true,
  "message": "Analytics retrieved",
  "data": {
    "totalContentGenerated": 42,
    "averageViralScore": 7.8,
    "averageConfidenceScore": 88.5,
    "totalApiCalls": 200,
    "favoriteContentCount": 8,
    "publishedContentCount": 15,
    "mostUsedModel": "meta-llama/Meta-Llama-3-8B-Instruct",
    "mostSuccessfulPlatform": "TikTok",
    "monthlyUsagePercentage": 20.0
  }
}
```

---

## Health Check Endpoints

### 6. Service Health
```http
GET /health

Response (200):
{
  "success": true,
  "message": "Service is healthy",
  "data": {
    "status": "UP",
    "service": "ViralForge AI",
    "version": "1.0.0"
  }
}
```

---

## Error Codes

| Code | Status | Meaning |
|------|--------|---------|
| 200 | OK | Request successful |
| 201 | Created | Resource created |
| 400 | Bad Request | Invalid input |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | Not authorized for resource |
| 404 | Not Found | Resource not found |
| 503 | Service Unavailable | AI service down |
| 500 | Internal Server Error | Server error |

---

## Response Format

All responses follow this structure:

```json
{
  "success": true/false,
  "message": "Human-readable message",
  "data": { /* Response data */ },
  "error": "ERROR_CODE",
  "statusCode": 200,
  "timestamp": "2025-05-25T14:30:00"
}
```

---

## Rate Limiting

Currently: 1000 requests per month per user

Future: 100 requests per minute (configurable)

---

## Request Validation

### Required Fields for Content Generation

| Field | Type | Rules |
|-------|------|-------|
| niche | string | 2-100 chars |
| targetAudience | string | 2-100 chars |
| platform | enum | Instagram Reels, TikTok, YouTube Shorts |
| topicIdea | string | 5-1000 chars |
| contentType | enum | educational, entertainment |
| vibe | string | Optional, 0-100 chars |
| creatorGoal | string | Optional, 0-500 chars |

### Example Request Validation

```json
{
  "niche": "", // Error: Required
  "targetAudience": "a", // Error: Min 2 chars
  "platform": "Invalid", // Error: Not in enum
  "topicIdea": "short", // Error: Min 5 chars
  "contentType": "music" // Error: Not in enum
}
```

---

## Token Expiration

JWT tokens expire after 24 hours.

To refresh: Re-login to get new token.

---

## Pagination

List endpoints support pagination:

```http
GET /content/history?page=0&size=20

Response includes pagination metadata:
{
  "data": [ /* items */ ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

---

## Example cURL Requests

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mycreator",
    "email": "creator@example.com",
    "password": "SecurePass123!",
    "fullName": "My Creator"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "creator@example.com",
    "password": "SecurePass123!"
  }'
```

### Generate Content (with token)
```bash
curl -X POST http://localhost:8080/api/content/generate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "niche": "Tech",
    "targetAudience": "Developers",
    "platform": "TikTok",
    "topicIdea": "Python tips",
    "contentType": "educational"
  }'
```

---

**Last Updated**: 2025 | **Version**: 1.0.0
