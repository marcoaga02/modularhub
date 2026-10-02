import {TestBed} from '@angular/core/testing';

import {AccountService} from 'app/core/api/services/account.service';
import {HttpClient} from '@angular/common/http';
import {TranslateService} from '@ngx-translate/core';
import {OAuthService} from 'angular-oauth2-oidc';

describe('AccountService', () => {
  let service: AccountService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AccountService,
        { provide: TranslateService, useValue: {} },
        { provide: HttpClient, useValue: {} },
        { provide: OAuthService, useValue: {} },
      ]
    });
    service = TestBed.inject(AccountService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
