import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from 'app/core/auth/auth.service';
import { oAuthInterceptor } from 'app/core/api/interceptors/oauth.interceptor';

describe('oAuthInterceptor', () => {
  const url = 'http://test/api/resource';

  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let authService: { accessToken: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authService = { accessToken: vi.fn().mockReturnValue('token-value') };
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([oAuthInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should send the access token as bearer authorization', () => {
    http.get(url).subscribe();

    const request = httpTesting.expectOne(url);
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-value');
    expect(authService.accessToken).toHaveBeenCalledOnce();
    request.flush({});
  });

  it('should keep the headers of the original request', () => {
    http.get(url, { headers: { 'X-Custom': 'value' } }).subscribe();

    const request = httpTesting.expectOne(url);
    expect(request.request.headers.get('X-Custom')).toBe('value');
    request.flush({});
  });
});
