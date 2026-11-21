import {Component, signal} from '@angular/core';
import {Avatar, AvatarPassThroughOptions} from "primeng/avatar";
import {AccountService} from 'app/core/api/services/account.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Account} from 'app/core/api/generated';

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
      .pipe(takeUntilDestroyed())
      .subscribe(account => {
        if (!account) {
          return;
        }

        this.currentAccount = account;
        this.loading.set(false);
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
