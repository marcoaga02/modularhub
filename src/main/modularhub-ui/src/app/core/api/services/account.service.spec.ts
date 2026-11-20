import {TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';

import {AccountService} from 'app/core/api/services/account.service';
import {Account, AccountApiService, Language} from 'app/core/api/generated';
import {TranslateService} from '@ngx-translate/core';
import {OAuthService} from 'angular-oauth2-oidc';

describe('AccountService', () => {
  const account: Account = {
    identityId: 'identity-id',
    email: 'mario@test.it',
    username: 'mario',
    firstName: 'Mario',
    lastName: 'Rossi',
    permissions: ['USER_MANAGEMENT'],
    preferences: {language: Language.It},
  };

  let usedLanguages: string[];

  function setUp(getCurrentAccount: () => unknown, hasValidAccessToken = true) {
    usedLanguages = [];
    TestBed.configureTestingModule({
      providers: [
        AccountService,
        {provide: TranslateService, useValue: {use: (lang: string) => usedLanguages.push(lang)}},
        {provide: AccountApiService, useValue: {getCurrentAccount}},
        {provide: OAuthService, useValue: {hasValidAccessToken: () => hasValidAccessToken}},
      ]
    });
    return TestBed.inject(AccountService);
  }

  it('should be created', () => {
    expect(setUp(() => of(account))).toBeTruthy();
  });

  it('should publish the account and use its language when it is loaded', async () => {
    const service = setUp(() => of(account));
    let published: Account | null = null;
    service.getObservableAccount().subscribe(value => published = value);

    service.load();
    await service.whenLoaded();

    expect(published).toEqual(account);
    expect(usedLanguages).toEqual([Language.It]);
  });

  it('should complete the loading with no account when the request fails', async () => {
    const service = setUp(() => throwError(() => new Error('boom')));
    let published: Account | null | undefined;
    service.getObservableAccount().subscribe(value => published = value);

    service.load();
    await service.whenLoaded();

    expect(published).toBeNull();
  });

  it('should complete the loading without requesting the account when there is no valid token', async () => {
    let requested = false;
    const service = setUp(() => {
      requested = true;
      return of(account);
    }, false);

    service.load();
    await service.whenLoaded();

    expect(requested).toBe(false);
  });
});
