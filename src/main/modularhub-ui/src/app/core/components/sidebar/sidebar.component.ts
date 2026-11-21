import { Component, inject, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Button } from 'primeng/button';
import { Tooltip } from 'primeng/tooltip';
import { PluginService } from 'app/core/api/services/plugin.service';
import { AccountService } from 'app/core/api/services/account.service';
import { ConfirmDialogService } from 'app/core/utils/services/confirm-dialog.service';

@Component({
  selector: 'app-sidebar',
  imports: [NgClass, Button, Tooltip, TranslatePipe],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  private readonly router = inject(Router);
  private readonly accountService = inject(AccountService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected readonly isOpen = signal(false);
  protected readonly plugins = inject(PluginService).plugins;

  protected toggle() {
    this.isOpen.update((open) => !open);
  }

  protected navigate(path: string) {
    this.isOpen.set(false);
    this.router.navigate([path]);
  }

  protected logout() {
    this.confirmDialog.confirm('general.logout.title', 'general.logout.message', () =>
      this.accountService.logout(),
    );
  }
}
