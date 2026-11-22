import { inject, Injectable } from '@angular/core';
import { filter, firstValueFrom } from 'rxjs';
import { OAuthService } from 'angular-oauth2-oidc';
import { UPDATE_PASSWORD_ACTION } from 'app/core/auth/password-update';
import { environment } from 'environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly oauthService = inject(OAuthService);
  private loginStarted = false;

  async init(): Promise<void> {
    this.oauthService.configure({
      issuer: environment.oidcIssuer,
      redirectUri: environment.oidcRedirectUri,
      clientId: environment.oidcClientId,
      responseType: 'code',
      scope: 'openid profile email offline_access',
    });

    this.oauthService.setupAutomaticSilentRefresh();
    await this.oauthService.loadDiscoveryDocumentAndLogin();
    await this.whenAuthenticated();
  }

  async whenAuthenticated(): Promise<void> {
    if (this.oauthService.hasValidAccessToken()) {
      return;
    }

    await firstValueFrom(this.oauthService.events.pipe(filter((e) => e.type === 'token_received')));
  }

  hasValidAccessToken(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

  accessToken(): string {
    return this.oauthService.getAccessToken();
  }

  async refreshToken(): Promise<void> {
    await this.oauthService.refreshToken();
  }

  login(): void {
    if (this.loginStarted) {
      return;
    }

    this.loginStarted = true;
    this.oauthService.initLoginFlow();
  }

  logout(): void {
    this.oauthService.logOut();
  }

  changePassword(): void {
    this.oauthService.initCodeFlow('', { kc_action: UPDATE_PASSWORD_ACTION });
  }
}
