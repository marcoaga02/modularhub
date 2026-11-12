import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {OAuthService} from 'angular-oauth2-oidc';
import {BehaviorSubject, Observable} from 'rxjs';
import {Role} from 'app/core/api/models/role';
import {Account, AccountApiService, AccountPreferencesUpdate} from 'app/core/api/generated';

@Injectable({
  providedIn: 'root',
})
export class AccountService {

  private readonly accountSubject = new BehaviorSubject<Account | null>(null);

  private account?: Account;

  constructor(
    private readonly accountApi: AccountApiService,
    private readonly translateService: TranslateService,
    private readonly oauthService: OAuthService,
  ) {}

  load(): void {
    if (!this.oauthService.hasValidAccessToken()) {
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
    this.translateService.use(accont.preferences.language.code);
    this.account = accont;
    this.accountSubject.next(accont);
  }

  private onError() {
    this.account = undefined;
    this.accountSubject.next(null);
  }

  logout(): void {
    this.oauthService.logOut();
  }

  getObservableAccount(): Observable<Account | null> {
    return this.accountSubject.asObservable();
  }

  hasAnyPermission(roles: Role | Role[]): boolean {
    if (!this.account?.roles) {
      return false;
    }

    if (!Array.isArray(roles)) {
      roles = [roles];
    }

    return this.account.roles.some(r => roles.includes(r as Role));
  }

  async reload() {
    this.oauthService.refreshToken().then(() => {
      this.load();
    });
  }

  updateCurrentAccountPreferences(preferences: AccountPreferencesUpdate) {
    return this.accountApi.updatePreferences(preferences);
  }
}
