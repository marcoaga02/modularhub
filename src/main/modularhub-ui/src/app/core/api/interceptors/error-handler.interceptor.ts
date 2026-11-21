import { inject } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { MessageService } from 'primeng/api';
import { ApiError } from 'app/core/api/generated';

export const DEFAULT_ERROR_CODE = 'exception.internal.error';

export function errorHandlerInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((e: HttpErrorResponse) => {
      const error = e.error as ApiError | null;
      const errorCode = error?.errorCode ?? DEFAULT_ERROR_CODE;
      const invalidFields: unknown = error?.params?.['fields'];
      const fields =
        Array.isArray(invalidFields) && invalidFields.length ? invalidFields.join(', ') : undefined;

      messageService.add({
        severity: 'error',
        summary: 'general.error',
        detail: errorCode,
        data: { fields },
      });
      return throwError(() => e);
    }),
  );
}
