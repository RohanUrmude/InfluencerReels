import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  register(data: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/auth/register`, data);
  }

  login(credentials: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/auth/login`, credentials);
  }

  generateContent(request: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/content/generate`, request);
  }

  getContentHistory(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/content/history`);
  }

  getAnalytics(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/analytics/dashboard`);
  }

  getHealth(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/health`);
  }

  getCurrentUser(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/auth/me`);
  }

  getTrendingContent(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/trending/all`);
  }

  // Model Validation APIs
  getModelPerformance(): Observable<any> {
    return this.http.get(`${this.apiUrl}/models/validation/performance/all`);
  }

  validateModelResponse(modelName: string, prompt: string, response: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/models/validation/validate`, response, {
      params: { modelName, prompt }
    });
  }

  scoreModelResponse(modelName: string, context: string, response: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/models/validation/score`, response, {
      params: { modelName, context }
    });
  }

  compareModels(prompt: string, responses: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/models/validation/compare`, responses, {
      params: { prompt }
    });
  }

  getTrendingByPlatform(platform: string): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/trending/${platform}`);
  }

  adaptContentToLanguages(request: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/content/adapt-languages`, request);
  }

  getAvailableLanguages(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/content/languages`);
  }

  generateSubtitles(request: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.apiUrl}/content/generate-subtitles`, request);
  }
}
