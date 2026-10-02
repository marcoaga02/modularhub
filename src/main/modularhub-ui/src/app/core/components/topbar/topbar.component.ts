import {Component, ElementRef, HostListener, signal, Signal} from '@angular/core';
import {Toolbar} from 'primeng/toolbar';
import {PluginService} from 'app/core/api/services/plugin.service';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NavigationEnd, Router} from '@angular/router';
import {Plugin} from 'app/core/api/models/plugin';
import {filter, retry} from 'rxjs';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {Avatar, AvatarPassThroughOptions} from 'primeng/avatar';
import {AccountService} from 'app/core/api/services/account.service';
import {Account} from 'app/core/api/models/account';
import {MAX_RETRY_ATTEMPTS, RETRY_DELAY_MS} from 'app/app.config';
import {Popover, PopoverPassThroughOptions} from 'primeng/popover';
import {ConfirmationService} from 'primeng/api';
import {DialogService} from 'primeng/dynamicdialog';
import {AccountDetailComponent} from 'app/core/pages/account-detail/account-detail.component';
import {
  AccountDetailHeaderComponent
} from 'app/core/pages/account-detail/account-detail-header/account-detail-header.component';

@Component({
  selector: 'app-topbar',
  imports: [
    Toolbar,
    TranslatePipe,
    Avatar,
    Popover
  ],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.css',
})
export class TopbarComponent {

  protected avatarPT: AvatarPassThroughOptions = {
    label: {
      class: "text-sm!"
    },
    root: {
      class: "w-10! h-10! cursor-pointer!"
    }
  };

  protected popoverPT: PopoverPassThroughOptions = {
    content: {
      class: 'p-0!'
    },
    root: {
      class: 'rounded-xl!'
    }
  };

  protected readonly plugins: Signal<Plugin[]>;
  protected loading = signal(true);

  protected currentPlugin?: Plugin;
  protected currentAccount?: Account;

  constructor(
    private readonly router: Router,
    private readonly pluginService: PluginService,
    private readonly accountService: AccountService,
    private readonly confirmationService: ConfirmationService,
    private readonly translateService: TranslateService,
    private readonly dialogService: DialogService,
    private readonly elementRef: ElementRef
  ) {
    this.plugins = this.pluginService.plugins;

    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        takeUntilDestroyed()
      )
      .subscribe(() => {
        if (this.plugins().length === 0) {
          return;
        }

        this.updateCurrentPlugin();
      });

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

  private updateCurrentPlugin() {
    const url = this.router.url.split('/')[1].split('?')[0];

    this.currentPlugin = this.plugins().find((p) => p.path === url);
  }

  protected getInitials(): string {
    if (!this.currentAccount) {
      return "";
    }

    return `${this.currentAccount.firstName?.[0] ?? ''}${this.currentAccount.lastName?.[0] ?? ''}`.toUpperCase();
  }

  protected getFullName() {
    if (!this.currentAccount) {
      return "";
    }

    return `${this.currentAccount.firstName} ${this.currentAccount.lastName}`;
  }

  protected logout() {
    this.confirmationService.confirm({
      header: this.translateService.instant('general.logout.title'),
      message: this.translateService.instant('general.logout.message'),
      closable: true,
      closeOnEscape: true,
      icon: 'pi pi-exclamation-triangle',
      rejectButtonProps: {
        label: this.translateService.instant('general.button.label.cancel'),
        severity: 'secondary',
        outlined: true,
      },
      acceptButtonProps: {
        label: this.translateService.instant('general.button.label.confirm'),
        severity: 'secondary',
      },
      accept: () => {
        this.accountService.logout();
      },
    });
  }

  protected onProfileClick() {
    this.dialogService.open(AccountDetailComponent, {
      style: {'max-width': '60vw', 'min-width': '40rem' },
      closable: true,
      closeOnEscape: true,
      templates: {
        header: AccountDetailHeaderComponent
      }
    })?.onClose.subscribe(() => {

    });
  }

  protected menuOpen = false;

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.menuOpen = false;
    }
  }
}
