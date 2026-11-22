import { ApplicationInitStatus } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideAuthInitializer } from 'app/core/auth/auth-initializer';
import { AccountService } from 'app/core/api/services/account.service';
import { AuthService } from 'app/core/auth/auth.service';

describe('provideAuthInitializer', () => {
  it('should initialize the authentication and then load the account', async () => {
    const authService = { init: vi.fn().mockResolvedValue(undefined) };
    const accountService = { load: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        provideAuthInitializer(),
        { provide: AuthService, useValue: authService },
        { provide: AccountService, useValue: accountService },
      ],
    });

    await TestBed.inject(ApplicationInitStatus).donePromise;

    expect(authService.init).toHaveBeenCalledOnce();
    expect(accountService.load).toHaveBeenCalledOnce();
    expect(authService.init.mock.invocationCallOrder[0]).toBeLessThan(
      accountService.load.mock.invocationCallOrder[0],
    );
  });
});
