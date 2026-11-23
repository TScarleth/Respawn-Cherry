import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ForumFacade } from './forum.facade';

export const authGuard: CanActivateFn = () => {
  const facade = inject(ForumFacade);
  const router = inject(Router);
  return facade.currentUser() ? true : router.createUrlTree(['/login']);
};

export const guestGuard: CanActivateFn = () => {
  const facade = inject(ForumFacade);
  const router = inject(Router);
  return facade.currentUser() ? router.createUrlTree(['/forum']) : true;
};

export const adminGuard: CanActivateFn = () => {
  const facade = inject(ForumFacade);
  const router = inject(Router);
  if (!facade.currentUser()) return router.createUrlTree(['/login']);
  return facade.isAdmin() ? true : router.createUrlTree(['/forum']);
};
