import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { NavigationStart, provideRouter, Router, withViewTransitions } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth.interceptor';

const authPaths = ['/login', '/register', '/recover', '/restablecer'];
let enterFromAuth = false;

function isAuthUrl(url: string): boolean {
  const path = url.split('?')[0].split('#')[0];
  return authPaths.some((item) => path === item || path.startsWith(`${item}/`));
}

function pinForumTop(): void {
  window.scrollTo(0, 0);
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withViewTransitions({
      skipInitialTransition: true,
      onViewTransitionCreated: ({ transition }) => {
        if (!enterFromAuth) return;
        queueMicrotask(pinForumTop);
        requestAnimationFrame(pinForumTop);
        void transition.finished.finally(pinForumTop);
      }
    })),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAppInitializer(() => {
      const router = inject(Router);
      router.events.subscribe((event) => {
        if (!(event instanceof NavigationStart)) return;
        const fromAuth = isAuthUrl(router.url);
        const toAuth = isAuthUrl(event.url);
        enterFromAuth = fromAuth && !toAuth;
        document.documentElement.classList.toggle('vt-auth', fromAuth || toAuth);
        if (enterFromAuth) pinForumTop();
      });
    })
  ]
};
