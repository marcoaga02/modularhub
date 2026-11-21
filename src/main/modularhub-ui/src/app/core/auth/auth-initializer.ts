import { EnvironmentProviders, inject, provideAppInitializer } from '@angular/core';
import { filter, firstValueFrom } from 'rxjs';
import { OAuthService } from 'angular-oauth2-oidc';
import { AccountService } from 'app/core/api/services/account.service';
import { environment } from 'environments/environment';

export function provideAuthInitializer(): EnvironmentProviders {
  return provideAppInitializer(async () => {
    const oAuthService = inject(OAuthService);
    const accountService = inject(AccountService);

    oAuthService.configure({
      issuer: environment.oidcIssuer,
      redirectUri: environment.oidcRedirectUri,
      clientId: environment.oidcClientId,
      responseType: 'code',
      scope: 'openid profile email offline_access',
    });

    oAuthService.setupAutomaticSilentRefresh();
    await oAuthService.loadDiscoveryDocumentAndLogin();
    await waitForValidToken(oAuthService);

    accountService.load();
  });
}

export async function waitForValidToken(
  oAuthService: Pick<OAuthService, 'hasValidAccessToken' | 'events'>,
): Promise<void> {
  if (oAuthService.hasValidAccessToken()) {
    return;
  }

  await firstValueFrom(oAuthService.events.pipe(filter((e) => e.type === 'token_received')));
}
