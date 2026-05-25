import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <!-- Navigation -->
      <nav class="bg-black/40 backdrop-blur-xl border-b border-white/10">
        <div class="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <h1 class="text-2xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
            ViralForge AI
          </h1>
          <div class="flex items-center gap-6">
            <a routerLink="/generator" class="text-white hover:text-purple-400 transition">Create</a>
            <a routerLink="/history" class="text-white hover:text-purple-400 transition">History</a>
            <a routerLink="/analytics" class="text-white hover:text-purple-400 transition">Analytics</a>
            <a routerLink="/trending" class="text-white hover:text-purple-400 transition">Trending</a>
            <a routerLink="/profile" class="text-white hover:text-purple-400 transition">Profile</a>
            <button (click)="logout()" class="px-4 py-2 rounded-lg bg-red-500/20 text-red-400 hover:bg-red-500/30 transition">
              Logout
            </button>
          </div>
        </div>
      </nav>

      <!-- Main Content -->
      <div class="max-w-7xl mx-auto px-6 py-12">
        <div class="mb-12">
          <h2 class="text-4xl font-bold text-white mb-2">Welcome, {{ user?.fullName }}!</h2>
          <p class="text-purple-200">Create viral short-form content with AI-powered insights</p>
        </div>

        <!-- Stats Grid -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
          <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6 hover:border-purple-500/50 transition">
            <p class="text-white/70 text-sm mb-2">API Usage</p>
            <p class="text-3xl font-bold text-purple-400">{{ user?.apiUsageCount || 0 }}</p>
            <p class="text-white/50 text-xs mt-2">of {{ user?.maxMonthlyApiCalls }} calls</p>
          </div>

          <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6 hover:border-pink-500/50 transition">
            <p class="text-white/70 text-sm mb-2">Content Generated</p>
            <p class="text-3xl font-bold text-pink-400">{{ analytics.totalGenerated || 0 }}</p>
            <p class="text-white/50 text-xs mt-2">Total pieces</p>
          </div>

          <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6 hover:border-blue-500/50 transition">
            <p class="text-white/70 text-sm mb-2">Avg. Viral Score</p>
            <p class="text-3xl font-bold text-blue-400">{{ avgViralScore | number:'1.1-1' }}</p>
            <p class="text-white/50 text-xs mt-2">Out of 10</p>
          </div>

          <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6 hover:border-green-500/50 transition">
            <p class="text-white/70 text-sm mb-2">Confidence Score</p>
            <p class="text-3xl font-bold text-green-400">{{ avgConfidence | number:'1.1-1' }}%</p>
            <p class="text-white/50 text-xs mt-2">Average</p>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="mb-12">
          <h3 class="text-2xl font-bold text-white mb-6">Quick Actions</h3>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div routerLink="/generator" class="backdrop-blur-xl bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-xl border border-purple-500/50 p-8 cursor-pointer hover:shadow-lg hover:shadow-purple-500/20 transition group">
              <div class="text-4xl mb-4 group-hover:scale-110 transition">✨</div>
              <h4 class="text-xl font-bold text-white mb-2">Create Content</h4>
              <p class="text-white/70">Generate AI-powered viral scripts</p>
            </div>

            <div routerLink="/history" class="backdrop-blur-xl bg-gradient-to-br from-blue-500/20 to-cyan-500/20 rounded-xl border border-blue-500/50 p-8 cursor-pointer hover:shadow-lg hover:shadow-blue-500/20 transition group">
              <div class="text-4xl mb-4 group-hover:scale-110 transition">📚</div>
              <h4 class="text-xl font-bold text-white mb-2">View History</h4>
              <p class="text-white/70">Browse all generated content</p>
            </div>

            <div routerLink="/analytics" class="backdrop-blur-xl bg-gradient-to-br from-green-500/20 to-emerald-500/20 rounded-xl border border-green-500/50 p-8 cursor-pointer hover:shadow-lg hover:shadow-green-500/20 transition group">
              <div class="text-4xl mb-4 group-hover:scale-110 transition">📊</div>
              <h4 class="text-xl font-bold text-white mb-2">View Analytics</h4>
              <p class="text-white/70">Track performance metrics</p>
            </div>

            <div routerLink="/trending" class="backdrop-blur-xl bg-gradient-to-br from-orange-500/20 to-red-500/20 rounded-xl border border-orange-500/50 p-8 cursor-pointer hover:shadow-lg hover:shadow-orange-500/20 transition group">
              <div class="text-4xl mb-4 group-hover:scale-110 transition">🔥</div>
              <h4 class="text-xl font-bold text-white mb-2">Trending Now</h4>
              <p class="text-white/70">Discover real-time trending content</p>
            </div>
          </div>
        </div>

        <!-- Features -->
        <div>
          <h3 class="text-2xl font-bold text-white mb-6">Platform Features</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="backdrop-blur-xl bg-white/5 rounded-lg border border-white/10 p-6">
              <h4 class="text-lg font-semibold text-white mb-2">🤖 Multi-Model AI</h4>
              <p class="text-white/70 text-sm">Uses Phi, Llama, and Mistral for optimal content generation</p>
            </div>
            <div class="backdrop-blur-xl bg-white/5 rounded-lg border border-white/10 p-6">
              <h4 class="text-lg font-semibold text-white mb-2">🎯 Viral Optimization</h4>
              <p class="text-white/70 text-sm">Analyzes audience and trends for maximum engagement</p>
            </div>
            <div class="backdrop-blur-xl bg-white/5 rounded-lg border border-white/10 p-6">
              <h4 class="text-lg font-semibold text-white mb-2">📱 Multi-Platform</h4>
              <p class="text-white/70 text-sm">Optimize for TikTok, Instagram Reels, YouTube Shorts</p>
            </div>
            <div class="backdrop-blur-xl bg-white/5 rounded-lg border border-white/10 p-6">
              <h4 class="text-lg font-semibold text-white mb-2">⚡ Instant Generation</h4>
              <p class="text-white/70 text-sm">Get complete content strategies in seconds</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  user: any;
  analytics: any = {
    totalGenerated: 0,
    avgViralScore: 0,
    avgConfidence: 0
  };
  avgViralScore = 0;
  avgConfidence = 0;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();
    this.loadAnalytics();
    this.loadUserData();

    // Refresh user data every 30 seconds to show updated API usage
    setInterval(() => {
      this.loadUserData();
    }, 30000);
  }

  loadUserData(): void {
    this.apiService.getCurrentUser().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.user = response.data;
          this.authService.updateUser(response.data);
        }
      },
      error: (error) => {
        console.log('Could not refresh user data');
      }
    });
  }

  loadAnalytics(): void {
    this.apiService.getAnalytics().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.analytics = response.data;
          this.avgViralScore = response.data.avgViralScore || 0;
          this.avgConfidence = response.data.avgConfidence || 0;
        }
      },
      error: (error) => {
        console.log('Analytics not available yet');
        this.analytics = {
          totalGenerated: 0,
          avgViralScore: 0,
          avgConfidence: 0
        };
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
