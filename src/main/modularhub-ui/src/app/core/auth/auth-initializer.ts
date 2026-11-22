import { EnvironmentProviders, inject, provideAppInitializer } from '@angular/core';
import { AccountService } from 'app/core/api/services/account.service';
import { AuthService } from 'app/core/auth/auth.service';

export function provideAuthInitializer(): EnvironmentProviders {
  return provideAppInitializer(async () => {
    const authService = inject(AuthService);
    const accountService = inject(AccountService);

    await authService.init();

    accountService.load();
  });
}
