import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-profile',
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
      <div class="max-w-4xl mx-auto px-6 py-12">
        <h2 class="text-3xl font-bold text-white mb-8">Creator Profile</h2>
        <div class="backdrop-blur-xl bg-white/10 rounded-xl border border-white/20 p-8 space-y-6">
          <div>
            <label class="block text-white font-semibold mb-2">Full Name</label>
            <input type="text" [value]="user?.fullName" readonly class="w-full px-4 py-2 rounded-lg bg-white/5 border border-white/20 text-white/70" />
          </div>
          <div>
            <label class="block text-white font-semibold mb-2">Email</label>
            <input type="email" [value]="user?.email" readonly class="w-full px-4 py-2 rounded-lg bg-white/5 border border-white/20 text-white/70" />
          </div>
          <div>
            <label class="block text-white font-semibold mb-2">API Usage</label>
            <div class="w-full bg-white/5 rounded-lg border border-white/20 p-4">
              <div class="flex justify-between mb-2">
                <span class="text-white">{{ user?.apiUsageCount || 0 }} / {{ user?.maxMonthlyApiCalls || 1000 }} calls</span>
                <span class="text-white/50">{{ ((user?.apiUsageCount || 0) / (user?.maxMonthlyApiCalls || 1000) * 100) | number:'1.0-0' }}%</span>
              </div>
              <div class="w-full h-2 bg-white/10 rounded-full overflow-hidden">
                <div class="h-full bg-gradient-to-r from-purple-500 to-pink-500"
                     [style.width.%]="((user?.apiUsageCount || 0) / (user?.maxMonthlyApiCalls || 1000) * 100)"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ProfileComponent implements OnInit {
  user: any;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();
  }
}
