import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MemoryStorage, OAuthStorage } from 'angular-oauth2-oidc';
import { oAuthInterceptor } from 'app/core/api/interceptors/oauth.interceptor';

describe('oAuthInterceptor', () => {
  const url = 'http://test/api/resource';

  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let storage: MemoryStorage;

  beforeEach(() => {
    storage = new MemoryStorage();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([oAuthInterceptor])),
        provideHttpClientTesting(),
        { provide: OAuthStorage, useValue: storage },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should send the stored access token as bearer authorization', () => {
    storage.setItem('access_token', 'token-value');

    http.get(url).subscribe();

    const request = httpTesting.expectOne(url);
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-value');
    request.flush({});
  });

  it('should keep the headers of the original request', () => {
    storage.setItem('access_token', 'token-value');

    http.get(url, { headers: { 'X-Custom': 'value' } }).subscribe();

    const request = httpTesting.expectOne(url);
    expect(request.request.headers.get('X-Custom')).toBe('value');
    request.flush({});
  });
});
