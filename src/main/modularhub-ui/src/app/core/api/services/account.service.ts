import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {HttpClient} from '@angular/common/http';
import {OAuthService} from 'angular-oauth2-oidc';
import {environment} from 'environments/environment';
import {Account} from 'app/core/api/models/account';
import {BehaviorSubject, Observable} from 'rxjs';
import {Role} from 'app/core/api/models/role';
import {AccountPreferences, AccountPreferencesRequest} from 'app/core/api/models/account-preferences';

@Injectable({
  providedIn: 'root',
})
export class AccountService {

  private readonly accountSubject = new BehaviorSubject<Account | null>(null);

  private readonly resourceUrl: string;
  private account?: Account;

  constructor(
    private readonly http: HttpClient,
    private readonly translateService: TranslateService,
    private readonly oauthService: OAuthService,
  ) {
    this.resourceUrl = `${environment.apiUrl}/account`;
  }

  load(): void {
    if (!this.oauthService.hasValidAccessToken()) {
      return;
    }

    this.http.get<Account>(this.resourceUrl).subscribe({
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

    return this.account.roles.some((r: Role) => roles.includes(r));
  }

  async reload() {
    this.oauthService.refreshToken().then(() => {
      this.load();
    });
  }

  updateCurrentAccountPreferences(preferences: AccountPreferencesRequest) {
    return this.http.put<AccountPreferences>(`${this.resourceUrl}/preferences`, preferences);
  }
}
