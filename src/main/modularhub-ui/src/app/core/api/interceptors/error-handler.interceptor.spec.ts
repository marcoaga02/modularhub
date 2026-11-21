import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  HttpErrorResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { MessageService, ToastMessageOptions } from 'primeng/api';
import {
  DEFAULT_ERROR_CODE,
  errorHandlerInterceptor,
} from 'app/core/api/interceptors/error-handler.interceptor';
import { ApiError } from 'app/core/api/generated';

describe('errorHandlerInterceptor', () => {
  const url = 'http://test/api/resource';
  const badRequest = { status: 400, statusText: 'Bad Request' };

  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let messages: ToastMessageOptions[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorHandlerInterceptor])),
        provideHttpClientTesting(),
        MessageService,
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);

    messages = [];
    TestBed.inject(MessageService).messageObserver.subscribe((message) => {
      messages.push(...(Array.isArray(message) ? message : [message]));
    });
  });

  afterEach(() => {
    httpTesting.verify();
  });

  function failWith(body: ApiError | null, options = badRequest): Promise<unknown> {
    const result = firstValueFrom(http.get(url)).catch((error: unknown) => error);
    httpTesting.expectOne(url).flush(body, options);
    return result;
  }

  it('should show the error code of the response with the invalid fields', async () => {
    await failWith({
      timestamp: '2026-01-01T00:00:00Z',
      errorCode: 'exception.validation',
      params: { fields: ['email', 'username'] },
    });

    expect(messages).toEqual([
      {
        severity: 'error',
        summary: 'general.error',
        detail: 'exception.validation',
        data: { fields: 'email, username' },
      },
    ]);
  });

  it('should show no invalid fields when the field list is empty', async () => {
    await failWith({
      timestamp: '2026-01-01T00:00:00Z',
      errorCode: 'exception.validation',
      params: { fields: [] },
    });

    expect(messages[0].data).toEqual({ fields: undefined });
  });

  it('should show the default error code when the response has no body', async () => {
    await failWith(null, { status: 500, statusText: 'Server Error' });

    expect(messages[0].detail).toBe(DEFAULT_ERROR_CODE);
    expect(messages[0].data).toEqual({ fields: undefined });
  });

  it('should rethrow the error to the caller', async () => {
    const error = await failWith(null, { status: 503, statusText: 'Service Unavailable' });

    expect(error).toBeInstanceOf(HttpErrorResponse);
    expect((error as HttpErrorResponse).status).toBe(503);
  });

  it('should not show any message when the request succeeds', async () => {
    const result = firstValueFrom(http.get(url));
    httpTesting.expectOne(url).flush({});
    await result;

    expect(messages).toEqual([]);
  });
});
