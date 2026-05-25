import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center p-4">
      <div class="w-full max-w-md">
        <div class="backdrop-blur-xl bg-white/10 rounded-2xl border border-white/20 p-8 shadow-2xl">
          <h1 class="text-3xl font-bold text-white mb-2 text-center">Join ViralForge</h1>
          <p class="text-purple-200 text-center mb-8">Create your creator account</p>

          <form [formGroup]="registerForm" (ngSubmit)="onRegister()" class="space-y-4">
            <div>
              <label class="block text-white text-sm font-medium mb-2">Full Name</label>
              <input
                type="text"
                formControlName="fullName"
                class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white placeholder-white/50 focus:outline-none focus:border-purple-500"
                placeholder="Your Name"
              />
            </div>

            <div>
              <label class="block text-white text-sm font-medium mb-2">Username</label>
              <input
                type="text"
                formControlName="username"
                class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white placeholder-white/50 focus:outline-none focus:border-purple-500"
                placeholder="creator_username"
              />
            </div>

            <div>
              <label class="block text-white text-sm font-medium mb-2">Email</label>
              <input
                type="email"
                formControlName="email"
                class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white placeholder-white/50 focus:outline-none focus:border-purple-500"
                placeholder="your@email.com"
              />
            </div>

            <div>
              <label class="block text-white text-sm font-medium mb-2">Password</label>
              <input
                type="password"
                formControlName="password"
                class="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/20 text-white placeholder-white/50 focus:outline-none focus:border-purple-500"
                placeholder="••••••••"
              />
              <p class="text-xs text-purple-300 mt-1">Min 8 chars: uppercase, lowercase, number, special char</p>
            </div>

            <button
              type="submit"
              [disabled]="isLoading"
              class="w-full py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:shadow-lg transition disabled:opacity-50"
            >
              {{ isLoading ? 'Creating account...' : 'Sign Up' }}
            </button>
          </form>

          <div class="mt-6 text-center">
            <p class="text-white/70">Already have an account?
              <a routerLink="/login" class="text-purple-400 hover:text-purple-300 font-semibold">Login</a>
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
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', Validators.required],
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)
      ]]
    });
  }

  onRegister(): void {
    if (this.registerForm.invalid) return;

    this.isLoading = true;
    this.error = '';

    this.authService.register(this.registerForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.authService.storeToken(response.data.token, response.data);
          this.router.navigate(['/dashboard']);
        }
      },
      error: (error) => {
        this.error = error.error?.message || 'Registration failed. Please try again.';
        this.isLoading = false;
      }
    });
  }
}
