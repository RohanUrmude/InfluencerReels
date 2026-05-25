import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8081/api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ========== AUTHENTICATION API TESTS ==========

  describe('Authentication', () => {
    it('should register a new user', (done) => {
      // TEST: AU-001 Register with valid data
      const registerData = {
        email: 'test@example.com',
        password: 'ValidPass123!',
        fullName: 'Test User'
      };

      const mockResponse = {
        success: true,
        data: {
          token: 'jwt_token_here',
          email: 'test@example.com',
          fullName: 'Test User'
        }
      };

      service.register(registerData).subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.token).toBeDefined();
        expect(response.data.email).toBe('test@example.com');
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/auth/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerData);
      req.flush(mockResponse);
    });

    it('should login with valid credentials', (done) => {
      // TEST: AU-004 Login with valid credentials
      const loginData = {
        email: 'test@example.com',
        password: 'ValidPass123!'
      };

      const mockResponse = {
        success: true,
        data: {
          token: 'jwt_token_here',
          email: 'test@example.com',
          fullName: 'Test User'
        }
      };

      service.login(loginData).subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.token).toBeDefined();
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/auth/login`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should get current user', (done) => {
      // TEST: AU-008 Get current user
      const mockResponse = {
        success: true,
        data: {
          id: 1,
          email: 'test@example.com',
          fullName: 'Test User',
          apiUsageCount: 5,
          maxMonthlyApiCalls: 1000
        }
      };

      service.getCurrentUser().subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.email).toBe('test@example.com');
        expect(response.data.apiUsageCount).toBe(5);
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/auth/me`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  // ========== CONTENT GENERATION API TESTS ==========

  describe('Content Generation', () => {
    it('should generate content successfully', (done) => {
      // TEST: CG-001 Generate content with valid input
      const generateRequest = {
        niche: 'Fitness',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'Morning workout motivation',
        targetAudience: 'Fitness Enthusiasts',
        vibe: 'inspirational'
      };

      const mockResponse = {
        success: true,
        data: {
          viralScore: 7.5,
          confidenceScore: 85.5,
          scriptContent: 'Generated script...',
          hashtags: ['#FitnessGoals', '#Motivation'],
          recommendedTone: 'Inspirational',
          contentStyle: 'Energetic visual storytelling'
        }
      };

      service.generateContent(generateRequest).subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.viralScore).toBe(7.5);
        expect(response.data.confidenceScore).toBe(85.5);
        expect(response.data.scriptContent).toBeDefined();
        expect(response.data.hashtags.length).toBeGreaterThan(0);
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/content/generate`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should get content history', (done) => {
      // TEST: Get content history
      const mockResponse = {
        success: true,
        data: [
          {
            id: 1,
            topicIdea: 'Morning workout',
            platform: 'TikTok',
            niche: 'Fitness',
            viralScore: 7.5,
            confidenceScore: 85.5,
            scriptContent: 'Generated script...',
            createdAt: new Date()
          },
          {
            id: 2,
            topicIdea: 'Healthy recipes',
            platform: 'Instagram Reels',
            niche: 'Food',
            viralScore: 8.2,
            confidenceScore: 90.0,
            scriptContent: 'Another script...',
            createdAt: new Date()
          }
        ]
      };

      service.getContentHistory().subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.length).toBe(2);
        expect(response.data[0].platform).toBe('TikTok');
        expect(response.data[1].platform).toBe('Instagram Reels');
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/content/history`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  // ========== LANGUAGE ADAPTATION API TESTS ==========

  describe('Language Adaptation', () => {
    it('should adapt content to languages', (done) => {
      // TEST: LA-001 Adapt to single language
      const adaptRequest = {
        scriptContent: 'Wake up at 5 AM...',
        topicIdea: 'Morning workout',
        platform: 'TikTok',
        niche: 'Fitness',
        targetAudience: 'Fitness Enthusiasts',
        languages: ['hi', 'te', 'ta']
      };

      const mockResponse = {
        success: true,
        languages: {
          'hi': 'Hindi (हिन्दी)',
          'te': 'Telugu (తెలుగు)',
          'ta': 'Tamil (தமிழ்)'
        },
        translations: {
          'en': 'Wake up at 5 AM...',
          'hi': 'सुबह 5 बजे उठो...',
          'te': 'ఉదయం 5:00 గంటకు లేవండి...',
          'ta': 'காலை 5 மணிக்கு எழுந்திருங்கள்...'
        }
      };

      service.adaptContentToLanguages(adaptRequest).subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.translations['hi']).toBeDefined();
        expect(response.data.translations['te']).toBeDefined();
        expect(response.data.translations['ta']).toBeDefined();
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/content/adapt-languages`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should get available languages', (done) => {
      // TEST: Get available languages
      const mockResponse = {
        success: true,
        data: {
          'hi': 'Hindi (हिन्दी)',
          'bn': 'Bengali (বাংলা)',
          'te': 'Telugu (తెలుగు)',
          'mr': 'Marathi (मराठी)',
          'ta': 'Tamil (தமிழ்)',
          'gu': 'Gujarati (ગુજરાતી)',
          'ur': 'Urdu (اردو)',
          'kn': 'Kannada (ಕನ್ನಡ)',
          'pa': 'Punjabi (ਪੰਜਾਬੀ)',
          'ml': 'Malayalam (മലയാളം)'
        }
      };

      service.getAvailableLanguages().subscribe((response) => {
        expect(response.success).toBe(true);
        expect(Object.keys(response.data).length).toBe(10);
        expect(response.data['hi']).toContain('हिन्दी');
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/content/languages`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should generate subtitles', (done) => {
      // TEST: LA-004 Generate subtitles
      const subtitleRequest = {
        content: 'Wake up at 5 AM. Hit the gym.',
        languageCode: 'hi'
      };

      const mockResponse = {
        success: true,
        data: {
          subtitles: '1\n00:00:00,000 --> 00:00:05,000\nसुबह 5 बजे उठो।\n\n2\n00:00:05,000 --> 00:00:10,000\nजिम जाओ।',
          languageCode: 'hi',
          format: 'SRT'
        }
      };

      service.generateSubtitles(subtitleRequest).subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.subtitles).toBeDefined();
        expect(response.data.format).toBe('SRT');
        expect(response.data.languageCode).toBe('hi');
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/content/generate-subtitles`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });
  });

  // ========== ANALYTICS API TESTS ==========

  describe('Analytics', () => {
    it('should get analytics dashboard data', (done) => {
      // TEST: AN-001 Dashboard analytics
      const mockResponse = {
        success: true,
        data: {
          totalGenerated: 5,
          avgViralScore: 7.4,
          avgConfidence: 85.2,
          educationalCount: 2,
          entertainmentCount: 3,
          platformStats: {
            'TikTok': 2,
            'Instagram Reels': 2,
            'YouTube Shorts': 1
          },
          modelStats: {
            'Phi': 5,
            'Llama': 3,
            'Mistral': 5
          }
        }
      };

      service.getAnalytics().subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.totalGenerated).toBe(5);
        expect(response.data.avgViralScore).toBe(7.4);
        expect(response.data.avgConfidence).toBe(85.2);
        expect(response.data.platformStats['TikTok']).toBe(2);
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/analytics/dashboard`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  // ========== TRENDING API TESTS ==========

  describe('Trending', () => {
    it('should get all trending content', (done) => {
      // TEST: TR-001 Get all trending data
      const mockResponse = {
        success: true,
        tiktokTrends: [
          {
            platform: 'tiktok',
            topic: 'POV: You are...',
            hashtag: '#trending1',
            trendScore: 92,
            growthRate: '+15%'
          }
        ],
        reelsTrends: [
          {
            platform: 'reels',
            topic: 'Relatable daily moments',
            hashtag: '#trending2',
            trendScore: 88,
            growthRate: '+12%'
          }
        ],
        youtubeTrends: [
          {
            platform: 'youtube',
            topic: 'Educational breakdown',
            hashtag: '#trending3',
            trendScore: 85,
            growthRate: '+10%'
          }
        ]
      };

      service.getTrendingContent().subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.tiktokTrends).toBeDefined();
        expect(response.data.reelsTrends).toBeDefined();
        expect(response.data.youtubeTrends).toBeDefined();
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/trending/all`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should get platform-specific trends', (done) => {
      // TEST: TR-002 Get platform-specific trends
      const mockResponse = {
        success: true,
        data: [
          {
            platform: 'tiktok',
            topic: 'Morning routines',
            hashtag: '#MorningRoutine',
            trendScore: 90
          },
          {
            platform: 'tiktok',
            topic: 'AI trends',
            hashtag: '#AITrends',
            trendScore: 88
          }
        ]
      };

      service.getTrendingByPlatform('tiktok').subscribe((response) => {
        expect(response.success).toBe(true);
        expect(response.data.length).toBeGreaterThan(0);
        expect(response.data[0].platform).toBe('tiktok');
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/trending/tiktok`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  // ========== ERROR HANDLING TESTS ==========

  describe('Error Handling', () => {
    it('should handle API errors gracefully', (done) => {
      // TEST: Error response
      const errorResponse = {
        success: false,
        error: 'Invalid credentials',
        statusCode: 401
      };

      service.login({ email: 'test@example.com', password: 'wrong' })
        .subscribe(
          () => fail('should have failed'),
          (error) => {
            expect(error.status).toBe(401);
            done();
          }
        );

      const req = httpMock.expectOne(`${apiUrl}/auth/login`);
      req.flush(errorResponse, { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle network errors', (done) => {
      // TEST: Network error
      service.getContentHistory().subscribe(
        () => fail('should have failed'),
        (error) => {
          expect(error.status).toBe(0);
          done();
        }
      );

      const req = httpMock.expectOne(`${apiUrl}/content/history`);
      req.error(new ErrorEvent('Network error'));
    });
  });

  // ========== REQUEST VALIDATION TESTS ==========

  describe('Request Validation', () => {
    it('should include correct headers in requests', (done) => {
      // TEST: Headers
      const generateRequest = {
        niche: 'Tech',
        platform: 'TikTok',
        contentType: 'educational',
        topicIdea: 'AI trends',
        targetAudience: 'Tech Geeks',
        vibe: 'educational'
      };

      service.generateContent(generateRequest).subscribe(() => {
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/content/generate`);
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      req.flush({ success: true, data: {} });
    });

    it('should send POST requests for generation', (done) => {
      // TEST: Correct HTTP method
      const request = {
        niche: 'Tech',
        platform: 'TikTok',
        contentType: 'entertainment',
        topicIdea: 'test',
        targetAudience: 'Everyone',
        vibe: 'funny'
      };

      service.generateContent(request).subscribe(() => {
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/content/generate`);
      expect(req.request.method).toBe('POST');
      req.flush({ success: true, data: {} });
    });

    it('should send GET requests for retrieval', (done) => {
      // TEST: Correct HTTP method for GET
      service.getContentHistory().subscribe(() => {
        done();
      });

      const req = httpMock.expectOne(`${apiUrl}/content/history`);
      expect(req.request.method).toBe('GET');
      req.flush({ success: true, data: [] });
    });
  });
});
