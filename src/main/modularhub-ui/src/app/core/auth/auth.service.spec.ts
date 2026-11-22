import { TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { OAuthEvent, OAuthInfoEvent, OAuthService, OAuthSuccessEvent } from 'angular-oauth2-oidc';
import { AuthService } from 'app/core/auth/auth.service';
import { environment } from 'environments/environment';

describe('AuthService', () => {
  let events: Subject<OAuthEvent>;
  let oauthService: {
    configure: ReturnType<typeof vi.fn>;
    setupAutomaticSilentRefresh: ReturnType<typeof vi.fn>;
    loadDiscoveryDocumentAndLogin: ReturnType<typeof vi.fn>;
    hasValidAccessToken: ReturnType<typeof vi.fn>;
    getAccessToken: ReturnType<typeof vi.fn>;
    refreshToken: ReturnType<typeof vi.fn>;
    initLoginFlow: ReturnType<typeof vi.fn>;
    logOut: ReturnType<typeof vi.fn>;
    initCodeFlow: ReturnType<typeof vi.fn>;
    events: Subject<OAuthEvent>;
  };
  let service: AuthService;

  beforeEach(() => {
    events = new Subject<OAuthEvent>();
    oauthService = {
      configure: vi.fn(),
      setupAutomaticSilentRefresh: vi.fn(),
      loadDiscoveryDocumentAndLogin: vi.fn().mockResolvedValue(true),
      hasValidAccessToken: vi.fn().mockReturnValue(true),
      getAccessToken: vi.fn().mockReturnValue('token-value'),
      refreshToken: vi.fn().mockResolvedValue({}),
      initLoginFlow: vi.fn(),
      logOut: vi.fn(),
      initCodeFlow: vi.fn(),
      events,
    };

    TestBed.configureTestingModule({
      providers: [{ provide: OAuthService, useValue: oauthService }],
    });
    service = TestBed.inject(AuthService);
  });

  async function isSettled(promise: Promise<void>): Promise<boolean> {
    let settled = false;
    promise.then(() => (settled = true));
    await new Promise((resolve) => setTimeout(resolve));
    return settled;
  }

  describe('init', () => {
    it('should configure the oauth client and log in', async () => {
      await service.init();

      expect(oauthService.configure).toHaveBeenCalledWith({
        issuer: environment.oidcIssuer,
        redirectUri: environment.oidcRedirectUri,
        clientId: environment.oidcClientId,
        responseType: 'code',
        scope: 'openid profile email offline_access',
      });
      expect(oauthService.setupAutomaticSilentRefresh).toHaveBeenCalledOnce();
      expect(oauthService.loadDiscoveryDocumentAndLogin).toHaveBeenCalledOnce();
      expect(oauthService.configure.mock.invocationCallOrder[0]).toBeLessThan(
        oauthService.loadDiscoveryDocumentAndLogin.mock.invocationCallOrder[0],
      );
    });

    it('should wait for the token when the login does not provide a valid one', async () => {
      oauthService.hasValidAccessToken.mockReturnValue(false);

      const init = service.init();

      expect(await isSettled(init)).toBe(false);
      events.next(new OAuthSuccessEvent('token_received'));
      expect(await isSettled(init)).toBe(true);
    });
  });

  describe('whenAuthenticated', () => {
    it('should resolve immediately when the access token is already valid', async () => {
      expect(await isSettled(service.whenAuthenticated())).toBe(true);
    });

    it('should wait for the token to be received when there is no valid access token', async () => {
      oauthService.hasValidAccessToken.mockReturnValue(false);

      const waiting = service.whenAuthenticated();
      events.next(new OAuthInfoEvent('discovery_document_loaded'));
      expect(await isSettled(waiting)).toBe(false);

      events.next(new OAuthSuccessEvent('token_received'));
      expect(await isSettled(waiting)).toBe(true);
    });
  });

  describe('hasValidAccessToken', () => {
    it('should tell whether the access token is valid', () => {
      oauthService.hasValidAccessToken.mockReturnValue(false);

      expect(service.hasValidAccessToken()).toBe(false);
      expect(oauthService.hasValidAccessToken).toHaveBeenCalledOnce();
    });
  });

  describe('accessToken', () => {
    it('should return the current access token', () => {
      expect(service.accessToken()).toBe('token-value');
      expect(oauthService.getAccessToken).toHaveBeenCalledOnce();
    });
  });

  describe('refreshToken', () => {
    it('should refresh the token', async () => {
      await service.refreshToken();

      expect(oauthService.refreshToken).toHaveBeenCalledOnce();
    });

    it('should propagate the refresh failure', async () => {
      oauthService.refreshToken.mockRejectedValue(new Error('refresh failed'));

      await expect(service.refreshToken()).rejects.toThrow('refresh failed');
    });
  });

  describe('login', () => {
    it('should start the login flow', () => {
      service.login();

      expect(oauthService.initLoginFlow).toHaveBeenCalledOnce();
    });

    it('should start the login flow only once', () => {
      service.login();
      service.login();

      expect(oauthService.initLoginFlow).toHaveBeenCalledOnce();
    });
  });

  describe('logout', () => {
    it('should log out from the identity provider', () => {
      service.logout();

      expect(oauthService.logOut).toHaveBeenCalledOnce();
    });
  });

  describe('changePassword', () => {
    it('should ask the identity provider to update the password', () => {
      service.changePassword();

      expect(oauthService.initCodeFlow).toHaveBeenCalledWith('', { kc_action: 'UPDATE_PASSWORD' });
    });
  });
});
