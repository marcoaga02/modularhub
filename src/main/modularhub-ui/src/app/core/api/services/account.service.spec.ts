import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { AccountService } from 'app/core/api/services/account.service';
import {
  Account,
  AccountApiService,
  AccountPreferencesUpdate,
  Language,
} from 'app/core/api/generated';

describe('AccountService', () => {
  const account: Account = {
    identityId: 'identity-id',
    email: 'mario@test.it',
    username: 'mario',
    firstName: 'Mario',
    lastName: 'Rossi',
    permissions: ['USER_MANAGEMENT'],
    preferences: { language: Language.It },
  };

  let accountApi: {
    getCurrentAccount: ReturnType<typeof vi.fn>;
    updatePreferences: ReturnType<typeof vi.fn>;
  };
  let translateService: { use: ReturnType<typeof vi.fn> };
  let oauthService: {
    hasValidAccessToken: ReturnType<typeof vi.fn>;
    logOut: ReturnType<typeof vi.fn>;
    initCodeFlow: ReturnType<typeof vi.fn>;
    initLoginFlow: ReturnType<typeof vi.fn>;
    refreshToken: ReturnType<typeof vi.fn>;
  };
  let service: AccountService;

  beforeEach(() => {
    accountApi = {
      getCurrentAccount: vi.fn().mockReturnValue(of(account)),
      updatePreferences: vi.fn(),
    };
    translateService = { use: vi.fn() };
    oauthService = {
      hasValidAccessToken: vi.fn().mockReturnValue(true),
      logOut: vi.fn(),
      initCodeFlow: vi.fn(),
      initLoginFlow: vi.fn(),
      refreshToken: vi.fn().mockResolvedValue({}),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AccountApiService, useValue: accountApi },
        { provide: TranslateService, useValue: translateService },
        { provide: OAuthService, useValue: oauthService },
      ],
    });
    service = TestBed.inject(AccountService);
  });

  describe('load', () => {
    it('should publish the account and use its language', async () => {
      service.load();
      await service.whenLoaded();

      expect(service.account()).toEqual(account);
      expect(translateService.use).toHaveBeenCalledWith(Language.It);
    });

    it('should complete the loading with no account when the request fails', async () => {
      accountApi.getCurrentAccount.mockReturnValue(throwError(() => new Error('boom')));

      service.load();
      await service.whenLoaded();

      expect(service.account()).toBeNull();
      expect(translateService.use).not.toHaveBeenCalled();
    });

    it('should complete the loading without requesting the account when there is no valid token', async () => {
      oauthService.hasValidAccessToken.mockReturnValue(false);

      service.load();
      await service.whenLoaded();

      expect(accountApi.getCurrentAccount).not.toHaveBeenCalled();
      expect(service.account()).toBeNull();
    });
  });

  describe('reload', () => {
    it('should refresh the token and then load the account again', async () => {
      await service.reload();

      expect(oauthService.refreshToken).toHaveBeenCalledOnce();
      expect(accountApi.getCurrentAccount).toHaveBeenCalledOnce();
      expect(oauthService.refreshToken.mock.invocationCallOrder[0]).toBeLessThan(
        accountApi.getCurrentAccount.mock.invocationCallOrder[0],
      );
      expect(service.account()).toEqual(account);
    });

    it('should load the account even when the token refresh fails', async () => {
      oauthService.refreshToken.mockRejectedValue(new Error('refresh failed'));

      await service.reload();

      expect(accountApi.getCurrentAccount).toHaveBeenCalledOnce();
      expect(service.account()).toEqual(account);
    });
  });

  describe('login', () => {
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

  describe('updateCurrentAccountPreferences', () => {
    const preferences: AccountPreferencesUpdate = { language: Language.En };

    it('should send the preferences to the api and return the saved ones', async () => {
      accountApi.updatePreferences.mockReturnValue(of(preferences));

      const result = await firstValueFrom(service.updateCurrentAccountPreferences(preferences));

      expect(accountApi.updatePreferences).toHaveBeenCalledWith(preferences);
      expect(result).toEqual(preferences);
    });

    it('should update the account and use the saved language', async () => {
      service.load();
      await service.whenLoaded();
      accountApi.updatePreferences.mockReturnValue(of(preferences));

      await firstValueFrom(service.updateCurrentAccountPreferences(preferences));

      expect(service.account()).toEqual({ ...account, preferences });
      expect(translateService.use).toHaveBeenLastCalledWith(Language.En);
    });

    it('should keep no account when the preferences are saved before the account is loaded', async () => {
      accountApi.updatePreferences.mockReturnValue(of(preferences));

      await firstValueFrom(service.updateCurrentAccountPreferences(preferences));

      expect(service.account()).toBeNull();
      expect(translateService.use).toHaveBeenCalledWith(Language.En);
    });

    it('should leave account and language unchanged when the save fails', async () => {
      service.load();
      await service.whenLoaded();
      translateService.use.mockClear();
      accountApi.updatePreferences.mockReturnValue(throwError(() => new Error('boom')));

      await expect(
        firstValueFrom(service.updateCurrentAccountPreferences(preferences)),
      ).rejects.toThrow('boom');

      expect(service.account()).toEqual(account);
      expect(translateService.use).not.toHaveBeenCalled();
    });
  });
});
