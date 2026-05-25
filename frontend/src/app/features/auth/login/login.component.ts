import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center p-4">
      <div class="w-full max-w-md">
        <div class="backdrop-blur-xl bg-white/10 rounded-2xl border border-white/20 p-8 shadow-2xl">
          <h1 class="text-3xl font-bold text-white mb-2 text-center">ViralForge AI</h1>
          <p class="text-purple-200 text-center mb-8">Generate viral content with AI</p>

          <form [formGroup]="loginForm" (ngSubmit)="onLogin()" class="space-y-4">
            <div>
              <label class="block text-white text-sm font-medium mb-2">Email</label>
              <input
                type="email"
                formControlName="email"
                class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white placeholder-white/50 focus:outline-none focus:border-purple-500 focus:bg-white/10 transition"
                placeholder="your@email.com"
              />
            </div>

            <div>
              <label class="block text-white text-sm font-medium mb-2">Password</label>
              <input
                type="password"
                formControlName="password"
                class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white placeholder-white/50 focus:outline-none focus:border-purple-500 focus:bg-white/10 transition"
                placeholder="••••••••"
              />
            </div>

            <button
              type="submit"
              [disabled]="isLoading"
              class="w-full py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:shadow-lg hover:shadow-purple-500/50 transition disabled:opacity-50"
            >
              {{ isLoading ? 'Logging in...' : 'Login' }}
            </button>
          </form>

          <div class="mt-6 text-center">
            <p class="text-white/70">Don't have an account?
              <a routerLink="/register" class="text-purple-400 hover:text-purple-300 font-semibold">Sign up</a>
            </p>
          </div>

          <div *ngIf="error" class="mt-4 p-4 bg-red-500/20 border border-red-500/50 rounded-lg">
            <p class="text-red-200 text-sm">{{ error }}</p>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onLogin(): void {
    if (this.loginForm.invalid) return;

    this.isLoading = true;
    this.error = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.authService.storeToken(response.data.token, response.data);
          this.router.navigate(['/dashboard']);
        }
      },
      error: (error) => {
        this.error = error.error?.message || 'Login failed. Please try again.';
        this.isLoading = false;
      }
    });
  }
}
