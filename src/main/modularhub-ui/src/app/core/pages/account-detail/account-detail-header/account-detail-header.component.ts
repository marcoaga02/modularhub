import { Component, computed, inject } from '@angular/core';
import { Avatar, AvatarPassThroughOptions } from 'primeng/avatar';
import { AccountService } from 'app/core/api/services/account.service';
import { getFullName, getInitials } from 'app/core/utils/functions/person-name';

@Component({
  selector: 'app-profile-dialog-header',
  imports: [Avatar],
  templateUrl: './account-detail-header.component.html',
  styleUrl: './account-detail-header.component.css',
})
export class AccountDetailHeaderComponent {
  protected readonly avatarPT: AvatarPassThroughOptions = {
    label: {
      class: 'text-base!',
    },
  };

  protected readonly account = inject(AccountService).account;
  protected readonly initials = computed(() => {
    const account = this.account();
    return account ? getInitials(account.firstName, account.lastName) : '';
  });
  protected readonly fullName = computed(() => {
    const account = this.account();
    return account ? getFullName(account.firstName, account.lastName) : '';
  });
}
