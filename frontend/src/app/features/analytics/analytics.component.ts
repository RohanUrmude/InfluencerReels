import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <nav class="bg-black/40 backdrop-blur-xl border-b border-white/10">
        <div class="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <a routerLink="/dashboard" class="text-2xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
            ViralForge AI
          </a>
          <a routerLink="/dashboard" class="text-white hover:text-purple-400">← Dashboard</a>
        </div>
      </nav>

      <div class="max-w-7xl mx-auto px-6 py-12">
        <h2 class="text-3xl font-bold text-white mb-8">Performance Analytics</h2>

        <div *ngIf="isLoading" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8 text-center">
          <div class="w-12 h-12 rounded-full border-4 border-purple-500/20 border-t-purple-500 animate-spin mx-auto mb-4"></div>
          <p class="text-white">Loading analytics...</p>
        </div>

        <div *ngIf="!isLoading" class="space-y-8">
          <!-- Key Metrics -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="backdrop-blur-xl bg-gradient-to-br from-purple-500/20 to-purple-500/10 rounded-xl border border-purple-500/50 p-6">
              <p class="text-white/70 text-sm mb-2">Avg Viral Score</p>
              <p class="text-4xl font-bold text-purple-400">{{ avgViralScore | number:'1.1-1' }}/10</p>
              <p class="text-white/50 text-xs mt-2">From {{ totalGenerated }} contents</p>
            </div>

            <div class="backdrop-blur-xl bg-gradient-to-br from-pink-500/20 to-pink-500/10 rounded-xl border border-pink-500/50 p-6">
              <p class="text-white/70 text-sm mb-2">Total Generated</p>
              <p class="text-4xl font-bold text-pink-400">{{ totalGenerated }}</p>
              <p class="text-white/50 text-xs mt-2">Content pieces</p>
            </div>

            <div class="backdrop-blur-xl bg-gradient-to-br from-blue-500/20 to-blue-500/10 rounded-xl border border-blue-500/50 p-6">
              <p class="text-white/70 text-sm mb-2">Avg Confidence</p>
              <p class="text-4xl font-bold text-blue-400">{{ avgConfidence | number:'1.1-1' }}%</p>
              <p class="text-white/50 text-xs mt-2">AI prediction certainty</p>
            </div>
          </div>

          <!-- Content Type Breakdown -->
          <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
            <h3 class="text-xl font-semibold text-white mb-6">Content Type Breakdown</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <p class="text-white/70 mb-2">Educational</p>
                <div class="bg-white/5 rounded-full h-3 overflow-hidden">
                  <div class="bg-green-500 h-full" [style.width.%]="educationalPercentage"></div>
                </div>
                <p class="text-white/50 text-sm mt-2">{{ educationalCount }} contents</p>
              </div>
              <div>
                <p class="text-white/70 mb-2">Entertainment</p>
                <div class="bg-white/5 rounded-full h-3 overflow-hidden">
                  <div class="bg-pink-500 h-full" [style.width.%]="entertainmentPercentage"></div>
                </div>
                <p class="text-white/50 text-sm mt-2">{{ entertainmentCount }} contents</p>
              </div>
            </div>
          </div>

          <!-- Platform Stats -->
          <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
            <h3 class="text-xl font-semibold text-white mb-6">Platform Distribution</h3>
            <div class="space-y-4">
              <div *ngFor="let platform of platformStats" class="flex items-center justify-between">
                <p class="text-white/70">{{ platform.name }}</p>
                <div class="flex items-center gap-4">
                  <div class="bg-white/5 rounded-full h-2 w-40 overflow-hidden">
                    <div class="bg-purple-500 h-full" [style.width.%]="platform.percentage"></div>
                  </div>
                  <p class="text-white font-semibold w-12 text-right">{{ platform.count }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Models Used -->
          <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
            <h3 class="text-xl font-semibold text-white mb-6">AI Models Used</h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div class="bg-white/5 rounded-lg p-4 text-center">
                <p class="text-white/70 text-sm mb-2">Phi (Analysis)</p>
                <p class="text-2xl font-bold text-purple-400">{{ modelStats.phi }}</p>
              </div>
              <div class="bg-white/5 rounded-lg p-4 text-center">
                <p class="text-white/70 text-sm mb-2">Llama (Educational)</p>
                <p class="text-2xl font-bold text-green-400">{{ modelStats.llama }}</p>
              </div>
              <div class="bg-white/5 rounded-lg p-4 text-center">
                <p class="text-white/70 text-sm mb-2">Mistral (Entertainment)</p>
                <p class="text-2xl font-bold text-pink-400">{{ modelStats.mistral }}</p>
              </div>
            </div>
          </div>
        </div>

        <div *ngIf="error" class="backdrop-blur-xl bg-red-500/20 rounded-xl border border-red-500/50 p-6 mt-6">
          <p class="text-red-200">{{ error }}</p>
        </div>
      </div>
    </div>
  `
})
export class AnalyticsComponent implements OnInit {
  isLoading = false;
  error = '';

  avgViralScore = 0;
  avgConfidence = 0;
  totalGenerated = 0;
  educationalCount = 0;
  entertainmentCount = 0;
  educationalPercentage = 0;
  entertainmentPercentage = 0;
  platformStats: any[] = [];
  modelStats = { phi: 0, llama: 0, mistral: 0 };

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    this.isLoading = true;
    this.error = '';

    this.apiService.getAnalytics().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.processAnalyticsData(response.data);
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to load analytics';
        this.isLoading = false;
      }
    });
  }

  processAnalyticsData(data: any): void {
    this.totalGenerated = data.totalGenerated || 0;
    this.avgViralScore = data.avgViralScore || 0;
    this.avgConfidence = data.avgConfidence || 0;
    this.educationalCount = data.educationalCount || 0;
    this.entertainmentCount = data.entertainmentCount || 0;

    const total = this.educationalCount + this.entertainmentCount;
    this.educationalPercentage = total > 0 ? (this.educationalCount / total) * 100 : 0;
    this.entertainmentPercentage = total > 0 ? (this.entertainmentCount / total) * 100 : 0;

    this.platformStats = data.platformStats || [];
    this.modelStats = data.modelStats || { phi: 0, llama: 0, mistral: 0 };
  }
}
