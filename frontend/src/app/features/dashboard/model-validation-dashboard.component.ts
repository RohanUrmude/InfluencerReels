import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-model-validation-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 p-6">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-4xl font-bold text-white mb-2">🤖 Model Validation Dashboard</h1>
        <p class="text-purple-200">Assess and Compare Phi, Llama, and Mistral Models with Google Gemini AI</p>
      </div>

      <!-- Tab Navigation -->
      <div class="flex gap-4 mb-8">
        <button (click)="activeTab = 'performance'" [class.active]="activeTab === 'performance'"
          class="px-6 py-2 rounded-lg font-semibold transition"
          [ngClass]="activeTab === 'performance' ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white' : 'bg-white/10 text-white/70 hover:bg-white/20'">
          📊 Performance
        </button>
        <button (click)="activeTab = 'validate'" [class.active]="activeTab === 'validate'"
          class="px-6 py-2 rounded-lg font-semibold transition"
          [ngClass]="activeTab === 'validate' ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white' : 'bg-white/10 text-white/70 hover:bg-white/20'">
          ✅ Validate Response
        </button>
        <button (click)="activeTab = 'compare'" [class.active]="activeTab === 'compare'"
          class="px-6 py-2 rounded-lg font-semibold transition"
          [ngClass]="activeTab === 'compare' ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white' : 'bg-white/10 text-white/70 hover:bg-white/20'">
          🔄 Compare Models
        </button>
      </div>

      <!-- Performance Tab -->
      <div *ngIf="activeTab === 'performance'">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          <div *ngFor="let model of modelsData" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6 hover:border-purple-400/50 transition">
            <!-- Model Header -->
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-bold text-white">{{ getModelName(model.modelName) }}</h3>
              <span *ngIf="model.isHealthy" class="text-2xl">✅</span>
              <span *ngIf="!model.isHealthy" class="text-2xl">⚠️</span>
            </div>

            <!-- Metrics -->
            <div class="space-y-3">
              <div class="flex justify-between">
                <span class="text-white/60">Requests</span>
                <span class="text-white font-bold">{{ model.totalRequests || 0 }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/60">Success Rate</span>
                <span class="text-green-400 font-bold">{{ (model.reliabilityScore * 100 || 0) | number:'1.0-0' }}%</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/60">Avg Latency</span>
                <span class="text-blue-400 font-bold">{{ model.averageLatencyMs || 0 }}ms</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/60">Avg Tokens</span>
                <span class="text-yellow-400 font-bold">{{ model.averageTokensUsed || 0 }}</span>
              </div>
            </div>

            <!-- Health Bar -->
            <div class="mt-4 w-full bg-white/10 rounded-full h-2">
              <div class="bg-gradient-to-r from-purple-500 to-pink-500 h-2 rounded-full"
                [style.width.%]="(model.reliabilityScore || 0) * 100"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Validate Tab -->
      <div *ngIf="activeTab === 'validate'" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8">
        <h2 class="text-2xl font-bold text-white mb-6">Validate Model Response</h2>

        <div class="space-y-4 mb-6">
          <!-- Model Selector -->
          <div>
            <label class="text-white/80 block mb-2">Select Model:</label>
            <select [(ngModel)]="selectedModel" class="w-full bg-white/10 border border-white/20 rounded-lg px-4 py-2 text-white">
              <option value="mistralai/Mistral-7B-Instruct-v0.2">Mistral</option>
              <option value="meta-llama/Meta-Llama-3-8B-Instruct">Llama</option>
              <option value="microsoft/Phi-3-mini-4k-instruct">Phi</option>
            </select>
          </div>

          <!-- Prompt Input -->
          <div>
            <label class="text-white/80 block mb-2">Prompt:</label>
            <textarea [(ngModel)]="validationPrompt" class="w-full bg-white/10 border border-white/20 rounded-lg px-4 py-2 text-white h-24"
              placeholder="Enter the prompt used..."></textarea>
          </div>

          <!-- Response Input -->
          <div>
            <label class="text-white/80 block mb-2">Model Response:</label>
            <textarea [(ngModel)]="modelResponse" class="w-full bg-white/10 border border-white/20 rounded-lg px-4 py-2 text-white h-32"
              placeholder="Paste the model's response here..."></textarea>
          </div>

          <!-- Buttons -->
          <div class="flex gap-4">
            <button (click)="validateResponse()" [disabled]="isValidating"
              class="flex-1 px-6 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:shadow-lg transition disabled:opacity-50">
              {{ isValidating ? '⏳ Validating...' : '✅ Validate Response' }}
            </button>
            <button (click)="scoreResponse()" [disabled]="isValidating"
              class="flex-1 px-6 py-2 bg-gradient-to-r from-blue-500 to-cyan-500 text-white font-semibold rounded-lg hover:shadow-lg transition disabled:opacity-50">
              {{ isValidating ? '⏳ Scoring...' : '⭐ Score Quality' }}
            </button>
          </div>
        </div>

        <!-- Results -->
        <div *ngIf="validationResult" class="space-y-4">
          <div class="backdrop-blur-xl bg-green-500/20 border border-green-500/50 rounded-xl p-6">
            <h3 class="text-lg font-bold text-green-300 mb-3">📋 Gemini Evaluation:</h3>
            <p class="text-white/80 whitespace-pre-wrap">{{ validationResult }}</p>
          </div>
        </div>

        <div *ngIf="scoreResult" class="space-y-4 mt-4">
          <div class="backdrop-blur-xl bg-blue-500/20 border border-blue-500/50 rounded-xl p-6">
            <h3 class="text-lg font-bold text-blue-300 mb-3">⭐ Quality Score:</h3>
            <div class="flex items-center gap-4">
              <div class="text-6xl font-bold text-blue-400">{{ scoreResult }}<span class="text-3xl">/10</span></div>
              <div class="flex-1">
                <div class="bg-white/10 rounded-full h-4 overflow-hidden">
                  <div class="bg-gradient-to-r from-yellow-400 to-red-500 h-4"
                    [style.width.%]="scoreResult * 10"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Compare Tab -->
      <div *ngIf="activeTab === 'compare'" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8">
        <h2 class="text-2xl font-bold text-white mb-6">Compare Models</h2>

        <div class="space-y-4 mb-6">
          <!-- Comparison Prompt -->
          <div>
            <label class="text-white/80 block mb-2">Prompt:</label>
            <textarea [(ngModel)]="comparisonPrompt" class="w-full bg-white/10 border border-white/20 rounded-lg px-4 py-2 text-white h-20"
              placeholder="Enter prompt for comparison..."></textarea>
          </div>

          <!-- Model Responses Grid -->
          <div class="grid grid-cols-3 gap-4">
            <div>
              <label class="text-white/80 block mb-2">Mistral Response:</label>
              <textarea [(ngModel)]="mistralResponse" class="w-full bg-white/10 border border-white/20 rounded-lg px-4 py-2 text-white h-24"
                placeholder="Mistral's response..."></textarea>
            </div>
            <div>
              <label class="text-white/80 block mb-2">Llama Response:</label>
              <textarea [(ngModel)]="llamaResponse" class="w-full bg-white/10 border border-white/20 rounded-lg px-4 py-2 text-white h-24"
                placeholder="Llama's response..."></textarea>
            </div>
            <div>
              <label class="text-white/80 block mb-2">Phi Response:</label>
              <textarea [(ngModel)]="phiResponse" class="w-full bg-white/10 border border-white/20 rounded-lg px-4 py-2 text-white h-24"
                placeholder="Phi's response..."></textarea>
            </div>
          </div>

          <!-- Compare Button -->
          <button (click)="compareModels()" [disabled]="isValidating"
            class="w-full px-6 py-3 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:shadow-lg transition disabled:opacity-50">
            {{ isValidating ? '⏳ Comparing...' : '🔄 Compare Models with Gemini' }}
          </button>
        </div>

        <!-- Comparison Results -->
        <div *ngIf="comparisonResult" class="space-y-4">
          <div class="backdrop-blur-xl bg-purple-500/20 border border-purple-500/50 rounded-xl p-6">
            <h3 class="text-lg font-bold text-purple-300 mb-3">🔍 Comparative Analysis:</h3>
            <p class="text-white/80 whitespace-pre-wrap">{{ comparisonResult }}</p>
          </div>
        </div>
      </div>

      <!-- Loading Overlay -->
      <div *ngIf="isLoading" class="fixed inset-0 bg-black/50 flex items-center justify-center rounded-xl">
        <div class="text-center">
          <div class="inline-block animate-spin text-4xl mb-4">⚙️</div>
          <p class="text-white text-lg">Processing with Gemini...</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .active {
      box-shadow: 0 0 20px rgba(168, 85, 247, 0.5);
    }
  `]
})
export class ModelValidationDashboardComponent implements OnInit {
  activeTab: 'performance' | 'validate' | 'compare' = 'performance';

  // Performance Tab
  modelsData: any[] = [];

  // Validation Tab
  selectedModel = 'mistralai/Mistral-7B-Instruct-v0.2';
  validationPrompt = '';
  modelResponse = '';
  validationResult = '';
  scoreResult: number | null = null;

  // Compare Tab
  comparisonPrompt = '';
  mistralResponse = '';
  llamaResponse = '';
  phiResponse = '';
  comparisonResult = '';

  // Loading States
  isValidating = false;
  isLoading = false;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadPerformanceMetrics();
  }

  loadPerformanceMetrics(): void {
    this.apiService.getModelPerformance().subscribe({
      next: (response: any) => {
        if (response.models) {
          this.modelsData = [
            response.models['mistralai/Mistral-7B-Instruct-v0.2'] || {},
            response.models['meta-llama/Meta-Llama-3-8B-Instruct'] || {},
            response.models['microsoft/Phi-3-mini-4k-instruct'] || {}
          ].map(model => ({
            modelName: model.modelName,
            totalRequests: model.totalRequests || 0,
            reliabilityScore: model.reliabilityScore || 0,
            averageLatencyMs: model.averageLatencyMs || 0,
            averageTokensUsed: model.averageTokensUsed || 0,
            isHealthy: model.isHealthy !== false
          }));
        }
      }
    });
  }

  validateResponse(): void {
    if (!this.selectedModel || !this.validationPrompt || !this.modelResponse) {
      alert('Please fill in all fields');
      return;
    }

    this.isValidating = true;
    this.validationResult = '';

    this.apiService.validateModelResponse(
      this.selectedModel,
      this.validationPrompt,
      this.modelResponse
    ).subscribe({
      next: (response: any) => {
        this.validationResult = response.evaluation || 'No evaluation available';
        this.isValidating = false;
      },
      error: (error) => {
        this.validationResult = 'Error: ' + (error.error?.message || 'Failed to validate');
        this.isValidating = false;
      }
    });
  }

  scoreResponse(): void {
    if (!this.selectedModel || !this.modelResponse) {
      alert('Please fill in model and response fields');
      return;
    }

    this.isValidating = true;
    this.scoreResult = null;

    this.apiService.scoreModelResponse(
      this.selectedModel,
      'Content Quality Assessment',
      this.modelResponse
    ).subscribe({
      next: (response: any) => {
        this.scoreResult = parseFloat(response.score) || 0;
        this.isValidating = false;
      },
      error: (error) => {
        alert('Error: ' + (error.error?.message || 'Failed to score'));
        this.isValidating = false;
      }
    });
  }

  compareModels(): void {
    if (!this.comparisonPrompt || !this.mistralResponse || !this.llamaResponse || !this.phiResponse) {
      alert('Please fill in all fields');
      return;
    }

    this.isValidating = true;
    this.comparisonResult = '';

    const responses = {
      'Mistral': this.mistralResponse,
      'Llama': this.llamaResponse,
      'Phi': this.phiResponse
    };

    this.apiService.compareModels(this.comparisonPrompt, responses).subscribe({
      next: (response: any) => {
        this.comparisonResult = response.comparison?.comparative_analysis || 'No analysis available';
        this.isValidating = false;
      },
      error: (error) => {
        this.comparisonResult = 'Error: ' + (error.error?.message || 'Failed to compare');
        this.isValidating = false;
      }
    });
  }

  getModelName(fullName: string): string {
    if (fullName.includes('Mistral')) return '🟦 Mistral';
    if (fullName.includes('Llama')) return '🦙 Llama';
    if (fullName.includes('Phi')) return '🚀 Phi';
    return fullName;
  }
}
