import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-trending',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <!-- Navigation -->
      <nav class="bg-black/40 backdrop-blur-xl border-b border-white/10">
        <div class="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <a routerLink="/dashboard" class="text-2xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
            ViralForge AI
          </a>
          <a routerLink="/dashboard" class="text-white hover:text-purple-400">← Dashboard</a>
        </div>
      </nav>

      <div class="max-w-7xl mx-auto px-6 py-12">
        <div class="mb-12">
          <h2 class="text-4xl font-bold text-white mb-2">🔥 Trending Now</h2>
          <p class="text-purple-200">Real-time trending content insights across platforms</p>
        </div>

        <!-- Platform Tabs -->
        <div class="flex gap-4 mb-8">
          <button (click)="selectedPlatform = 'tiktok'" [class.active]="selectedPlatform === 'tiktok'" class="px-6 py-2 rounded-lg font-semibold transition" [ngClass]="selectedPlatform === 'tiktok' ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white' : 'bg-white/10 text-white/70 hover:bg-white/20'">
            🎵 TikTok
          </button>
          <button (click)="selectedPlatform = 'reels'" [class.active]="selectedPlatform === 'reels'" class="px-6 py-2 rounded-lg font-semibold transition" [ngClass]="selectedPlatform === 'reels' ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white' : 'bg-white/10 text-white/70 hover:bg-white/20'">
            📱 Instagram Reels
          </button>
          <button (click)="selectedPlatform = 'youtube'" [class.active]="selectedPlatform === 'youtube'" class="px-6 py-2 rounded-lg font-semibold transition" [ngClass]="selectedPlatform === 'youtube' ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white' : 'bg-white/10 text-white/70 hover:bg-white/20'">
            ▶️ YouTube Shorts
          </button>
        </div>

        <!-- Loading State -->
        <div *ngIf="isLoading" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8 text-center">
          <div class="w-12 h-12 rounded-full border-4 border-purple-500/20 border-t-purple-500 animate-spin mx-auto mb-4"></div>
          <p class="text-white">Loading trending content...</p>
        </div>

        <!-- Error State -->
        <div *ngIf="error && !isLoading" class="backdrop-blur-xl bg-red-500/20 rounded-xl border border-red-500/50 p-6 mb-6">
          <p class="text-red-200">{{ error }}</p>
        </div>

        <!-- Trending Items Grid -->
        <div *ngIf="!isLoading && currentTrends.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div *ngFor="let trend of currentTrends" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6 hover:border-purple-400/50 hover:bg-white/15 transition group">
            <!-- Trend Score Badge -->
            <div class="flex justify-between items-start mb-4">
              <div>
                <h3 class="text-lg font-bold text-white group-hover:text-purple-300 transition">{{ trend.topic }}</h3>
                <p class="text-white/60 text-xs mt-1">{{ selectedPlatform }}</p>
              </div>
              <div class="bg-gradient-to-r from-purple-500 to-pink-500 rounded-lg px-3 py-1">
                <span class="text-white font-bold text-sm">{{ trend.trendScore }}/100</span>
              </div>
            </div>

            <!-- Hashtag and Growth -->
            <div class="mb-4 space-y-2">
              <p class="text-purple-300 text-sm font-semibold">{{ trend.hashtag }}</p>
              <div class="flex items-center gap-2">
                <span class="text-green-400 text-xs font-bold">📈 {{ trend.growthRate }}</span>
                <span class="text-white/60 text-xs">growth rate</span>
              </div>
            </div>

            <!-- Content Type -->
            <div class="mb-4 flex flex-wrap gap-2">
              <span class="bg-purple-500/30 text-purple-200 text-xs px-2 py-1 rounded">{{ trend.contentType }}</span>
              <span *ngIf="trend.challenge" class="bg-pink-500/30 text-pink-200 text-xs px-2 py-1 rounded">{{ trend.challenge }}</span>
            </div>

            <!-- Music/Audio -->
            <div class="mb-4 p-3 bg-white/5 rounded-lg border border-white/10">
              <p class="text-white/60 text-xs mb-1">Trending Audio</p>
              <p class="text-white text-sm truncate">🎵 {{ trend.music }}</p>
            </div>

            <!-- Description -->
            <p class="text-white/70 text-sm mb-4 line-clamp-2">{{ trend.description }}</p>

            <!-- Action Button -->
            <button routerLink="/generator" class="w-full py-2 px-4 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:shadow-lg hover:shadow-purple-500/50 transition text-sm">
              Create Content
            </button>
          </div>
        </div>

        <!-- Empty State -->
        <div *ngIf="!isLoading && currentTrends.length === 0 && !error" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8 text-center">
          <p class="text-white/70">No trending content available. Try refreshing or selecting a different platform.</p>
          <button (click)="loadTrendingContent()" class="mt-4 px-6 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:shadow-lg transition">
            Refresh Trends
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .active {
      @apply shadow-lg shadow-purple-500/50;
    }
  `]
})
export class TrendingComponent implements OnInit {
  selectedPlatform: string = 'tiktok';
  currentTrends: any[] = [];
  allTrends: any = {};
  isLoading = false;
  error = '';

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadTrendingContent();
  }

  loadTrendingContent(): void {
    this.isLoading = true;
    this.error = '';

    this.apiService.getTrendingContent().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.allTrends = response.data;
          this.updateCurrentTrends();
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to load trending content';
        this.isLoading = false;
      }
    });
  }

  updateCurrentTrends(): void {
    const platformKey = this.selectedPlatform + 'Trends';
    this.currentTrends = this.allTrends[platformKey] || [];
  }
}
