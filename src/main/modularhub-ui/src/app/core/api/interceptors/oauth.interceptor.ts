import { inject } from '@angular/core';
import { HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from 'app/core/auth/auth.service';

export function oAuthInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const token = inject(AuthService).accessToken();
  const headers = req.headers.set('Authorization', 'Bearer ' + token);

  return next(req.clone({ headers }));
}
