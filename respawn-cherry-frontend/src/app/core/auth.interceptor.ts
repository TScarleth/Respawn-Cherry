import { HttpInterceptorFn } from '@angular/common/http';
import { readAuthToken } from './auth-session';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = readAuthToken();
  if (!token) return next(req);
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
