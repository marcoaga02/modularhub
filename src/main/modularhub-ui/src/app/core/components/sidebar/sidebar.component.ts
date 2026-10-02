import {Component, Signal} from '@angular/core';
import {NgClass} from '@angular/common';
import {Button} from 'primeng/button';
import {PluginService} from 'app/core/api/services/plugin.service';
import {Plugin} from 'app/core/api/models/plugin';
import {Router} from '@angular/router';
import {Tooltip} from 'primeng/tooltip';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {AccountService} from 'app/core/api/services/account.service';
import {ConfirmationService} from 'primeng/api';

@Component({
  selector: 'app-sidebar',
  imports: [
    NgClass,
    Button,
    Tooltip,
    TranslatePipe
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {

  protected isOpen: boolean = false;
  protected readonly plugins: Signal<Plugin[]>;

  constructor(
    pluginService: PluginService,
    private readonly confirmationService: ConfirmationService,
    private readonly translateService: TranslateService,
    private readonly accountService: AccountService,
    private readonly router: Router
  ) {
    this.plugins = pluginService.plugins;
  }

  protected toggle() {
    this.isOpen = !this.isOpen;
  }

  private close() {
    if (!this.isOpen) {
      return;
    }

    this.toggle();
  }

  protected navigate(path: string) {
    this.close();
    this.router.navigate([path]);
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
}
