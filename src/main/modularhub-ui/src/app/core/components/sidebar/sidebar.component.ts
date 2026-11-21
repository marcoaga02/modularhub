import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Tooltip } from 'primeng/tooltip';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AccountService } from 'app/core/api/services/account.service';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, Tooltip, TranslatePipe],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private readonly accountService = inject(AccountService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected readonly isOpen = signal(true);
  protected readonly plugins = inject(PluginService).plugins;

  protected toggle() {
    this.isOpen.update((open) => !open);
  }

  protected logout() {
    this.confirmDialog.confirm('general.logout.title', 'general.logout.message', () =>
      this.accountService.logout(),
    );
  }
}
