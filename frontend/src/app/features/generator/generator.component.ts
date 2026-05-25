import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-generator',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <!-- Navigation -->
      <nav class="bg-black/40 backdrop-blur-xl border-b border-white/10">
        <div class="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <a routerLink="/dashboard" class="text-2xl font-bold bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
            ViralForge AI
          </a>
          <a routerLink="/dashboard" class="text-white hover:text-purple-400">← Back</a>
        </div>
      </nav>

      <div class="max-w-7xl mx-auto px-6 py-12">
        <h2 class="text-4xl font-bold text-white mb-2">Create Viral Content</h2>
        <p class="text-purple-200 mb-8">Describe your content idea and let AI create viral scripts in Indian languages</p>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- Form -->
          <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8">
            <form [formGroup]="generatorForm" (ngSubmit)="onGenerate()" class="space-y-6">
              <div>
                <label class="block text-white font-semibold mb-2">Niche</label>
                <select
                  formControlName="niche"
                  class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white focus:outline-none focus:border-purple-500"
                >
                  <option value="" class="bg-slate-900">Select niche...</option>
                  <option value="Tech" class="bg-slate-900">Tech</option>
                  <option value="Fitness" class="bg-slate-900">Fitness</option>
                  <option value="Finance" class="bg-slate-900">Finance</option>
                  <option value="Beauty" class="bg-slate-900">Beauty</option>
                  <option value="Fashion" class="bg-slate-900">Fashion</option>
                  <option value="Food" class="bg-slate-900">Food</option>
                  <option value="Travel" class="bg-slate-900">Travel</option>
                  <option value="Gaming" class="bg-slate-900">Gaming</option>
                  <option value="Education" class="bg-slate-900">Education</option>
                  <option value="Business" class="bg-slate-900">Business</option>
                  <option value="Entertainment" class="bg-slate-900">Entertainment</option>
                  <option value="Music" class="bg-slate-900">Music</option>
                  <option value="Sports" class="bg-slate-900">Sports</option>
                  <option value="Lifestyle" class="bg-slate-900">Lifestyle</option>
                  <option value="Comedy" class="bg-slate-900">Comedy</option>
                  <option value="Self-Help" class="bg-slate-900">Self-Help</option>
                </select>
              </div>

              <div>
                <label class="block text-white font-semibold mb-2">Target Audience</label>
                <select
                  formControlName="targetAudience"
                  class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white focus:outline-none focus:border-purple-500"
                >
                  <option value="" class="bg-slate-900">Select audience...</option>
                  <option value="Gen Z (13-24)" class="bg-slate-900">Gen Z (13-24)</option>
                  <option value="Millennials (25-40)" class="bg-slate-900">Millennials (25-40)</option>
                  <option value="Gen X (41-56)" class="bg-slate-900">Gen X (41-56)</option>
                  <option value="Professionals" class="bg-slate-900">Professionals</option>
                  <option value="Students" class="bg-slate-900">Students</option>
                  <option value="Parents" class="bg-slate-900">Parents</option>
                  <option value="Entrepreneurs" class="bg-slate-900">Entrepreneurs</option>
                  <option value="Fitness Enthusiasts" class="bg-slate-900">Fitness Enthusiasts</option>
                  <option value="Tech Geeks" class="bg-slate-900">Tech Geeks</option>
                  <option value="Fashion Lovers" class="bg-slate-900">Fashion Lovers</option>
                  <option value="Foodies" class="bg-slate-900">Foodies</option>
                  <option value="Gamers" class="bg-slate-900">Gamers</option>
                  <option value="Creatives" class="bg-slate-900">Creatives</option>
                  <option value="Travelers" class="bg-slate-900">Travelers</option>
                  <option value="Business Owners" class="bg-slate-900">Business Owners</option>
                </select>
              </div>

              <div>
                <label class="block text-white font-semibold mb-2">Vibe/Tone</label>
                <select
                  formControlName="vibe"
                  class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white focus:outline-none focus:border-purple-500"
                >
                  <option value="" class="bg-slate-900">Select vibe...</option>
                  <option value="funny" class="bg-slate-900">Funny</option>
                  <option value="inspirational" class="bg-slate-900">Inspirational</option>
                  <option value="educational" class="bg-slate-900">Educational</option>
                  <option value="emotional" class="bg-slate-900">Emotional</option>
                  <option value="trending" class="bg-slate-900">Trending</option>
                </select>
              </div>

              <div>
                <label class="block text-white font-semibold mb-2">Platform</label>
                <select
                  formControlName="platform"
                  class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white focus:outline-none focus:border-purple-500"
                >
                  <option value="" class="bg-slate-900">Select platform...</option>
                  <option value="Instagram Reels" class="bg-slate-900">Instagram Reels</option>
                  <option value="TikTok" class="bg-slate-900">TikTok</option>
                  <option value="YouTube Shorts" class="bg-slate-900">YouTube Shorts</option>
                </select>
              </div>

              <div>
                <label class="block text-white font-semibold mb-2">Content Type</label>
                <select
                  formControlName="contentType"
                  class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white focus:outline-none focus:border-purple-500"
                >
                  <option value="" class="bg-slate-900">Select type...</option>
                  <option value="educational" class="bg-slate-900">Educational</option>
                  <option value="entertainment" class="bg-slate-900">Entertainment</option>
                </select>
              </div>

              <div>
                <label class="block text-white font-semibold mb-2">Topic Idea</label>
                <textarea
                  formControlName="topicIdea"
                  class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white placeholder-white/50 focus:outline-none focus:border-purple-500 resize-none h-24"
                  placeholder="Describe your content idea in detail..."
                ></textarea>
              </div>

              <div>
                <label class="block text-white font-semibold mb-2">Creator Goal (Optional)</label>
                <textarea
                  formControlName="creatorGoal"
                  class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white placeholder-white/50 focus:outline-none focus:border-purple-500 resize-none h-20"
                  placeholder="What's your goal for this content? e.g., Increase followers, Drive engagement"
                ></textarea>
              </div>

              <!-- Language Selection -->
              <div>
                <label class="block text-white font-semibold mb-3">📍 Generate in Indian Languages</label>
                <div class="grid grid-cols-2 gap-2">
                  <label *ngFor="let lang of availableLanguages | keyvalue" class="flex items-center p-2 rounded-lg bg-white/5 border border-white/10 hover:border-purple-500/50 cursor-pointer transition">
                    <input
                      type="checkbox"
                      [value]="lang.key"
                      (change)="toggleLanguage(lang.key)"
                      class="rounded"
                    >
                    <span class="text-white text-sm ml-2">{{ lang.value }}</span>
                  </label>
                </div>
              </div>

              <button
                type="submit"
                [disabled]="isGenerating || selectedLanguages.length === 0"
                class="w-full py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:shadow-lg hover:shadow-purple-500/50 transition disabled:opacity-50"
              >
                {{ isGenerating ? '⚡ Generating Content...' : '✨ Generate Content' }}
              </button>
            </form>
          </div>

          <!-- Results -->
          <div class="space-y-6">
            <div *ngIf="!generatedContent && !isGenerating" class="backdrop-blur-xl bg-white/5 rounded-xl border border-white/20 p-8 flex items-center justify-center min-h-96">
              <div class="text-center">
                <p class="text-white/70 text-lg">Fill in the form and click "Generate Content"</p>
                <p class="text-white/50 text-sm mt-2">Your AI-generated content will appear here</p>
              </div>
            </div>

            <div *ngIf="isGenerating" class="backdrop-blur-xl bg-white/5 rounded-xl border border-white/20 p-8 flex items-center justify-center min-h-96">
              <div class="text-center">
                <div class="w-12 h-12 rounded-full border-4 border-purple-500/20 border-t-purple-500 animate-spin mx-auto mb-4"></div>
                <p class="text-white font-semibold">Generating your viral content...</p>
                <p class="text-white/50 text-sm mt-2">This usually takes 10-30 seconds</p>
              </div>
            </div>

            <div *ngIf="generatedContent && !isGenerating" class="space-y-6">
              <!-- Language Tabs (if available) -->
              <div *ngIf="multiLanguageResults" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 overflow-hidden">
                <div class="flex gap-2 p-4 border-b border-white/20 flex-wrap">
                  <button
                    *ngFor="let lang of getLanguageList()"
                    (click)="selectedLanguageTab = lang"
                    [class.active]="selectedLanguageTab === lang"
                    class="px-4 py-2 rounded-lg font-semibold transition text-sm"
                    [ngClass]="selectedLanguageTab === lang ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white' : 'bg-white/5 text-white/70 hover:bg-white/10'"
                  >
                    {{ getLanguageName(lang) }}
                  </button>
                </div>
              </div>

              <!-- Script Display (always show) -->
              <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
                <div class="flex justify-between items-center mb-4">
                  <h4 class="text-white font-semibold">Generated Script</h4>
                  <span *ngIf="multiLanguageResults" class="text-xs text-purple-300">{{ getLanguageName(selectedLanguageTab) }}</span>
                </div>
                <div class="text-white/90 text-sm whitespace-pre-wrap max-h-96 overflow-y-auto pr-4 bg-white/5 rounded-lg p-4 border border-white/10">
                  {{ multiLanguageResults?.translations[selectedLanguageTab] || generatedContent.scriptContent }}
                </div>
                <p *ngIf="isAdaptingLanguages" class="text-xs text-purple-300 mt-2">⏳ Adapting to other languages...</p>
              </div>

              <!-- Viral Score -->
              <div class="backdrop-blur-xl bg-gradient-to-br from-purple-500/20 to-pink-500/20 rounded-xl border border-purple-500/50 p-6">
                <h4 class="text-white font-semibold mb-4">AI Analysis</h4>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <p class="text-white/70 text-sm">Viral Score</p>
                    <p class="text-2xl font-bold text-purple-400">{{ generatedContent.viralScore | number:'1.1-2' }}/10</p>
                  </div>
                  <div>
                    <p class="text-white/70 text-sm">Confidence</p>
                    <p class="text-2xl font-bold text-pink-400">{{ generatedContent.confidenceScore | number:'1.1-2' }}%</p>
                  </div>
                </div>
              </div>

              <!-- Hashtags -->
              <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
                <h4 class="text-white font-semibold mb-4">Hashtags</h4>
                <div class="flex flex-wrap gap-2">
                  <span *ngFor="let tag of generatedContent.hashtags?.slice(0, 10)" class="px-3 py-1 rounded-full bg-purple-500/20 text-purple-300 text-sm">
                    {{ tag }}
                  </span>
                </div>
              </div>

              <!-- Recommended Posting Strategy -->
              <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
                <h4 class="text-white font-semibold mb-4">📅 Posting Strategy</h4>
                <div class="space-y-4">
                  <div>
                    <p class="text-white/70 text-sm mb-2">Best Posting Times</p>
                    <p class="text-white font-semibold">{{ generatedContent.bestPostingTime || 'Peak hours: 4-8 PM' }}</p>
                  </div>
                  <div>
                    <p class="text-white/70 text-sm mb-2">Posting Schedule</p>
                    <p class="text-white font-semibold">{{ generatedContent.postingSchedule || 'Post 3-5 times per week for consistency' }}</p>
                  </div>
                </div>
              </div>

              <!-- Content Style & Tone -->
              <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
                <h4 class="text-white font-semibold mb-4">🎬 Content Recommendations</h4>
                <div class="space-y-3">
                  <div>
                    <p class="text-white/70 text-sm">Recommended Tone</p>
                    <p class="text-white">{{ generatedContent.recommendedTone }}</p>
                  </div>
                  <div>
                    <p class="text-white/70 text-sm">Content Style</p>
                    <p class="text-white">{{ generatedContent.contentStyle }}</p>
                  </div>
                  <div *ngIf="generatedContent.engagementTriggers && generatedContent.engagementTriggers.length > 0">
                    <p class="text-white/70 text-sm mb-2">Engagement Triggers</p>
                    <div class="flex flex-wrap gap-2">
                      <span *ngFor="let trigger of generatedContent.engagementTriggers" class="px-3 py-1 rounded-full bg-blue-500/20 text-blue-300 text-xs">
                        {{ trigger }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Viral Hooks & CTA -->
              <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
                <h4 class="text-white font-semibold mb-4">🎣 Viral Elements</h4>
                <div class="space-y-4">
                  <div *ngIf="generatedContent.viralHooks && generatedContent.viralHooks.length > 0">
                    <p class="text-white/70 text-sm mb-2">Viral Hooks</p>
                    <ul class="space-y-2">
                      <li *ngFor="let hook of generatedContent.viralHooks" class="text-white text-sm flex items-start">
                        <span class="mr-2">✨</span>{{ hook }}
                      </li>
                    </ul>
                  </div>
                  <div>
                    <p class="text-white/70 text-sm mb-2">Recommended CTA</p>
                    <p class="text-white font-semibold">{{ generatedContent.recommendedCta }}</p>
                  </div>
                </div>
              </div>

              <!-- Trend Alignment -->
              <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
                <h4 class="text-white font-semibold mb-4">📈 Trend Analysis</h4>
                <p class="text-white">{{ generatedContent.trendAlignment }}</p>
              </div>

              <!-- Platform Optimization -->
              <div *ngIf="generatedContent.platformOptimization" class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
                <h4 class="text-white font-semibold mb-4">📱 Platform Optimization</h4>
                <p class="text-white/90 text-sm whitespace-pre-wrap">{{ generatedContent.platformOptimization }}</p>
              </div>

              <!-- AI Models Used -->
              <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-6">
                <h4 class="text-white font-semibold mb-4">AI Models Used</h4>
                <div class="space-y-2 text-sm">
                  <p class="text-white/70">Primary: <span class="text-green-400">{{ generatedContent.primaryModelUsed }}</span></p>
                  <p *ngIf="generatedContent.fallbackModelUsed" class="text-white/70">Fallback: <span class="text-yellow-400">{{ generatedContent.fallbackModelUsed }}</span></p>
                  <p class="text-white/70">Latency: <span class="text-blue-400">{{ generatedContent.generationLatencyMs }}ms</span></p>
                </div>
              </div>
            </div>

            <div *ngIf="error" class="backdrop-blur-xl bg-red-500/20 rounded-xl border border-red-500/50 p-6">
              <p class="text-red-200">{{ error }}</p>
            </div>
          </div>
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
export class GeneratorComponent implements OnInit {
  generatorForm: FormGroup;
  isGenerating = false;
  isAdaptingLanguages = false;
  error = '';
  generatedContent: any = null;
  multiLanguageResults: any = null;
  selectedLanguageTab = 'en';
  selectedLanguages: string[] = [];
  availableLanguages: { [key: string]: string } = {};

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService
  ) {
    this.generatorForm = this.fb.group({
      niche: ['', Validators.required],
      targetAudience: ['', Validators.required],
      vibe: [''],
      platform: ['', Validators.required],
      topicIdea: ['', Validators.required],
      contentType: ['', Validators.required],
      creatorGoal: ['']
    });
  }

  ngOnInit(): void {
    this.loadAvailableLanguages();
  }

  loadAvailableLanguages(): void {
    this.apiService.getAvailableLanguages().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.availableLanguages = response.data;
          // Select all languages by default
          this.selectedLanguages = Object.keys(this.availableLanguages);
        }
      },
      error: (error) => {
        console.error('Failed to load languages', error);
        // Fallback languages
        this.availableLanguages = {
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
        };
        this.selectedLanguages = Object.keys(this.availableLanguages);
      }
    });
  }

  toggleLanguage(langCode: string): void {
    const index = this.selectedLanguages.indexOf(langCode);
    if (index > -1) {
      this.selectedLanguages.splice(index, 1);
    } else {
      this.selectedLanguages.push(langCode);
    }
  }

  getLanguageList(): string[] {
    return Object.keys(this.availableLanguages).filter(lang =>
      this.multiLanguageResults?.translations[lang]
    );
  }

  getLanguageName(langCode: string): string {
    return this.availableLanguages[langCode] || langCode;
  }

  onGenerate(): void {
    if (this.generatorForm.invalid || this.selectedLanguages.length === 0) return;

    this.isGenerating = true;
    this.isAdaptingLanguages = false;
    this.error = '';
    this.generatedContent = null;
    this.multiLanguageResults = null;

    // First generate content in English
    this.apiService.generateContent(this.generatorForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.generatedContent = response.data;
          this.isGenerating = false;

          // Set up initial result with English content
          this.multiLanguageResults = {
            translations: { 'en': response.data.scriptContent }
          };
          this.selectedLanguageTab = 'en';

          // Then adapt to selected languages in background
          if (this.selectedLanguages.length > 1 || (this.selectedLanguages.length === 1 && this.selectedLanguages[0] !== 'en')) {
            this.adaptContentToLanguages();
          }
        } else {
          this.isGenerating = false;
        }
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to generate content. Please try again.';
        this.isGenerating = false;
      }
    });
  }

  adaptContentToLanguages(): void {
    this.isAdaptingLanguages = true;

    const adaptRequest = {
      scriptContent: this.generatedContent.scriptContent,
      topicIdea: this.generatorForm.value.topicIdea,
      platform: this.generatorForm.value.platform,
      niche: this.generatorForm.value.niche,
      targetAudience: this.generatorForm.value.targetAudience,
      languages: this.selectedLanguages
    };

    this.apiService.adaptContentToLanguages(adaptRequest).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.multiLanguageResults = response.data;
          this.selectedLanguageTab = 'en';
        }
        this.isAdaptingLanguages = false;
      },
      error: (error) => {
        console.error('Failed to adapt to languages', error);
        this.isAdaptingLanguages = false;
        // Still show English content even if adaptation fails
        if (!this.multiLanguageResults) {
          this.multiLanguageResults = {
            translations: { 'en': this.generatedContent.scriptContent }
          };
        }
      }
    });
  }
}
