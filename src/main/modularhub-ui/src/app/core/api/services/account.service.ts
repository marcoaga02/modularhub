import { inject, Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { firstValueFrom, ReplaySubject, tap } from 'rxjs';
import {
  Account,
  AccountApiService,
  AccountPreferencesResponse,
  AccountPreferencesUpdate,
} from 'app/core/api/generated';
import { UPDATE_PASSWORD_ACTION } from 'app/core/auth/password-update';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly accountApi = inject(AccountApiService);
  private readonly translateService = inject(TranslateService);
  private readonly oauthService = inject(OAuthService);

  private readonly currentAccount = signal<Account | null>(null);
  readonly account = this.currentAccount.asReadonly();
  private readonly loaded = new ReplaySubject<void>(1);

  load(): void {
    if (!this.oauthService.hasValidAccessToken()) {
      this.loaded.next();
      return;
    }

    this.accountApi.getCurrentAccount().subscribe({
      next: (account) => this.onSuccess(account),
      error: () => this.onError(),
    });
  }

  private onSuccess(account: Account) {
    this.translateService.use(account.preferences.language);
    this.currentAccount.set(account);
    this.loaded.next();
  }

  private onError() {
    this.currentAccount.set(null);
    this.loaded.next();
  }

  whenLoaded(): Promise<void> {
    return firstValueFrom(this.loaded);
  }

  logout(): void {
    this.oauthService.logOut();
  }

  changePassword(): void {
    this.oauthService.initCodeFlow('', { kc_action: UPDATE_PASSWORD_ACTION });
  }

  async reload(): Promise<void> {
    await this.oauthService.refreshToken().catch(() => undefined);
    this.load();
  }

  updateCurrentAccountPreferences(preferences: AccountPreferencesUpdate) {
    return this.accountApi
      .updatePreferences(preferences)
      .pipe(tap((updated) => this.applyPreferences(updated)));
  }

  private applyPreferences(preferences: AccountPreferencesResponse) {
    this.translateService.use(preferences.language);
    this.currentAccount.update((account) => account && { ...account, preferences });
  }
}
