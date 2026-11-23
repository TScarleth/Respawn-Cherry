import { Routes } from '@angular/router';
import { authGuard, guestGuard, adminGuard } from './core/route-guards';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'forum' },
  { path: 'login', canActivate: [guestGuard], loadComponent: () => import('./pages/auth.page').then((m) => m.AuthPage) },
  { path: 'register', canActivate: [guestGuard], loadComponent: () => import('./pages/auth.page').then((m) => m.AuthPage) },
  { path: 'recover', canActivate: [guestGuard], loadComponent: () => import('./pages/auth.page').then((m) => m.AuthPage) },
  { path: 'restablecer', canActivate: [guestGuard], loadComponent: () => import('./pages/auth.page').then((m) => m.AuthPage) },
  { path: 'forum', canActivate: [authGuard], loadComponent: () => import('./pages/forum.page').then((m) => m.ForumPage) },
  { path: 'categories', canActivate: [authGuard], loadComponent: () => import('./pages/categories.page').then((m) => m.CategoriesPage) },
  { path: 'nosotras', canActivate: [authGuard], loadComponent: () => import('./pages/about.page').then((m) => m.AboutPage) },
  { path: 'posts/:id', canActivate: [authGuard], loadComponent: () => import('./pages/post.page').then((m) => m.PostPage) },
  { path: 'new-post', canActivate: [authGuard], loadComponent: () => import('./pages/create-post.page').then((m) => m.CreatePostPage) },
  { path: 'profile', canActivate: [authGuard], loadComponent: () => import('./pages/profile.page').then((m) => m.ProfilePage) },
  { path: 'admin', canActivate: [adminGuard], loadComponent: () => import('./pages/admin.page').then((m) => m.AdminPage) },
  { path: 'denuncias', canActivate: [adminGuard], loadComponent: () => import('./pages/reports.page').then((m) => m.ReportsPage) },
  { path: '**', redirectTo: 'forum' }
];
