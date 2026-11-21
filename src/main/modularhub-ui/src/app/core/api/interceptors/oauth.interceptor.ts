import { inject } from '@angular/core';
import { HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OAuthStorage } from 'angular-oauth2-oidc';

export function oAuthInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const token = inject(OAuthStorage).getItem('access_token');
  const headers = req.headers.set('Authorization', 'Bearer ' + token);

  return next(req.clone({ headers }));
}
