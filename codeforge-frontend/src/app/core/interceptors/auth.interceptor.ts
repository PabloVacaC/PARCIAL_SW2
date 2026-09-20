import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from '../../auth/services/auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(Auth);

  const token = auth.obtenerToken();

  if (!token) {
    return next(req);
  }

  const requestWithToken = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(requestWithToken);
};