import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <nav class="bg-black/40 backdrop-blur-xl border-b border-white/10">
        <div class="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <a routerLink="/dashboard" class="text-2xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
            ViralForge AI
          </a>
          <div class="flex items-center gap-6">
            <a routerLink="/trending" class="text-white hover:text-purple-400">Trending</a>
            <a routerLink="/dashboard" class="text-white hover:text-purple-400">← Dashboard</a>
          </div>
        </div>
      </nav>

      <div class="max-w-7xl mx-auto px-6 py-12">
        <h2 class="text-3xl font-bold text-white mb-8">Content History</h2>

        <div *ngIf="isLoading" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8 text-center">
          <div class="w-12 h-12 rounded-full border-4 border-purple-500/20 border-t-purple-500 animate-spin mx-auto mb-4"></div>
          <p class="text-white">Loading your content history...</p>
        </div>

        <div *ngIf="!isLoading && contentList.length === 0" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8 text-center">
          <p class="text-white/70">No content generated yet. <a routerLink="/generator" class="text-purple-400 hover:text-purple-300">Create your first content!</a></p>
        </div>

        <div *ngIf="!isLoading && contentList.length > 0" class="space-y-6">
          <div *ngFor="let content of contentList" (click)="viewDetails(content)" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6 hover:bg-white/15 transition cursor-pointer hover:border-purple-400/50">
            <div class="flex justify-between items-start mb-4">
              <div>
                <h3 class="text-xl font-semibold text-white">{{ content.topicIdea | slice:0:50 }}...</h3>
                <p class="text-white/60 text-sm">{{ content.platform }} • {{ content.niche }}</p>
              </div>
              <div class="text-right">
                <p class="text-2xl font-bold text-purple-400">{{ content.viralScore | number:'1.1-1' }}/10</p>
                <p class="text-white/60 text-sm">Viral Score</p>
              </div>
            </div>
            <div class="grid grid-cols-3 gap-4 mb-4">
              <div>
                <p class="text-white/60 text-sm">Confidence</p>
                <p class="text-lg font-semibold text-pink-400">{{ content.confidenceScore | number:'1.1-1' }}%</p>
              </div>
              <div>
                <p class="text-white/60 text-sm">Model Used</p>
                <p class="text-sm text-green-400">{{ content.primaryModelUsed | slice:0:20 }}</p>
              </div>
              <div>
                <p class="text-white/60 text-sm">Generated</p>
                <p class="text-sm text-blue-400">{{ content.createdAt | date:'short' }}</p>
              </div>
            </div>
            <p class="text-white/70 text-sm line-clamp-3">{{ content.scriptContent }}</p>
            <p class="text-purple-400 text-xs mt-4">📄 Click to view details & download PDF</p>
          </div>
        </div>

        <!-- Detail Modal -->
        <div *ngIf="selectedContent" class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div class="backdrop-blur-xl bg-slate-900 rounded-xl border border-white/20 max-w-3xl max-h-[90vh] overflow-y-auto w-full p-8">
            <div class="flex justify-between items-start mb-6">
              <div>
                <h2 class="text-3xl font-bold text-white mb-2">{{ selectedContent.topicIdea }}</h2>
                <p class="text-white/60">{{ selectedContent.platform }} • {{ selectedContent.niche }} • {{ selectedContent.createdAt | date:'medium' }}</p>
              </div>
              <button (click)="selectedContent = null" class="text-white/60 hover:text-white text-2xl">✕</button>
            </div>

            <!-- Stats -->
            <div class="grid grid-cols-3 gap-4 mb-6">
              <div class="bg-white/10 rounded-lg p-4">
                <p class="text-white/60 text-sm">Viral Score</p>
                <p class="text-2xl font-bold text-purple-400">{{ selectedContent.viralScore | number:'1.1-1' }}/10</p>
              </div>
              <div class="bg-white/10 rounded-lg p-4">
                <p class="text-white/60 text-sm">Confidence</p>
                <p class="text-2xl font-bold text-pink-400">{{ selectedContent.confidenceScore | number:'1.1-1' }}%</p>
              </div>
              <div class="bg-white/10 rounded-lg p-4">
                <p class="text-white/60 text-sm">Model Used</p>
                <p class="text-sm text-green-400">{{ selectedContent.primaryModelUsed }}</p>
              </div>
            </div>

            <!-- Full Script -->
            <div class="bg-white/5 rounded-lg p-4 mb-6">
              <h3 class="text-white font-semibold mb-3">Generated Script</h3>
              <p class="text-white/90 whitespace-pre-wrap text-sm">{{ selectedContent.scriptContent }}</p>
            </div>

            <!-- Action Buttons -->
            <div class="flex gap-4">
              <button (click)="downloadPDF(selectedContent)" class="flex-1 py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:shadow-lg hover:shadow-purple-500/50 transition">
                📥 Download PDF
              </button>
              <button (click)="selectedContent = null" class="flex-1 py-3 px-4 bg-white/10 text-white font-semibold rounded-lg hover:bg-white/20 transition">
                Close
              </button>
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
export class HistoryComponent implements OnInit {
  contentList: any[] = [];
  isLoading = false;
  error = '';
  selectedContent: any = null;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    this.isLoading = true;
    this.error = '';

    this.apiService.getContentHistory().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.contentList = response.data;
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to load content history';
        this.isLoading = false;
      }
    });
  }

  viewDetails(content: any): void {
    this.selectedContent = content;
  }

  downloadPDF(content: any): void {
    const docContent = `
ViralForge AI - Content Report
${'='.repeat(50)}

Topic: ${content.topicIdea}
Platform: ${content.platform}
Niche: ${content.niche}
Generated: ${new Date(content.createdAt).toLocaleString()}

PERFORMANCE METRICS
${'-'.repeat(50)}
Viral Score: ${(content.viralScore || 0).toFixed(1)}/10
Confidence Score: ${(content.confidenceScore || 0).toFixed(1)}%
Model Used: ${content.primaryModelUsed}

GENERATED SCRIPT
${'-'.repeat(50)}
${content.scriptContent}
${'='.repeat(50)}

Downloaded from ViralForge AI
    `.trim();

    const blob = new Blob([docContent], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `content-${content.id || 'report'}-${new Date().toISOString().split('T')[0]}.txt`;
    link.click();
    window.URL.revokeObjectURL(url);
  }
}
