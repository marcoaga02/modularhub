import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {OAuthService} from 'angular-oauth2-oidc';
import {BehaviorSubject, firstValueFrom, Observable, ReplaySubject} from 'rxjs';
import {Account, AccountApiService, AccountPreferencesUpdate} from 'app/core/api/generated';

@Injectable({
  providedIn: 'root',
})
export class AccountService {

  private readonly accountSubject = new BehaviorSubject<Account | null>(null);
  private readonly loaded = new ReplaySubject<void>(1);

  constructor(
    private readonly accountApi: AccountApiService,
    private readonly translateService: TranslateService,
    private readonly oauthService: OAuthService,
  ) {}

  load(): void {
    if (!this.oauthService.hasValidAccessToken()) {
      this.loaded.next();
      return;
    }

    this.accountApi.getCurrentAccount().subscribe({
      next: (account: Account) => {
        this.onSuccess(account);
      },
      error: () => {
        this.onError();
      }
    })
  }

  private onSuccess(accont: Account) {
    this.translateService.use(accont.preferences.language);
    this.accountSubject.next(accont);
    this.loaded.next();
  }

  private onError() {
    this.accountSubject.next(null);
    this.loaded.next();
  }

  whenLoaded(): Promise<void> {
    return firstValueFrom(this.loaded);
  }

  logout(): void {
    this.oauthService.logOut();
  }

  getObservableAccount(): Observable<Account | null> {
    return this.accountSubject.asObservable();
  }

  async reload(): Promise<void> {
    await this.oauthService.refreshToken().catch(() => undefined);
    this.load();
  }

  updateCurrentAccountPreferences(preferences: AccountPreferencesUpdate) {
    return this.accountApi.updatePreferences(preferences);
  }
}
