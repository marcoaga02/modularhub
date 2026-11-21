import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { Toolbar } from 'primeng/toolbar';
import { Avatar, AvatarPassThroughOptions } from 'primeng/avatar';
import { Popover, PopoverPassThroughOptions } from 'primeng/popover';
import { DialogService } from 'primeng/dynamicdialog';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AccountService } from 'app/core/api/services/account.service';
import { getFullName, getInitials } from 'app/core/utils/functions/person-name';
import { AccountDetailComponent } from 'app/core/pages/account-detail/account-detail.component';
import { FORM_DIALOG_CONFIG } from 'app/core/utils/constants/dialog';

@Component({
  selector: 'app-topbar',
  imports: [Toolbar, TranslatePipe, Avatar, Popover],
  templateUrl: './topbar.component.html',
})
export class TopbarComponent {
  private readonly router = inject(Router);
  private readonly accountService = inject(AccountService);
  private readonly dialogService = inject(DialogService);

  protected readonly avatarPT: AvatarPassThroughOptions = {
    root: { class: 'cursor-pointer' },
  };

  protected readonly popoverPT: PopoverPassThroughOptions = {
    root: { class: 'rounded-xl before:hidden after:hidden' },
    content: { class: 'p-0' },
  };

  private readonly plugins = inject(PluginService).plugins;
  private readonly url = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  protected readonly currentPlugin = computed(() => {
    const path = this.url().split('/')[1].split('?')[0];
    return this.plugins().find((p) => p.path === path);
  });

  protected readonly account = this.accountService.account;
  protected readonly initials = computed(() => {
    const account = this.account();
    return account ? getInitials(account.firstName, account.lastName) : '';
  });
  protected readonly fullName = computed(() => {
    const account = this.account();
    return account ? getFullName(account.firstName, account.lastName) : '';
  });

  protected logout() {
    this.accountService.logout();
  }

  protected onProfileClick() {
    this.dialogService.open(AccountDetailComponent, {
      ...FORM_DIALOG_CONFIG,
      width: '32rem',
      closeOnEscape: true,
      dismissableMask: true,
    });
  }
}
