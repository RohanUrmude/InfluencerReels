import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const appRoutes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'generator',
    canActivate: [authGuard],
    loadComponent: () => import('./features/generator/generator.component').then(m => m.GeneratorComponent)
  },
  {
    path: 'analytics',
    canActivate: [authGuard],
    loadComponent: () => import('./features/analytics/analytics.component').then(m => m.AnalyticsComponent)
  },
  {
    path: 'history',
    canActivate: [authGuard],
    loadComponent: () => import('./features/history/history.component').then(m => m.HistoryComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: 'trending',
    canActivate: [authGuard],
    loadComponent: () => import('./features/trending/trending.component').then(m => m.TrendingComponent)
  },
  {
    path: 'models',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/model-validation-dashboard.component').then(m => m.ModelValidationDashboardComponent)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
