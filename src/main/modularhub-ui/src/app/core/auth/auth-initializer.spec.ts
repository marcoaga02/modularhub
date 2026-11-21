import { ApplicationInitStatus } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { OAuthEvent, OAuthInfoEvent, OAuthService, OAuthSuccessEvent } from 'angular-oauth2-oidc';
import { provideAuthInitializer, waitForValidToken } from 'app/core/auth/auth-initializer';
import { AccountService } from 'app/core/api/services/account.service';
import { environment } from 'environments/environment';

describe('auth initializer', () => {
  let events: Subject<OAuthEvent>;

  beforeEach(() => {
    events = new Subject<OAuthEvent>();
  });

  function oAuthServiceStub(hasValidAccessToken: boolean) {
    return {
      configure: vi.fn(),
      setupAutomaticSilentRefresh: vi.fn(),
      loadDiscoveryDocumentAndLogin: vi.fn().mockResolvedValue(true),
      hasValidAccessToken: vi.fn().mockReturnValue(hasValidAccessToken),
      events: events.asObservable(),
    };
  }

  async function isSettled(promise: Promise<void>): Promise<boolean> {
    let settled = false;
    promise.then(() => (settled = true));
    await new Promise((resolve) => setTimeout(resolve));
    return settled;
  }

  describe('waitForValidToken', () => {
    it('should resolve immediately when the access token is already valid', async () => {
      const oAuthService = oAuthServiceStub(true);

      expect(await isSettled(waitForValidToken(oAuthService))).toBe(true);
    });

    it('should wait for the token to be received when there is no valid access token', async () => {
      const oAuthService = oAuthServiceStub(false);
      const waiting = waitForValidToken(oAuthService);

      events.next(new OAuthInfoEvent('discovery_document_loaded'));
      expect(await isSettled(waiting)).toBe(false);

      events.next(new OAuthSuccessEvent('token_received'));
      expect(await isSettled(waiting)).toBe(true);
    });
  });

  describe('provideAuthInitializer', () => {
    it('should configure the oauth client, log in and then load the account', async () => {
      const oAuthService = oAuthServiceStub(true);
      const accountService = { load: vi.fn() };

      TestBed.configureTestingModule({
        providers: [
          provideAuthInitializer(),
          { provide: OAuthService, useValue: oAuthService },
          { provide: AccountService, useValue: accountService },
        ],
      });
      await TestBed.inject(ApplicationInitStatus).donePromise;

      expect(oAuthService.configure).toHaveBeenCalledWith({
        issuer: environment.oidcIssuer,
        redirectUri: environment.oidcRedirectUri,
        clientId: environment.oidcClientId,
        responseType: 'code',
        scope: 'openid profile email offline_access',
      });
      expect(oAuthService.setupAutomaticSilentRefresh).toHaveBeenCalled();
      expect(oAuthService.loadDiscoveryDocumentAndLogin).toHaveBeenCalled();
      expect(accountService.load).toHaveBeenCalledOnce();
      expect(oAuthService.loadDiscoveryDocumentAndLogin.mock.invocationCallOrder[0]).toBeLessThan(
        accountService.load.mock.invocationCallOrder[0],
      );
    });
  });
});
