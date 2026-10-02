import {Component, signal} from '@angular/core';
import {Avatar, AvatarPassThroughOptions} from "primeng/avatar";
import {AccountService} from 'app/core/api/services/account.service';
import {retry} from 'rxjs';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {Account} from 'app/core/api/models/account';

@Component({
  selector: 'app-profile-dialog-header',
    imports: [
        Avatar
    ],
  templateUrl: './account-detail-header.component.html',
  styleUrl: './account-detail-header.component.css',
})
export class AccountDetailHeaderComponent {

  protected avatarPT: AvatarPassThroughOptions = {
    label: {
      class: "text-base!"
    }
  };

  protected loading = signal(true);
  protected currentAccount?: Account;

  constructor(
    private readonly accountService: AccountService
  ) {
    this.accountService.getObservableAccount()
      .pipe(retry({count: MAX_RETRY_ATTEMPTS, delay: RETRY_DELAY_MS}))
      .subscribe({
        next: (account) => {
          if (!account) {
            return;
          }

          this.currentAccount = account;
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
        }
      });
  }

  protected getFullName() {
    if (!this.currentAccount) {
      return "";
    }

    return `${this.currentAccount.firstName} ${this.currentAccount.lastName}`;
  }

  protected getInitials(): string {
    if (!this.currentAccount) {
      return "";
    }

    return `${this.currentAccount.firstName?.[0] ?? ''}${this.currentAccount.lastName?.[0] ?? ''}`.toUpperCase();
  }

}
